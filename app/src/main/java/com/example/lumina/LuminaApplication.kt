package com.example.lumina

import android.app.Application
import com.example.lumina.core.ProfileManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The main [Application] class for the Lumina project.
 *
 * This class is annotated with [HiltAndroidApp] to trigger Hilt's code generation,
 * which is necessary for dependency injection throughout the application.
 */
@HiltAndroidApp
class LuminaApplication : Application() {

    @Inject
    lateinit var profileManager: ProfileManager

    override fun onCreate() {
        super.onCreate()
        // Ensure a default profile exists on application start.
        CoroutineScope(Dispatchers.Main).launch {
            profileManager.createDefaultProfileIfNeeded()
        }
    }
}