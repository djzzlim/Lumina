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

/**
 * Manages the lifecycle and interaction with the Tor proxy.
 * This class handles starting and stopping the Tor process, monitoring its bootstrapping progress,
 * and fetching information about the Tor exit node, such as its timezone and IP address.
 *
 * @property context The application context, injected by Hilt.
 * @property settingsDataStore Data store for application settings, including the Tor enable status.
 */
@Singleton
class TorManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore
) {
    private val TAG = "TorManager"
    private var torProcess: Process? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isTorRunning = MutableStateFlow(false)

    /**
     * A [StateFlow] indicating whether the Tor process is currently running.
     */
    val isTorRunning: StateFlow<Boolean> = _isTorRunning

    private val _bootstrappingProgress = MutableStateFlow(0)

    /**
     * A [StateFlow] providing the current bootstrapping progress of the Tor client (0-100%).
     */
    val bootstrappingProgress: StateFlow<Int> = _bootstrappingProgress

    private val _torLogs = MutableStateFlow("")

    /**
     * A [StateFlow] containing the aggregated logs from the Tor process.
     */
    val torLogs: StateFlow<String> = _torLogs
    
    private val _exitNodeTimezone = MutableStateFlow<String?>(null)

    /**
     * A [StateFlow] holding the timezone ID of the Tor exit node (e.g., "America/New_York").
     * Null if not yet determined or Tor is not running.
     */
    val exitNodeTimezone: StateFlow<String?> = _exitNodeTimezone

    private val _exitNodeOffsetMinutes = MutableStateFlow<Int>(0)

    /**
     * A [StateFlow] holding the UTC offset in minutes of the Tor exit node.
     * 0 if not yet determined or Tor is not running.
     */
    val exitNodeOffsetMinutes: StateFlow<Int> = _exitNodeOffsetMinutes

    private val _exitNodeIp = MutableStateFlow<String?>(null)

    /**
     * A [StateFlow] holding the IP address of the Tor exit node.
     * Null if not yet determined or Tor is not running.
     */
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
        
        // Monitor bootstrapping and periodically refresh timezone info
        scope.launch {
            _bootstrappingProgress.collect { progress ->
                if (progress == 100) {
                    while (_isTorRunning.value) {
                        fetchExitNodeTimezone()
                        delay(TimeUnit.MINUTES.toMillis(5)) // Refresh every 5 minutes
                    }
                }
            }
        }
    }

    /**
     * Fetches the timezone, offset, and IP address of the current Tor exit node.
     * This is done by making a request through the Tor proxy to a public IP information service.
     * Retries multiple times if the initial attempts fail.
     */
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
                            
                            _exitNodeTimezone.value = timezoneId
                            _exitNodeOffsetMinutes.value = offsetMin
                            _exitNodeIp.value = ip
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

    /**
     * Starts the Tor proxy process.
     * Prepares the Tor binary and configuration file (torrc), then executes the Tor command.
     * Monitors the process's output for bootstrapping progress and logs.
     */
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
                // If Tor was intentionally stopped, this exception (read interrupted) is expected
                if (e is java.io.IOException && (e.message?.contains("read interrupted") == true || e is java.io.InterruptedIOException)) {
                    Log.d(TAG, "Tor log reader interrupted (Tor stopping)")
                } else {
                    Log.e(TAG, "Error starting Tor", e)
                }
                _isTorRunning.value = false
                _bootstrappingProgress.value = 0
            }
        }
    }

    /**
     * Stops the running Tor proxy process.
     */
    private fun stopTor() {
        Log.d(TAG, "Stopping Tor")
        torProcess?.destroy()
        torProcess = null
        _isTorRunning.value = false
        _bootstrappingProgress.value = 0
    }

    /**
     * Signals Tor to switch to a new identity (NEWNYM).
     * This will result in a new exit node and IP address for future requests.
     */
    fun requestNewNym() {
        scope.launch(Dispatchers.IO) {
            try {
                val cookieFile = File(context.filesDir, "tor_data/control_auth_cookie")
                if (!cookieFile.exists()) {
                    Log.e(TAG, "Tor control cookie not found. Cannot send NEWNYM.")
                    return@launch
                }

                val cookieHex = cookieFile.readBytes().joinToString("") { "%02x".format(it) }

                java.net.Socket("127.0.0.1", 9051).use { socket ->
                    val writer = socket.getOutputStream().bufferedWriter()
                    val reader = socket.getInputStream().bufferedReader()

                    writer.write("AUTHENTICATE $cookieHex\r\n")
                    writer.flush()
                    if (reader.readLine()?.startsWith("250") == true) {
                        writer.write("SIGNAL NEWNYM\r\n")
                        writer.flush()
                        if (reader.readLine()?.startsWith("250") == true) {
                            Log.i(TAG, "Tor NEWNYM signal sent successfully")
                            _exitNodeIp.value = null // Clear to trigger refresh
                            _exitNodeTimezone.value = null
                            fetchExitNodeTimezone()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send NEWNYM signal: ${e.message}")
            }
        }
    }

    /**
     * Locates and returns the Tor binary executable file.
     * It looks for "libtor.so" in the application's native library directory.
     *
     * @return The [File] object for the Tor binary, or null if not found.
     */
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

    /**
     * Creates and configures the `torrc` file, which is the configuration file for Tor.
     * It specifies the data directory, SocksPort, ControlPort, and enables cookie authentication.
     *
     * @return The [File] object for the created `torrc` file.
     */
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
