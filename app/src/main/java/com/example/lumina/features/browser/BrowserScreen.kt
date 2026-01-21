package com.example.lumina.features.browser

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

/**
 * The main browser screen of the Lumina app.
 *
 * This screen provides a full-featured web browsing experience using Mozilla GeckoView.
 * It includes an address bar, navigation controls, security information, and supports
 * full-screen media playback.
 *
 * @param onClose Callback to be invoked when the browser screen should be closed.
 * @param browserViewModel The ViewModel that manages the browser's state and logic.
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
    val isSecure by browserViewModel.isSecure.collectAsState()
    val securityInfo by browserViewModel.securityInfo.collectAsState()
    val isAppLevelFullscreen by browserViewModel.isAppLevelFullscreen.collectAsState()

    val geckoView = remember { mutableStateOf<GeckoView?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var isTextFieldFocused by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val activity = context as Activity
    val window = activity.window
    val insetsController = remember { WindowCompat.getInsetsController(window, window.decorView) }

    var showWebView by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!showWebView) {
            delay(350)
            showWebView = true
        }
        browserViewModel.onAnimationFinished()
    }

    BackHandler(enabled = true) {
        if (isAppLevelFullscreen) {
            browserViewModel.exitFullScreen()
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            insetsController.show(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        } else {
            val didGoBack = browserViewModel.goBack()
            if (!didGoBack) {
                onClose()
            }
        }
    }

    DisposableEffect(isAppLevelFullscreen) {
        if (isAppLevelFullscreen) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            // Restore portrait orientation and system UI when leaving the browser screen or during recomposition
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Lifecycle observer to handle GeckoSession active state
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    browserViewModel.geckoSession.setActive(true)
                }
                Lifecycle.Event.ON_PAUSE -> {
                    browserViewModel.geckoSession.setActive(false)
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun String.formatForDisplay() = this.removePrefix("https://").removePrefix("http://").removePrefix("www.")

    LaunchedEffect(currentUrl, title) {
        if (!isTextFieldFocused) {
            searchQuery = if (title.isNotEmpty()) title else currentUrl.formatForDisplay()
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        if (!isLoading) {
            isRefreshing = false
        }
    }

    if (showSecurityDialog) {
        ConnectionInfoDialog(
            isSecure = isSecure,
            currentUrl = currentUrl,
            securityInfo = securityInfo,
            onDismiss = { showSecurityDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        if (!isAppLevelFullscreen) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onClose() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Close Browser",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .onFocusChanged {
                                isTextFieldFocused = it.isFocused
                                if (it.isFocused) {
                                    searchQuery = currentUrl
                                } else {
                                    searchQuery = if (title.isNotEmpty()) title else currentUrl.formatForDisplay()
                                }
                            },
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isTextFieldFocused && currentUrl.isNotEmpty()) {
                                IconButton(
                                    onClick = { showSecurityDialog = true },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSecure) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = if (isSecure) "Secure Connection" else "Unsecured Connection",
                                        modifier = Modifier.size(12.dp),
                                        tint = if (isSecure) Color(0xFFBB86FC) else Color.Red
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 13.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFFBB86FC)),
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
                                            color = Color.Gray
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
                                        tint = Color.Gray
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
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFFBB86FC)
                        )
                    }
                }
            }

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)) {
                if (isLoading) {
                    LinearProgressIndicator(
                        progress = { progress.toFloat() / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFBB86FC),
                        trackColor = Color.Transparent
                    )
                }
            }
        }

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
                        isNestedScrollingEnabled = true
                        geckoView.value = this
                    }
                },
                update = { view ->
                    // Ensure the view is always displaying the current session
                    if (view.session != browserViewModel.geckoSession) {
                        view.setSession(browserViewModel.geckoSession)
                    }
                },
                onRelease = { view ->
                    // Detach session when the view is destroyed/leaves composition
                    view.releaseSession()
                    geckoView.value = null
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * A dialog that displays connection and security information for the current web page.
 *
 * It shows whether the connection is secure, the URL, and provides details about the
 * SSL/TLS certificate if available.
 *
 * @param isSecure Whether the current connection is secure.
 * @param currentUrl The current URL of the page.
 * @param securityInfo The security information from GeckoView.
 * @param onDismiss Callback to be invoked when the dialog should be dismissed.
 */
@Composable
fun ConnectionInfoDialog(
    isSecure: Boolean,
    currentUrl: String,
    securityInfo: GeckoSession.ProgressDelegate.SecurityInformation?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Connection Information",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isSecure) "Connection is secure" else "Connection is not secure",
                    color = if (isSecure) Color(0xFFBB86FC) else Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                InfoItem("URL", currentUrl)

                if (isSecure) {
                    Text(
                        "Your information (for example, passwords or credit card numbers) is private when it is sent to this site.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Text(
                        "You should not enter any sensitive information on this site (for example, passwords or credit cards), because it could be stolen by attackers.",
                        fontSize = 12.sp,
                        color = Color.Red.copy(alpha = 0.8f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                val certificate = securityInfo?.certificate
                if (certificate != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Certificate Details", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                    InfoItem("Subject", certificate.subjectDN.name)
                    InfoItem("Issuer", certificate.issuerDN.name)
                    InfoItem("Valid From", certificate.notBefore.toString())
                    InfoItem("Valid Until", certificate.notAfter.toString())
                    InfoItem("Algorithm", certificate.sigAlgName)
                } else if (isSecure) {
                    Text("No detailed certificate information available", color = Color.Gray, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFFBB86FC))
            }
        },
        containerColor = Color(0xFF121212),
        textContentColor = Color.White,
        titleContentColor = Color.White
    )
}

/**
 * A helper composable to display a labeled piece of information.
 *
 * @param label The label for the information (e.g., "Subject").
 * @param value The value of the information.
 */
@Composable
fun InfoItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}
