package com.example.lumina.core.tor

import android.content.Context
import android.util.Log
import com.example.lumina.core.SettingsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
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
    
    private val _exitNodeTimezone = MutableStateFlow<String?>(null)
    val exitNodeTimezone: StateFlow<String?> = _exitNodeTimezone

    private val _exitNodeOffsetMinutes = MutableStateFlow<Int>(0)
    val exitNodeOffsetMinutes: StateFlow<Int> = _exitNodeOffsetMinutes

    private val _exitNodeIp = MutableStateFlow<String?>(null)
    val exitNodeIp: StateFlow<String?> = _exitNodeIp

    init {
        scope.launch {
            settingsDataStore.torEnabledFlow.collectLatest { enabled ->
                if (enabled) {
                    startTor()
                } else {
                    stopTor()
                    _exitNodeTimezone.value = null
                    _exitNodeOffsetMinutes.value = 0
                }
            }
        }
        
        // Monitor bootstrapping to fetch timezone when ready
        scope.launch {
            _bootstrappingProgress.collect { progress ->
                if (progress == 100) {
                    fetchExitNodeTimezone()
                }
            }
        }
    }

    private suspend fun fetchExitNodeTimezone() {
        var retries = 5
        val url = "https://ipwho.is/"
        
        while (retries > 0) {
            delay(3000) // Wait for Tor to stabilize
            
            val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", 9050))
            val client = OkHttpClient.Builder()
                .proxy(proxy)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()

            try {
                Log.d(TAG, "Fetching location from $url (Attempt ${6-retries})...")
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val json = JSONObject(body)
                        val success = json.optBoolean("success", false)
                        if (success) {
                            val ip = json.optString("ip")
                            val countryCode = json.optString("country_code")
                            val connection = json.optJSONObject("connection")
                            val timezoneObj = json.optJSONObject("timezone")
                            
                            val timezoneId = timezoneObj?.optString("id") ?: "UTC"
                            val offsetSec = timezoneObj?.optInt("offset", 0) ?: 0
                            val offsetMin = offsetSec / 60
                            
                            Log.i(TAG, "Tor Exit Node Identified: $ip in $countryCode. Timezone: $timezoneId (Offset: $offsetMin min)")
                            _exitNodeTimezone.value = timezoneId
                            _exitNodeOffsetMinutes.value = offsetMin
                            _exitNodeIp.value = ip
                            _torLogs.value += "Exit Node: $ip ($countryCode / $timezoneId)\n"
                            return // Success
                        } else {
                            Log.w(TAG, "API returned success=false: ${json.optJSONObject("message")}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch from $url: ${e.message}")
            }
            
            retries--
            delay(2000)
        }
        
        Log.e(TAG, "Timezone detection failed after all retries. Defaulting to UTC.")
        _exitNodeTimezone.value = "UTC"
        _exitNodeOffsetMinutes.value = 0
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
