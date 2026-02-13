package com.example.lumina.core.di

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import com.example.lumina.BuildConfig
import com.example.lumina.core.AppPreferences
import com.example.lumina.core.SettingsDataStore
import com.example.lumina.core.tor.TorManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.mozilla.geckoview.ContentBlocking
import org.mozilla.geckoview.ExperimentalGeckoViewApi
import org.mozilla.geckoview.GeckoPreferenceController
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.WebExtension
import org.mozilla.geckoview.WebExtensionController
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GeckoRuntimeModule {

    @OptIn(ExperimentalGeckoViewApi::class)
    @Provides
    @Singleton
    fun provideGeckoRuntime(
        @ApplicationContext context: Context,
        appPreferences: AppPreferences,
        settingsDataStore: SettingsDataStore,
        torManager: TorManager,
        luminaRepository: com.example.lumina.core.LuminaRepository
    ): GeckoRuntime {
        // 1. Set Preferences BEFORE creating the runtime to ensure they take effect immediately
        
        // Tor Proxy Settings
        val torEnabled = runBlocking { settingsDataStore.torEnabledFlow.first() }
        if (torEnabled) {
            GeckoPreferenceController.setGeckoPref("network.proxy.type", 1, GeckoPreferenceController.PREF_BRANCH_USER)
            GeckoPreferenceController.setGeckoPref("network.proxy.socks", "127.0.0.1", GeckoPreferenceController.PREF_BRANCH_USER)
            GeckoPreferenceController.setGeckoPref("network.proxy.socks_port", 9050, GeckoPreferenceController.PREF_BRANCH_USER)
            GeckoPreferenceController.setGeckoPref("network.proxy.socks_remote_dns", true, GeckoPreferenceController.PREF_BRANCH_USER)
            GeckoPreferenceController.setGeckoPref("network.proxy.socks_version", 5, GeckoPreferenceController.PREF_BRANCH_USER)
        } else {
            GeckoPreferenceController.setGeckoPref("network.proxy.type", 0, GeckoPreferenceController.PREF_BRANCH_USER)
        }

        val torProfile = runBlocking { settingsDataStore.torProfileFlow.first() }
        when (torProfile) {
            "Safer" -> {
                GeckoPreferenceController.setGeckoPref("javascript.enabled", true, GeckoPreferenceController.PREF_BRANCH_USER)
                GeckoPreferenceController.setGeckoPref("svg.disabled", true, GeckoPreferenceController.PREF_BRANCH_USER)
            }
            "Safest" -> {
                GeckoPreferenceController.setGeckoPref("javascript.enabled", false, GeckoPreferenceController.PREF_BRANCH_USER)
                GeckoPreferenceController.setGeckoPref("svg.disabled", true, GeckoPreferenceController.PREF_BRANCH_USER)
            }
            else -> { // Standard
                GeckoPreferenceController.setGeckoPref("javascript.enabled", true, GeckoPreferenceController.PREF_BRANCH_USER)
                GeckoPreferenceController.setGeckoPref("svg.disabled", false, GeckoPreferenceController.PREF_BRANCH_USER)
            }
        }

        // Silence "Native manifests not supported" and Managed Storage spam
        GeckoPreferenceController.setGeckoPref("browser.storage.managed.enabled", false, GeckoPreferenceController.PREF_BRANCH_USER)
        GeckoPreferenceController.setGeckoPref("extensions.managedStorage.enabled", false, GeckoPreferenceController.PREF_BRANCH_USER)
        GeckoPreferenceController.setGeckoPref("extensions.webextensions.managedStorage", false, GeckoPreferenceController.PREF_BRANCH_USER)
        GeckoPreferenceController.setGeckoPref("extensions.webextensions.nativeMessaging", false, GeckoPreferenceController.PREF_BRANCH_USER)

        // Fix InstallException: -5 (ERROR_SIGNATURE_INVALID)
        GeckoPreferenceController.setGeckoPref("xpinstall.signatures.required", false, GeckoPreferenceController.PREF_BRANCH_USER)
        GeckoPreferenceController.setGeckoPref("extensions.webapi.testing", true, GeckoPreferenceController.PREF_BRANCH_USER)

        // Set the Google Safe Browsing API Key
        GeckoPreferenceController.setGeckoPref(
            "browser.safebrowsing.key.google",
            BuildConfig.SAFE_BROWSING_KEY,
            GeckoPreferenceController.PREF_BRANCH_USER
        )

        val dnsProvider = runBlocking { appPreferences.dnsProviderFlow.first() }
        val isolationStrategy = runBlocking { appPreferences.isolationStrategyFlow.first() }

        val contentBlocking = ContentBlocking.Settings.Builder()
            .safeBrowsing(ContentBlocking.SafeBrowsing.DEFAULT)
            .enhancedTrackingProtectionLevel(ContentBlocking.EtpLevel.DEFAULT)
            .build()

        // Compatibility Prefs for modern websites (Cloudflare, CAPTCHAs)
        GeckoPreferenceController.setGeckoPref("dom.storage.enabled", true, GeckoPreferenceController.PREF_BRANCH_USER)
        GeckoPreferenceController.setGeckoPref("dom.indexedDB.enabled", true, GeckoPreferenceController.PREF_BRANCH_USER)
        GeckoPreferenceController.setGeckoPref("javascript.options.wasm", true, GeckoPreferenceController.PREF_BRANCH_USER)
        GeckoPreferenceController.setGeckoPref("network.cookie.cookieBehavior", 4, GeckoPreferenceController.PREF_BRANCH_USER) // Partitioned cookies
        GeckoPreferenceController.setGeckoPref("dom.w3c_touch_events.enabled", 2, GeckoPreferenceController.PREF_BRANCH_USER) // Enable touch events
        GeckoPreferenceController.setGeckoPref("network.http.sendRefererHeader", 2, GeckoPreferenceController.PREF_BRANCH_USER) // Send referrers
        GeckoPreferenceController.setGeckoPref("privacy.partition.network_state", true, GeckoPreferenceController.PREF_BRANCH_USER)

        val runtimeSettings = GeckoRuntimeSettings.Builder()
            .aboutConfigEnabled(true)
            .fissionEnabled(true)
            .trustedRecursiveResolverUri(dnsProvider.uri)
            .trustedRecursiveResolverMode(dnsProvider.mode)
            .allowInsecureConnections(GeckoRuntimeSettings.ALLOW_ALL)
            .contentBlocking(contentBlocking)
            .build()
            .setWebContentIsolationStrategy(isolationStrategy)

        val runtime = GeckoRuntime.create(context, runtimeSettings)

        // 2. Set PromptDelegate to auto-grant permissions (including Private Browsing)
        runtime.webExtensionController.promptDelegate = object : WebExtensionController.PromptDelegate {
            override fun onInstallPromptRequest(
                extension: WebExtension,
                permissions: Array<out String>,
                origins: Array<out String>,
                dataCollectionPermissions: Array<out String>
            ): GeckoResult<WebExtension.PermissionPromptResponse>? {
                Log.d("Lumina-Gecko", "Auto-granting permissions for: ${extension.id}")
                return GeckoResult.fromValue(WebExtension.PermissionPromptResponse(
                    true, true, true
                ))
            }
        }

        // Install the built-in Timezone Spoofer extension
        runtime.webExtensionController.installBuiltIn("resource://android/assets/extensions/timezone_spoofer/")

        // 3. Set AddonManagerDelegate to monitor extension lifecycle
        runtime.webExtensionController.setAddonManagerDelegate(object : WebExtensionController.AddonManagerDelegate {
            override fun onInstalled(extension: WebExtension) {
                Log.d("Lumina-Gecko", "Extension installed: ${extension.id}")
                
                extension.setActionDelegate(object : WebExtension.ActionDelegate {})
                
                // Handle messages from the Timezone Spoofer extension
                if (extension.id == "timezone-spoofer@lumina.example.com") {
                    extension.setMessageDelegate(object : WebExtension.MessageDelegate {
                        override fun onMessage(
                            nativeApp: String,
                            message: Any,
                            sender: WebExtension.MessageSender
                        ): GeckoResult<Any>? {
                            Log.d("Lumina-Gecko", "Received extension message: $message")
                            if (message is JSONObject && message.optString("type") == "getTimezone") {
                                val result = GeckoResult<Any>()
                                
                                // Parse lumina ID from contextId (e.g., "lumina_session_123")
                                val contextId = sender.session?.settings?.contextId
                                val luminaId = contextId?.substringAfterLast("_")?.toLongOrNull()
                                
                                val systemTz = java.util.TimeZone.getDefault().id
                                val useNetworkTz = runBlocking { settingsDataStore.useNetworkTimezoneFlow.first() }
                                val torOn = torManager.isTorRunning.value 
                                val detectedTimezone = torManager.exitNodeTimezone.value
                                val detectedOffset = torManager.exitNodeOffsetMinutes.value

                                // Fetch session-specific spoofing preference
                                CoroutineScope(Dispatchers.Main).launch {
                                    // Always enable spoofing now that the toggle is removed from UI
                                    val spoofEnabled = true
                                    
                                    Log.d("Lumina-Gecko", "Processing getTimezone: ID=$luminaId, spoofEnabled=$spoofEnabled")

                                    val response = JSONObject()
                                    response.put("systemTimezone", systemTz)

                                    if (!spoofEnabled) {
                                        response.put("timezone", "disabled")
                                    } else if (useNetworkTz) {
                                        if (torOn) {
                                            Log.d("Lumina-Gecko", "Tor is ON. Detected Exit TZ: $detectedTimezone")
                                            if (detectedTimezone != null) {
                                                response.put("timezone", detectedTimezone)
                                                response.put("offset", detectedOffset)
                                            } else {
                                                response.put("timezone", null)
                                            }
                                        } else {
                                            Log.d("Lumina-Gecko", "Tor is OFF. Telling extension to check network vs system: $systemTz")
                                            response.put("timezone", "system")
                                        }
                                    } else {
                                        Log.d("Lumina-Gecko", "Spoof enabled but useNetworkTz is FALSE. Requesting fallback.")
                                        response.put("timezone", "fallback")
                                    }
                                    
                                    Log.d("Lumina-Gecko", "Sending response to extension: $response")
                                    result.complete(response)
                                }
                                return result
                            }
                            return null
                        }
                    }, "lumina")
                } else {
                    extension.setMessageDelegate(object : WebExtension.MessageDelegate {}, "lumina")
                }
            }

            override fun onInstallationFailed(extension: WebExtension?, error: WebExtension.InstallException) {
                Log.e("Lumina-Gecko", "Installation failed for ${extension?.id}: ${error.message} (code: ${error.code})", error)
            }

            override fun onReady(extension: WebExtension) {
                Log.d("Lumina-Gecko", "Extension ready: ${extension.id}")
                extension.setActionDelegate(object : WebExtension.ActionDelegate {})
            }
        })

        return runtime
    }
}
