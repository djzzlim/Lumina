package com.example.lumina.core.di

import android.content.Context
import com.example.lumina.core.ml.PhishingDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides machine learning related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object MLModule {

    /**
     * Provides a singleton instance of [PhishingDetector].
     *
     * @param context The application context.
     * @return A [PhishingDetector] instance.
     */
    @Provides
    @Singleton
    fun providePhishingDetector(@ApplicationContext context: Context): PhishingDetector {
        return PhishingDetector(context)
    }
}
