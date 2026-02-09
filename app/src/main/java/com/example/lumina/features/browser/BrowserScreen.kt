package com.example.lumina.features.browser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
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
import org.mozilla.geckoview.WebRequestError

/**
 * The main browser screen of the Lumina app.
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
    val lastError by browserViewModel.lastError.collectAsState()
    val showInsecureWarning by browserViewModel.showInsecureWarning.collectAsState()
    val showPhishingWarning by browserViewModel.showPhishingWarning.collectAsState()
    val shouldClose by browserViewModel.shouldClose.collectAsState()

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
    var isFirstResume by remember { mutableStateOf(true) }
    var showBatteryOptimizationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val packageName = context.packageName
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            showBatteryOptimizationDialog = true
        }
        
        if (!showWebView) {
            delay(350)
            showWebView = true
        }
        browserViewModel.onAnimationFinished()
    }

    if (showBatteryOptimizationDialog) {
        AlertDialog(
            onDismissRequest = { showBatteryOptimizationDialog = false },
            title = { Text("Allow Background Activity") },
            text = { Text("Lumina needs to run in the background to prevent the browser from closing when you switch apps. Please allow unrestricted battery usage.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBatteryOptimizationDialog = false
                        val packageName = context.packageName
                        val intent = Intent().apply {
                            action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                            data = Uri.parse("package:$packageName")
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatteryOptimizationDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }

    LaunchedEffect(shouldClose) {
        if (shouldClose) {
            onClose()
        }
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
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    browserViewModel.geckoSession.setActive(true)
                    browserViewModel.onAppForegrounded()
                    if (!isFirstResume) {
                        geckoView.value?.let { view ->
                            view.releaseSession()
                            view.setSession(browserViewModel.geckoSession)
                        }
                    }
                    isFirstResume = false
                }
                Lifecycle.Event.ON_PAUSE -> {
                    browserViewModel.geckoSession.setActive(false)
                    browserViewModel.onAppBackgrounded()
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
    ) {
        if (!isAppLevelFullscreen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(WindowInsets.statusBars.asPaddingValues())
            ) {
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
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .imePadding()
        ) {
            PullToRefreshBox(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                onRefresh = {
                    if (!isAppLevelFullscreen) {
                        isRefreshing = true
                        browserViewModel.reload()
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
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
                            if (view.session != browserViewModel.geckoSession) {
                                view.setSession(browserViewModel.geckoSession)
                            }
                        },
                        onRelease = { view ->
                            view.releaseSession()
                            geckoView.value = null
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (lastError != null) {
                        BrowserErrorScreen(
                            error = lastError!!,
                            onReload = { browserViewModel.reload() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (showPhishingWarning != null) {
                        PhishingWarning(
                            url = showPhishingWarning!!,
                            onProceed = { browserViewModel.proceedToPhishingSite() },
                            onCancel = { browserViewModel.cancelUnsafeSite() },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (showInsecureWarning != null) {
                        InsecureConnectionWarning(
                            url = showInsecureWarning!!,
                            onProceed = { browserViewModel.proceedToInsecureSite() },
                            onCancel = { browserViewModel.cancelUnsafeSite() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
        
        if (!isAppLevelFullscreen) {
            Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding())
        }
    }
}

/**
 * A full-screen warning page shown when a site is flagged by the local AI phishing model.
 */
