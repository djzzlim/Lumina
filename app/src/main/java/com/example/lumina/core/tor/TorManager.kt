package com.example.lumina.core.tor

import android.content.Context
import android.util.Log
import com.example.lumina.core.SettingsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TorManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore
) {
    private val TAG = "TorManager"
    private var torProcess: Process? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isTorRunning = MutableStateFlow(false)
    val isTorRunning: StateFlow<Boolean> = _isTorRunning

    private val _bootstrappingProgress = MutableStateFlow(0)
    val bootstrappingProgress: StateFlow<Int> = _bootstrappingProgress

    private val _torLogs = MutableStateFlow("")
    val torLogs: StateFlow<String> = _torLogs

    init {
        scope.launch {
            settingsDataStore.torEnabledFlow.collectLatest { enabled ->
                if (enabled) {
                    startTor()
                } else {
                    stopTor()
                }
            }
        }
    }

    private fun startTor() {
        if (_isTorRunning.value) return

        scope.launch(Dispatchers.IO) {
            try {
                _bootstrappingProgress.value = 0
                val torBinary = prepareTorBinary()
                if (torBinary == null) {
                    Log.e(TAG, "Failed to prepare Tor binary")
                    return@launch
                }

                val torrc = prepareTorrc()
                val command = arrayOf(torBinary.absolutePath, "-f", torrc.absolutePath)

                Log.d(TAG, "Starting Tor with command: ${command.joinToString(" ")}")
                
                val processBuilder = ProcessBuilder(*command)
                    .directory(context.filesDir)
                    .redirectErrorStream(true)

                torProcess = processBuilder.start()
                _isTorRunning.value = true

                torProcess?.inputStream?.bufferedReader()?.use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        Log.d(TAG, "Tor: $line")
                        _torLogs.value += "$line\n"
                        
                        // Parse bootstrapping progress
                        if (line?.contains("Bootstrapped") == true) {
                            val regex = """Bootstrapped (\d+)%""".toRegex()
                            val match = regex.find(line!!)
                            match?.groupValues?.get(1)?.toIntOrNull()?.let { progress ->
                                _bootstrappingProgress.value = progress
                            }
                        }

                        if (line?.contains("Bootstrapped 100%") == true) {
                            Log.i(TAG, "Tor bootstrapped successfully")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting Tor", e)
                _isTorRunning.value = false
                _bootstrappingProgress.value = 0
            }
        }
    }

    private fun stopTor() {
        Log.d(TAG, "Stopping Tor")
        torProcess?.destroy()
        torProcess = null
        _isTorRunning.value = false
        _bootstrappingProgress.value = 0
    }

    private fun prepareTorBinary(): File? {
        val nativeLibDir = context.applicationInfo.nativeLibraryDir
        // Guardian Project's tor-android usually names the binary libtor.so
        val libTor = File(nativeLibDir, "libtor.so")
        
        if (libTor.exists()) {
            Log.d(TAG, "Found Tor binary at ${libTor.absolutePath}")
            return libTor
        } else {
            Log.e(TAG, "Tor binary NOT found in $nativeLibDir")
            // Log available files for debugging
            File(nativeLibDir).listFiles()?.forEach { 
                Log.d(TAG, "Available lib: ${it.name}")
            }
        }
        
        return null
    }

    private fun prepareTorrc(): File {
        val torrc = File(context.filesDir, "torrc")
        val dataDir = File(context.filesDir, "tor_data")
        if (!dataDir.exists()) dataDir.mkdirs()

        val content = """
            DataDirectory ${dataDir.absolutePath}
            SocksPort 9050
            ControlPort 9051
            CookieAuthentication 1
        """.trimIndent()

        torrc.writeText(content)
        return torrc
    }
}
