package com.example.lumina.features.browser

import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    onClose: () -> Unit,
    browserViewModel: BrowserViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val geckoRuntime = browserViewModel.geckoRuntime
    val luminaInfo by browserViewModel.luminaInfo.collectAsState()

    var displayUrl by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val navigationDelegate = remember {
        object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                permissions: List<GeckoSession.PermissionDelegate.ContentPermission>,
                isRedirection: Boolean
            ) {
                displayUrl = url ?: ""
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search or enter address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close Browser")
                }
            },
            actions = {
                IconButton(onClick = { browserViewModel.onSearchQuery(searchQuery) }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        )

        // geckoRuntime is a Singleton provided by Hilt, so it should be available.
        val geckoSession = browserViewModel.geckoSession
        LaunchedEffect(geckoSession) {
            geckoSession.navigationDelegate = navigationDelegate
        }

        AndroidView(
            factory = { factoryContext ->
                GeckoView(factoryContext).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setSession(geckoSession)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
