package com.example.lumina.features.browser

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lumina.core.browser.GeckoViewManager
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    onClose: () -> Unit,
    browserViewModel: BrowserViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val geckoViewManager = remember { GeckoViewManager(context) }
    var geckoView: GeckoView? by remember { mutableStateOf(null) }

    val luminaInfo by browserViewModel.luminaInfo.collectAsState()

    val geckoSession = browserViewModel.geckoSession


    var displayUrl by remember { mutableStateOf("") }

    val navigationDelegate = object : GeckoSession.NavigationDelegate {
        override fun onLocationChange(
            session: GeckoSession,
            url: String?,
            permissions: List<GeckoSession.PermissionDelegate.ContentPermission>,
            isRedirection: Boolean
        ) {
            // We don't update displayUrl here because we want to show the lumina name
            // not the current browser URL.
        }
    }

    LaunchedEffect(luminaInfo) {
        luminaInfo?.let {
            browserViewModel.applySettings(it)
            geckoSession.navigationDelegate = navigationDelegate
            geckoSession.open(GeckoViewManager.runtime)
            geckoSession.load(GeckoSession.Loader().uri(it.url))
            displayUrl = it.name // Set the display name to lumina name
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = displayUrl) },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close Browser")
                }
            }
        )
        AndroidView(factory = { context ->
            val view = geckoViewManager.createGeckoView()
            view.layoutParams = ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            geckoView = view
            view.setSession(geckoSession)
            view
        })
    }
}

