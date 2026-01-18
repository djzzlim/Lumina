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

@Module
@InstallIn(SingletonComponent::class)
object GeckoRuntimeModule {

    @Provides
    @Singleton
    fun provideGeckoRuntime(@ApplicationContext context: Context): GeckoRuntime {
        // Create GeckoRuntimeSettings here if needed, but for a global runtime, keep it minimal.
        val runtimeSettings = GeckoRuntimeSettings.Builder()
            // Add global runtime settings here if necessary
            .build()
        return GeckoRuntime.create(context, runtimeSettings)
    }
}
