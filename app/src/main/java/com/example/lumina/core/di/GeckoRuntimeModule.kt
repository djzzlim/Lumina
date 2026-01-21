package com.example.lumina.core.di

import android.content.Context
import com.example.lumina.core.AppPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
    fun provideGeckoRuntime(
        @ApplicationContext context: Context,
        appPreferences: AppPreferences
    ): GeckoRuntime {
        val dnsProvider = runBlocking { appPreferences.dnsProviderFlow.first() }

        val runtimeSettings = GeckoRuntimeSettings.Builder()
            .aboutConfigEnabled(true)
            .trustedRecursiveResolverUri(dnsProvider.uri)
            .trustedRecursiveResolverMode(dnsProvider.mode)
            .build()

        val runtime = GeckoRuntime.create(context, runtimeSettings)

        return runtime
    }
}
