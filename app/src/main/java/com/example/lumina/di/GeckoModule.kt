package com.example.lumina.di

// This module was providing a duplicate GeckoRuntime binding and has been disabled 
// in favor of GeckoRuntimeModule in com.example.lumina.core.di
/*
import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.mozilla.geckoview.GeckoRuntime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GeckoModule {

    @Singleton
    @Provides
    fun provideGeckoRuntime(application: Application): GeckoRuntime {
        return GeckoRuntime.create(application)
    }
}
*/
