package com.example.lumina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.lumina.core.ProfileManager
import com.example.lumina.navigation.AppNavigation
import com.example.lumina.ui.theme.LuminaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.StorageController
import javax.inject.Inject
import kotlin.system.exitProcess

/**
 * Main activity for the Lumina application.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var geckoRuntime: GeckoRuntime

    @Inject
    lateinit var profileManager: ProfileManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure the default profile exists and is set correctly
        lifecycleScope.launch {
            profileManager.createDefaultProfileIfNeeded()
        }
        
        // Redundancy: Clear data on start to ensure a clean slate
        geckoRuntime.storageController.clearData(StorageController.ClearFlags.ALL)
        
        enableEdgeToEdge()
        setContent {
            LuminaTheme {
                AppNavigation()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            // Clear history, cookies, and cache upon exit
            geckoRuntime.storageController.clearData(StorageController.ClearFlags.ALL)
            
            // Shut down the runtime
            geckoRuntime.shutdown()
            
            // Completely terminate the app process
            exitProcess(0)
        }
    }
}
