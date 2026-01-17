package com.example.lumina.core.browser

import android.content.Context
import org.mozilla.geckoview.ContentBlocking
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoView

class GeckoViewManager(private val context: Context) {
    init {
        if (sRuntime == null) {
            // Fix: Remove 'set' prefix, use .safeBrowsing()
            val contentBlockingSettings = ContentBlocking.Settings.Builder()
                .safeBrowsing(ContentBlocking.SafeBrowsing.NONE)
                .build()

            val runtimeSettings = GeckoRuntimeSettings.Builder()
                .contentBlocking(contentBlockingSettings)
                .build()

            sRuntime = GeckoRuntime.create(context, runtimeSettings)
        }
    }

    fun createGeckoView(): GeckoView {
        return GeckoView(context)
    }

    companion object {
        private var sRuntime: GeckoRuntime? = null
        val runtime: GeckoRuntime
            get() = sRuntime ?: throw IllegalStateException("GeckoRuntime not initialized")
    }
}