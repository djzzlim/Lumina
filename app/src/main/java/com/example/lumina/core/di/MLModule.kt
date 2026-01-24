package com.example.lumina.core.di

import android.content.Context
import com.example.lumina.core.ml.PhishingDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MLModule {

    @Provides
    @Singleton
    fun providePhishingDetector(@ApplicationContext context: Context): PhishingDetector {
        return PhishingDetector(context)
    }
}
