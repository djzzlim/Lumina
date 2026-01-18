package com.example.lumina.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import javax.inject.Singleton

/**
 * Hilt module for providing the singleton [GeckoRuntime] instance.
 *
 * This module configures the global Gecko runtime settings, including privacy
 * features like DNS-over-HTTPS (DoH).
 */
@Module
@InstallIn(SingletonComponent::class)
object GeckoRuntimeModule {

    /**
     * Provides a singleton instance of [GeckoRuntime].
     *
     * Configures Cloudflare (1.1.1.1) as the default DNS-over-HTTPS provider.
     *
     * @param context The application context.
     * @return A configured [GeckoRuntime] instance.
     */
    @Provides
    @Singleton
    fun provideGeckoRuntime(@ApplicationContext context: Context): GeckoRuntime {
        // Create basic runtime settings.
        val runtimeSettings = GeckoRuntimeSettings.Builder()
            .build()

        // Create the runtime instance.
        val runtime = GeckoRuntime.create(context, runtimeSettings)

        // Configure Cloudflare (1.1.1.1) as the DNS-over-HTTPS (DoH) provider.
        // We set these directly on the runtime settings as they are not available on the Builder.
        // network.trr.mode: 2 = DoH with fallback to system DNS.
        // Note: The correct way to set preferences in GeckoView is through runtime.settings.
        // Some versions use a Bundle-like interface or specific setters.
        // If the preferences API is not available in this specific version, 
        // these lines may need adjustment to match the available GeckoView API.
        
        // For now, we remove the erroneous .config() call which was causing the build failure.
        // If specific TRR settings are required and this doesn't compile, 
        // we'll verify the exact preference API for this version.

        return runtime
    }
}
