package com.example.lumina.core.browser

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.mozilla.geckoview.GeckoView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeckoViewManager @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) {
    fun createGeckoView(activityContext: Context): GeckoView {
        return GeckoView(activityContext)
    }
}