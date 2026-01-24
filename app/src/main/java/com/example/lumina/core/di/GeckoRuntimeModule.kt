package com.example.lumina.core.di

import android.content.Context
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
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import javax.inject.Singleton

/**
 * Hilt module for providing the singleton [GeckoRuntime] instance.
 *
 * This module configures the global Gecko runtime settings, including privacy
 * features like DNS-over-HTTPS (DoH) and Google Safe Browsing.
 */
@Module
@InstallIn(SingletonComponent::class)
object GeckoRuntimeModule {

    /**
     * Provides a singleton instance of [GeckoRuntime].
     *
     * @param context The application context.
     * @param appPreferences User preferences for DNS and isolation.
     * @return A configured [GeckoRuntime] instance.
     */
    @OptIn(ExperimentalGeckoViewApi::class)
    @Provides
    @Singleton
    fun provideGeckoRuntime(
        @ApplicationContext context: Context,
        appPreferences: AppPreferences
    ): GeckoRuntime {
        val dnsProvider = runBlocking { appPreferences.dnsProviderFlow.first() }
        val isolationStrategy = runBlocking { appPreferences.isolationStrategyFlow.first() }

        // Initialize with default Safe Browsing enabled at the Runtime level.
        // Whether it's actually used per-session is now controlled in BrowserViewModel.
        val contentBlocking = ContentBlocking.Settings.Builder()
            .safeBrowsing(ContentBlocking.SafeBrowsing.DEFAULT)
            .enhancedTrackingProtectionLevel(ContentBlocking.EtpLevel.STRICT)
            .build()

        val runtimeSettings = GeckoRuntimeSettings.Builder()
            .aboutConfigEnabled(true)
            .fissionEnabled(true) // Required for site isolation
            .trustedRecursiveResolverUri(dnsProvider.uri)
            .trustedRecursiveResolverMode(dnsProvider.mode)
            .allowInsecureConnections(GeckoRuntimeSettings.ALLOW_ALL)
            .contentBlocking(contentBlocking)
            .build()
            .setWebContentIsolationStrategy(isolationStrategy)

        val runtime = GeckoRuntime.create(context, runtimeSettings)

        // Set the Google Safe Browsing API Key from BuildConfig
        GeckoPreferenceController.setGeckoPref(
            "browser.safebrowsing.key.google",
            BuildConfig.SAFE_BROWSING_KEY,
            GeckoPreferenceController.PREF_BRANCH_USER
        )

        return runtime
    }
}
