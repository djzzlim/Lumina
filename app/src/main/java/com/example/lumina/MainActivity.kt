package com.example.lumina

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.core.view.WindowCompat
import com.example.lumina.core.ProfileManager
import com.example.lumina.navigation.AppNavigation
import com.example.lumina.ui.theme.LuminaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.StorageController
import java.util.regex.Pattern
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

    private val urlState = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prevents screenshots, screen recordings, and hides content in the Recents (Multitasking) screen.
        // This is a key forensic protection measure.
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        
        // Only clear runtime storage on a fresh cold-start, not on activity recreation (e.g. rotation)
        if (savedInstanceState == null) {
            // Cold start: clear any leftover runtime data for a clean slate
            geckoRuntime.storageController.clearData(StorageController.ClearFlags.ALL)
        }
        
        handleIntent(intent)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            val startUrl by urlState.collectAsState()
            LuminaTheme {
                FixedTextToolbar {
                    AppNavigation(
                        startUrl = startUrl,
                        onUrlHandled = { urlState.value = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                urlState.value = intent.dataString
            }
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (sharedText != null) {
                        urlState.value = extractUrl(sharedText) ?: sharedText
                    }
                }
            }
        }
    }

    /**
     * Extracts the first HTTP/HTTPS URL from a string using regex.
     */
    private fun extractUrl(text: String): String? {
        val urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}\\w+(:\\d+)?(\\/\\S*)?)",
            Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
        )
        val matcher = urlPattern.matcher(text)
        if (matcher.find()) {
            val res = text.substring(matcher.start(1), matcher.end())
            return if (!res.startsWith("http")) "https://$res" else res
        }
        return null
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
