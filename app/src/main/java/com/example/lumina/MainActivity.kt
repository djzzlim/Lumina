package com.example.lumina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.lumina.navigation.AppNavigation
import com.example.lumina.ui.theme.LuminaTheme
import dagger.hilt.android.AndroidEntryPoint
import org.mozilla.geckoview.GeckoRuntime
import javax.inject.Inject

/**
 * Main activity for the Lumina application.
 *
 * This activity serves as the entry point for the UI, setting up the Compose theme
 * and the main navigation graph. It is annotated with [AndroidEntryPoint] to enable
 * Hilt dependency injection.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var geckoRuntime: GeckoRuntime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LuminaTheme {
                // The MainActivity is now extremely clean.
                // Its only job is to set the theme and call the navigation graph.
                AppNavigation()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure the GeckoRuntime is shut down when the activity is destroyed
        // to free up resources and stop background processes.
        if (isFinishing) {
            geckoRuntime.shutdown()
        }
    }
}
