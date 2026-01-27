package com.example.lumina.core.di

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import com.example.lumina.BuildConfig
import com.example.lumina.core.AppPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
        appPreferences: AppPreferences
    ): GeckoRuntime {
        // 1. Set Preferences BEFORE creating the runtime to ensure they take effect immediately
        
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
            .enhancedTrackingProtectionLevel(ContentBlocking.EtpLevel.STRICT)
            .build()

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
                // The second parameter 'true' grants Private Browsing access immediately,
                // preventing the need for a manual restart later.
                return GeckoResult.fromValue(WebExtension.PermissionPromptResponse(
                    true, true, true
                ))
            }
        }

        // 3. Set AddonManagerDelegate to monitor extension lifecycle
        runtime.webExtensionController.setAddonManagerDelegate(object : WebExtensionController.AddonManagerDelegate {
            override fun onInstalled(extension: WebExtension) {
                Log.d("Lumina-Gecko", "Extension installed: ${extension.id}")
                
                // DO NOT call setAllowedInPrivateBrowsing(true) here. 
                // It is already granted by the PromptDelegate above.
                // Calling it here triggers a Redundant Restart which causes errors.
                
                extension.setActionDelegate(object : WebExtension.ActionDelegate {})
                extension.setMessageDelegate(object : WebExtension.MessageDelegate {}, "lumina")
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
