package com.example.lumina.ui.extensions

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumina.network.RetrofitClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.browser.state.state.WebExtensionState
import okhttp3.ResponseBody
import org.mozilla.geckoview.GeckoRuntime
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ExtensionsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geckoRuntime: GeckoRuntime
) : ViewModel() {

    private val _extensions = MutableStateFlow<List<WebExtensionState>>(emptyList())
    val extensions: StateFlow<List<WebExtensionState>> = _extensions.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<String, Float?>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float?>> = _downloadProgress.asStateFlow()

    private val _updateAvailable = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val updateAvailable: StateFlow<Map<String, Boolean>> = _updateAvailable.asStateFlow()

    init {
        // Initialize with default list
        _extensions.value = listOf(
            WebExtensionState(
                id = "uBlock0@raymondhill.net",
                name = "uBlock Origin",
                enabled = true,
                allowedInPrivateBrowsing = true
            )
        )
        // Check if already installed on runtime
        refreshInstalledStatus()
        checkForUpdates()
    }

    private fun refreshInstalledStatus() {
        // Check if the file exists in our internal storage to restore UI state
        _extensions.value = _extensions.value.map { extension ->
            val file = File(context.filesDir, "${extension.id}.xpi")
            if (file.exists()) {
                extension.copy(url = "file://${file.absolutePath}")
            } else {
                extension.copy(url = null)
            }
        }
        
        // Also ensure they are registered in the runtime if files exist
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                _extensions.value.forEach { ext ->
                    ext.url?.let { url ->
                        geckoRuntime.webExtensionController.install(url).accept(
                            { extension ->
                                if (extension != null) {
                                    geckoRuntime.webExtensionController.setAllowedInPrivateBrowsing(extension, true)
                                }
                            },
                            { error -> Log.e("ExtensionsViewModel", "Failed to re-register ${ext.id}", error) }
                        )
                    }
                }
            }
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _extensions.value.forEach { extension ->
                if (extension.id == "uBlock0@raymondhill.net") {
                    try {
                        RetrofitClient.instance.getLatestRelease("gorhill", "uBlock")
                        // In real use, you'd compare current version vs release.tagName
                        if (extension.url != null) {
                             _updateAvailable.value = _updateAvailable.value.toMutableMap().apply {
                                this[extension.id] = true // Hardcoded for demo
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ExtensionsViewModel", "Update check failed", e)
                    }
                }
            }
        }
    }

    fun downloadExtension(extension: WebExtensionState) {
        viewModelScope.launch {
            _downloadProgress.value = _downloadProgress.value.toMutableMap().apply {
                this[extension.id] = -1f
            }
            try {
                val release = RetrofitClient.instance.getLatestRelease("gorhill", "uBlock")
                val asset = release.assets.find { it.name.endsWith(".xpi") }
                if (asset != null) {
                    val response = RetrofitClient.instance.downloadFile(asset.browserDownloadUrl)
                    val downloadedPath = saveFile(response.body(), "${extension.id}.xpi", extension.id, context.filesDir)
                    installExtension(downloadedPath, extension.id)
                }
            } catch (e: Exception) {
                Log.e("ExtensionsViewModel", "Download failed", e)
                _downloadProgress.value = _downloadProgress.value.toMutableMap().apply {
                    this[extension.id] = null
                }
            }
        }
    }

    private suspend fun saveFile(body: ResponseBody?, fileName: String, extensionId: String, directory: File): String {
        if (body == null) throw Exception("Response body is null")

        return withContext(Dispatchers.IO) {
            val file = File(directory, fileName)
            body.byteStream().use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    val fileSize = body.contentLength()
                    var fileSizeDownloaded: Long = 0
                    var read: Int
                    
                    _downloadProgress.value = _downloadProgress.value.toMutableMap().apply {
                        this[extensionId] = 0f
                    }

                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        fileSizeDownloaded += read
                        if (fileSize > 0) {
                            val progress = (fileSizeDownloaded * 100 / fileSize).toFloat()
                            _downloadProgress.value = _downloadProgress.value.toMutableMap().apply {
                                this[extensionId] = progress
                            }
                        }
                    }
                    output.flush()
                }
            }
            file.absolutePath
        }
    }

    private suspend fun installExtension(path: String, id: String) {
        withContext(Dispatchers.Main) {
            val file = File(path)
            if (file.exists()) {
                val uri = "file://${file.absolutePath}"
                Log.d("ExtensionsViewModel", "Installing from: $uri")
                
                geckoRuntime.webExtensionController.install(uri).accept(
                    { extension ->
                        Log.d("ExtensionsViewModel", "Installation successful: ${extension?.id}")
                        if (extension != null) {
                            // Enable in Private Browsing (important for Lumina!)
                            geckoRuntime.webExtensionController.setAllowedInPrivateBrowsing(extension, true)
                            
                            // Update UI state
                            _extensions.value = _extensions.value.map {
                                if (it.id == extension.id) it.copy(url = uri) else it
                            }
                            _updateAvailable.value = _updateAvailable.value.toMutableMap().apply {
                                this[extension.id] = false
                            }
                        }
                        _downloadProgress.value = _downloadProgress.value.toMutableMap().apply {
                            this[id] = null
                        }
                    },
                    { error ->
                        Log.e("ExtensionsViewModel", "Installation failed", error)
                        _downloadProgress.value = _downloadProgress.value.toMutableMap().apply {
                            this[id] = null
                        }
                    }
                )
            }
        }
    }

    fun uninstallExtension(extension: WebExtensionState) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                geckoRuntime.webExtensionController.list().accept { installedExtensions ->
                    val geckoExtension = installedExtensions?.find { it.id == extension.id }
                    if (geckoExtension != null) {
                        geckoRuntime.webExtensionController.uninstall(geckoExtension)
                    }
                }
            }
            
            val file = File(context.filesDir, "${extension.id}.xpi")
            if (file.exists()) file.delete()
            
            _extensions.value = _extensions.value.map {
                if (it.id == extension.id) it.copy(url = null) else it
            }
            _updateAvailable.value = _updateAvailable.value.toMutableMap().apply {
                remove(extension.id)
            }
        }
    }
}
