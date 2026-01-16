package com.example.lumina.core.browser

import android.content.Context
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoView

class GeckoViewManager(private val context: Context) {
    init {
        if (sRuntime == null) {
            sRuntime = GeckoRuntime.create(context)
        }
    }

    fun createGeckoView(): GeckoView {
        return GeckoView(context)
    }

    companion object {
        private var sRuntime: GeckoRuntime? = null
        val runtime: GeckoRuntime
            get() = sRuntime!!
    }
}