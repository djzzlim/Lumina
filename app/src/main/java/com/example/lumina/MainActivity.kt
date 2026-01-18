package com.example.lumina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.lumina.navigation.AppNavigation
import com.example.lumina.ui.theme.LuminaTheme
import dagger.hilt.android.AndroidEntryPoint
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.StorageController
import javax.inject.Inject

/**
 * Main activity for the Lumina application.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var geckoRuntime: GeckoRuntime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
            // We trigger this, and although it's async, we follow with shutdown.
            geckoRuntime.storageController.clearData(StorageController.ClearFlags.ALL)
            
            // Shut down the runtime
            geckoRuntime.shutdown()
        }
    }
}