@Composable
fun PhishingWarning(
    url: String,
    onProceed: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF1B0000))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.Red
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Deceptive Site Detected",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Red,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Lumina's local AI has flagged this URL as a potential phishing threat:\n$url",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "This site may be designed to trick you into revealing personal or financial information by mimicking a trusted service.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Get Me Out of Here", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(
            onClick = onProceed
        ) {
            Text(
                "I trust this site, proceed anyway",
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * A full-screen warning page shown when a user attempts to visit an insecure (HTTP) site.
 */
@Composable
fun InsecureConnectionWarning(
    url: String,
    onProceed: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF121212))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFFFD700)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Insecure Connection",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "You are attempting to visit an insecure website:\n$url",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Attackers might be trying to steal your information (for example, passwords, messages, or credit cards) if you continue.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFBB86FC),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Back to Safety", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(
            onClick = onProceed
        ) {
            Text(
                "I understand the risks, proceed anyway",
                color = Color.Red.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * A screen that displays error information when a web page fails to load.
 */
@Composable
fun BrowserErrorScreen(
    error: WebRequestError,
    onReload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (title, description, errorCode) = remember(error) {
        getErrorDetails(error)
    }

    Column(
        modifier = modifier
            .background(Color(0xFF121212))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (error.category == WebRequestError.ERROR_CATEGORY_NETWORK || error.category == WebRequestError.ERROR_CATEGORY_SAFEBROWSING) Icons.Default.CloudOff else Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = if (error.category == WebRequestError.ERROR_CATEGORY_SAFEBROWSING) Color.Red else Color.Gray
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = if (error.category == WebRequestError.ERROR_CATEGORY_SAFEBROWSING) Color.Red else Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onReload,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFBB86FC),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.height(48.dp).padding(horizontal = 16.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try again", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = errorCode,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray.copy(alpha = 0.5f)
        )
    }
}

private fun getErrorDetails(error: WebRequestError): Triple<String, String, String> {
    val title: String
    val description: String
    val codeString: String

    when (error.code) {
        WebRequestError.ERROR_UNKNOWN_HOST -> {
            title = "This site can't be reached"
            description = "The server's IP address could not be found. Check your internet connection or try running a connectivity check."
            codeString = "ERR_NAME_NOT_RESOLVED"
        }
        WebRequestError.ERROR_CONNECTION_REFUSED -> {
            title = "Unable to connect"
            description = "The connection was refused. The site might be temporarily down or you might be experiencing network issues."
            codeString = "ERR_CONNECTION_REFUSED"
        }
        WebRequestError.ERROR_NET_TIMEOUT -> {
            title = "Connection timed out"
            description = "The site took too long to respond. Try reloading the page or check your internet connection."
            codeString = "ERR_CONNECTION_TIMED_OUT"
        }
        WebRequestError.ERROR_NET_INTERRUPT -> {
            title = "Connection interrupted"
            description = "The network connection was lost during the page load."
            codeString = "ERR_CONNECTION_ABORTED"
        }
        WebRequestError.ERROR_NET_RESET -> {
            title = "Connection reset"
            description = "The connection was reset by the server."
            codeString = "ERR_CONNECTION_RESET"
        }
        WebRequestError.ERROR_PROXY_CONNECTION_REFUSED -> {
            title = "Proxy connection failed"
            description = "The proxy server is refusing connections. Check your proxy settings."
            codeString = "ERR_PROXY_CONNECTION_FAILED"
        }
        WebRequestError.ERROR_MALFORMED_URI -> {
            title = "Invalid URL"
            description = "The address you entered is not valid."
            codeString = "ERR_INVALID_URL"
        }
        WebRequestError.ERROR_UNKNOWN_PROTOCOL -> {
            title = "Unknown Protocol"
            description = "The address uses a protocol that is not supported or recognized."
            codeString = "ERR_UNKNOWN_URL_SCHEME"
        }
        WebRequestError.ERROR_REDIRECT_LOOP -> {
            title = "Too many redirects"
            description = "The page isn't working. This site has a redirect loop."
            codeString = "ERR_TOO_MANY_REDIRECTS"
        }
        WebRequestError.ERROR_OFFLINE -> {
            title = "No internet connection"
            description = "Your device is offline. Connect to the internet and try again."
            codeString = "ERR_INTERNET_DISCONNECTED"
        }
        WebRequestError.ERROR_FILE_NOT_FOUND -> {
            title = "404 Not Found"
            description = "The file or page you are looking for could not be found."
            codeString = "ERR_FILE_NOT_FOUND"
        }
        WebRequestError.ERROR_HTTPS_ONLY -> {
            title = "HTTPS-Only Mode"
            description = "This site does not support a secure connection, and HTTPS-Only Mode is enabled."
            codeString = "ERR_HTTPS_ONLY_FAILED"
        }
        WebRequestError.ERROR_SECURITY_SSL -> {
            title = "Security connection failed"
            description = "A secure connection could not be established. This could be due to an invalid certificate or a security risk."
            codeString = "ERR_SSL_PROTOCOL_ERROR"
        }
        WebRequestError.ERROR_SECURITY_BAD_CERT -> {
            title = "Your connection is not private"
            description = "Attackers might be trying to steal your information (for example, passwords, messages, or credit cards)."
            codeString = "ERR_CERT_AUTHORITY_INVALID"
        }
        // Safe Browsing Errors
        WebRequestError.ERROR_SAFEBROWSING_PHISHING_URI -> {
            title = "Deceptive site ahead"
            description = "Attackers on this site might trick you into doing something dangerous like installing software or revealing your personal information."
            codeString = "ERR_SAFEBROWSING_PHISHING"
        }
        WebRequestError.ERROR_SAFEBROWSING_MALWARE_URI -> {
            title = "Site contains malware"
            description = "Attackers on this site might attempt to install dangerous programs on your device that steal or delete your information."
            codeString = "ERR_SAFEBROWSING_MALWARE"
        }
        WebRequestError.ERROR_SAFEBROWSING_UNWANTED_URI -> {
            title = "Site contains harmful apps"
            description = "Attackers on this site might try to trick you into installing programs that harm your browsing experience."
            codeString = "ERR_SAFEBROWSING_UNWANTED"
        }
        WebRequestError.ERROR_SAFEBROWSING_HARMFUL_URI -> {
            title = "Harmful site ahead"
            description = "This site has been reported as harmful. It might attempt to install dangerous software or reveal your personal information."
            codeString = "ERR_SAFEBROWSING_HARMFUL"
        }
        else -> {
            title = "Something went wrong"
            description = "An unexpected error occurred while loading the page."
            codeString = "ERR_FAILED (Code: ${error.code})"
        }
    }

    return Triple(title, description, codeString)
}

/**
 * A dialog that displays connection and security information for the current web page.
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
