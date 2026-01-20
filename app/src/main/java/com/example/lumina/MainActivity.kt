package com.example.lumina

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.lumina.core.ProfileManager
import com.example.lumina.navigation.AppNavigation
import com.example.lumina.ui.theme.LuminaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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

    @Inject
    lateinit var profileManager: ProfileManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prevents screenshots, screen recordings, and hides content in the Recents (Multitasking) screen.
        // This is a key forensic protection measure.
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        
        // Ensure the default profile exists and is set correctly
        lifecycleScope.launch {
            profileManager.createDefaultProfileIfNeeded()
        }
        
        // Redundancy: Clear data on start to ensure a clean slate
        geckoRuntime.storageController.clearData(StorageController.ClearFlags.ALL)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            LuminaTheme {
                FixedTextToolbar {
                    AppNavigation()
                }
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
        }
    }
}

/**
 * A wrapper that provides a "Safe" TextToolbar to prevent crashes on Xiaomi devices.
 * Xiaomi's MIUI/HyperOS custom context menu implementation often throws ClassCastException
 * because it expects a standard Android TextView, but Compose uses a custom view.
 */
@Composable
fun FixedTextToolbar(content: @Composable () -> Unit) {
    val originalToolbar = LocalTextToolbar.current
    
    val customToolbar = remember(originalToolbar) {
        object : TextToolbar {
            override val status: TextToolbarStatus get() = originalToolbar.status
            override fun hide() = originalToolbar.hide()

            override fun showMenu(
                rect: Rect,
                onCopyRequested: (() -> Unit)?,
                onPasteRequested: (() -> Unit)?,
                onCutRequested: (() -> Unit)?,
                onSelectAllRequested: (() -> Unit)?
            ) {
                try {
                    originalToolbar.showMenu(
                        rect, 
                        onCopyRequested, 
                        onPasteRequested, 
                        onCutRequested, 
                        onSelectAllRequested
                    )
                } catch (_: Exception) {
                    // This catches the MIUI/HyperOS ClassCastException.
                    // The menu might not show up on affected devices, but the app won't crash.
                }
            }
        }
    }

    CompositionLocalProvider(LocalTextToolbar provides customToolbar) {
        content()
    }
}
