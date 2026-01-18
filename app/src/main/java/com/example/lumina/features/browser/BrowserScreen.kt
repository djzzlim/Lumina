package com.example.lumina.features.browser

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import org.mozilla.geckoview.GeckoView

/**
 * Composable representing the browser screen with a modern UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    onClose: () -> Unit,
    browserViewModel: BrowserViewModel = hiltViewModel(),
) {
    val currentUrl by browserViewModel.currentUrl.collectAsState()
    val title by browserViewModel.title.collectAsState()
    val progress by browserViewModel.progress.collectAsState()
    val isLoading by browserViewModel.isLoading.collectAsState()
    val isAtTop by browserViewModel.isAtTop.collectAsState()
    val isSecure by browserViewModel.isSecure.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isTextFieldFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Helper to format URL for display
    fun String.formatForDisplay() = this.removePrefix("https://").removePrefix("http://").removePrefix("www.")

    // Sync searchQuery with current state when not interacting
    LaunchedEffect(currentUrl, title) {
        if (!isTextFieldFocused) {
            searchQuery = if (title.isNotEmpty()) title else currentUrl.formatForDisplay()
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    // Reset the refreshing state when loading finishes
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            isRefreshing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        // Modern Minimalist Address Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, 
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .onFocusChanged { 
                            isTextFieldFocused = it.isFocused 
                            if (it.isFocused) {
                                searchQuery = currentUrl // Show full URL for editing
                            } else {
                                searchQuery = if (title.isNotEmpty()) title else currentUrl.formatForDisplay()
                            }
                        },
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSecure && !isTextFieldFocused) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Secure",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp // Smaller, modern font
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = {
                                browserViewModel.onSearchQuery(searchQuery)
                                focusManager.clearFocus()
                            }),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        "Search or enter address",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                                innerTextField()
                            }
                        )

                        if (searchQuery.isNotEmpty() && isTextFieldFocused) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = { 
                        if (isTextFieldFocused) {
                            browserViewModel.onSearchQuery(searchQuery)
                            focusManager.clearFocus()
                        } else {
                            browserViewModel.reload()
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        if (isTextFieldFocused) Icons.Default.Search else Icons.Default.Refresh,
                        contentDescription = if (isTextFieldFocused) "Search" else "Reload",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Integrated Slim Progress Indicator
        Box(modifier = Modifier.fillMaxWidth().height(1.5.dp)) {
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { progress.toFloat() / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }
        }

        // Pull-to-Refresh Content
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = { 
                isRefreshing = true
                browserViewModel.reload() 
            },
            modifier = Modifier.weight(1f)
        ) {
            AndroidView(
                factory = { factoryContext ->
                    GeckoView(factoryContext).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setSession(browserViewModel.geckoSession)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
