package com.example.lumina

import android.app.Application
import com.example.lumina.core.ProfileManager
import com.example.lumina.core.di.GeckoRuntimeModule
import com.example.lumina.core.tor.TorManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.sqlcipher.database.SQLiteDatabase
import org.mozilla.geckoview.StorageController
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

    @Inject
    lateinit var torManager: TorManager

    override fun onCreate() {
        super.onCreate()

        // Initialize SQLCipher libraries
        SQLiteDatabase.loadLibs(this)

        // Ensure a default profile exists on application start.
        CoroutineScope(Dispatchers.Main).launch {
            profileManager.createDefaultProfileIfNeeded()
        }
    }

    override fun onTerminate() {
        // 1. Clear GeckoRuntime Data and Shutdown
        GeckoRuntimeModule.getRuntime()?.let { runtime ->
            runtime.storageController.clearData(StorageController.ClearFlags.ALL)
            runtime.shutdown()
        }

        // 2. Stop Tor
        torManager.stopTor()

        // 3. Clear System Clipboard to prevent forensic leaks
        try {
            val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clipData = android.content.ClipData.newPlainText("cleared", "")
            clipboard.setPrimaryClip(clipData)
        } catch (e: Exception) {
            // Ignore clipboard errors during shutdown
        }

        super.onTerminate()
        
        // 4. Force Process Exit for a hard purge of memory
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}
