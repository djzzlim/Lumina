package com.example.lumina.core.browser

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.mozilla.geckoview.GeckoView
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager class responsible for handling the lifecycle and instantiation of [GeckoView].
 *
 * This class provides a centralized way to create and manage GeckoView instances,
 * which are used for rendering web content within the application.
 *
 * @property applicationContext The application-level context.
 */
@Singleton
class GeckoViewManager @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) {
    /**
     * Creates a new instance of [GeckoView] using the provided activity context.
     *
     * @param activityContext The context of the activity where the GeckoView will be hosted.
     * @return A new [GeckoView] instance.
     */
    fun createGeckoView(activityContext: Context): GeckoView {
        return GeckoView(activityContext)
    }
}
