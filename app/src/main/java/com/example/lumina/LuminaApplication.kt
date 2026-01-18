package com.example.lumina

import android.app.Application
import com.example.lumina.core.ProfileManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LuminaApplication : Application() {

    @Inject
    lateinit var profileManager: ProfileManager

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.Main).launch {
            profileManager.createDefaultProfileIfNeeded()
        }
    }
}