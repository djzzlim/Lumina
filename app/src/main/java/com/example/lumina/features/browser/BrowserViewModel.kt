package com.example.lumina.features.browser

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumina.core.AppPreferences
import com.example.lumina.core.LuminaRepository
import com.example.lumina.core.ProfileManager
import com.example.lumina.core.database.LuminaInfo
import com.example.lumina.core.ml.PhishingDetector
import com.example.lumina.core.tor.TorManager
import com.example.lumina.navigation.ScreenRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.ExperimentalGeckoViewApi
import org.mozilla.geckoview.GeckoPreferenceController
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.StorageController
import org.mozilla.geckoview.WebRequestError
import javax.inject.Inject

/**
 * ViewModel for the [BrowserScreen].
 */
@HiltViewModel
class BrowserViewModel @androidx.annotation.OptIn(ExperimentalGeckoViewApi::class)
@Inject constructor(
    private val luminaRepository: LuminaRepository,
    @Suppress("UNUSED_PARAMETER") private val profileManager: ProfileManager,
    private val globalGeckoRuntime: GeckoRuntime,
    private val appPreferences: AppPreferences,
    private val phishingDetector: PhishingDetector,
    private val torManager: TorManager,
    @param:ApplicationContext private val applicationContext: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val luminaId: Long = savedStateHandle.get<Long>(ScreenRoutes.BROWSER_ID_ARG)!!
    private val sessionContextId = "lumina_session_$luminaId"

    val luminaInfo: StateFlow<LuminaInfo?> = luminaRepository.getLuminaById(luminaId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _geckoSession = GeckoSession(
        GeckoSessionSettings.Builder()
            .usePrivateMode(true)
            .contextId(sessionContextId)
            .build()
    )

    val geckoSession: GeckoSession get() = _geckoSession

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentUrl = MutableStateFlow(savedStateHandle.get<String>("persisted_url") ?: "")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _isAtTop = MutableStateFlow(true)
    val isAtTop: StateFlow<Boolean> = _isAtTop.asStateFlow()

    private val _isSecure = MutableStateFlow(false)
    val isSecure: StateFlow<Boolean> = _isSecure.asStateFlow()

    private val _securityInfo = MutableStateFlow<GeckoSession.ProgressDelegate.SecurityInformation?>(null)
    val securityInfo: StateFlow<GeckoSession.ProgressDelegate.SecurityInformation?> = _securityInfo.asStateFlow()

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private val _isAppLevelFullscreen = MutableStateFlow(false)
    val isAppLevelFullscreen: StateFlow<Boolean> = _isAppLevelFullscreen.asStateFlow()

    private val _lastError = MutableStateFlow<WebRequestError?>(null)
    val lastError: StateFlow<WebRequestError?> = _lastError.asStateFlow()

    private val _showInsecureWarning = MutableStateFlow<String?>(null)
    val showInsecureWarning: StateFlow<String?> = _showInsecureWarning.asStateFlow()

    private val _showPhishingWarning = MutableStateFlow<String?>(null)
    val showPhishingWarning: StateFlow<String?> = _showPhishingWarning.asStateFlow()

    private val _shouldClose = MutableStateFlow(false)
    val shouldClose: StateFlow<Boolean> = _shouldClose.asStateFlow()

    val torEnabled: StateFlow<Boolean> = appPreferences.torEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isTorRunning: StateFlow<Boolean> = torManager.isTorRunning
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val torBootstrappingProgress: StateFlow<Int> = torManager.bootstrappingProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val torLogs: StateFlow<String> = torManager.torLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _isAnimationFinished = MutableStateFlow(false)
    private var isInitialized = false
    private var isGoingBack = false
    
    private var lastAttemptedUrl: String? = null
    private var lastCommittedUrl: String = savedStateHandle.get<String>("persisted_url") ?: ""
    private var lastCommittedTitle: String = ""
    private var wasHttpsForced = false
    private val allowedInsecureHosts = mutableSetOf<String>()
    private val allowedPhishingHosts = mutableSetOf<String>()
    
    private var autoCloseJob: Job? = null

    private val searchEngine = appPreferences.searchEngineFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.example.lumina.core.SearchEngine.Google)

    init {
        setupDelegates()

        // Apply global settings dynamically
        viewModelScope.launch {
            appPreferences.dnsProviderFlow.collect { dnsProvider ->
                globalGeckoRuntime.settings.setTrustedRecursiveResolverUri(dnsProvider.uri)
                globalGeckoRuntime.settings.setTrustedRecursiveResolverMode(dnsProvider.mode)
            }
        }

        viewModelScope.launch {
            appPreferences.safeBrowsingEnabledFlow.collect { enabled ->
                Log.d("BrowserViewModel", "Setting Google Safe Browsing to: $enabled")
                GeckoPreferenceController.setGeckoPref("browser.safebrowsing.malware.enabled", enabled, GeckoPreferenceController.PREF_BRANCH_USER)
                GeckoPreferenceController.setGeckoPref("browser.safebrowsing.phishing.enabled", enabled, GeckoPreferenceController.PREF_BRANCH_USER)
                GeckoPreferenceController.setGeckoPref("browser.safebrowsing.downloads.enabled", enabled, GeckoPreferenceController.PREF_BRANCH_USER)
            }
        }

        // Synchronize Tor timezone with the browser
        viewModelScope.launch {
            combine(torManager.exitNodeTimezone, torManager.exitNodeIp) { tz, ip ->
                if (tz != null && ip != null) {
                    Log.d("BrowserViewModel", "Tor exit node detected: $ip in $tz")
                }
            }
        }

        // Dynamically toggle Tor Proxy based on preference
        viewModelScope.launch {
            appPreferences.torEnabledFlow.collect { enabled ->
                Log.d("BrowserViewModel", "Setting Tor Proxy to: $enabled")
                if (enabled) {
                    GeckoPreferenceController.setGeckoPref("network.proxy.type", 1, GeckoPreferenceController.PREF_BRANCH_USER)
                    GeckoPreferenceController.setGeckoPref("network.proxy.socks", "127.0.0.1", GeckoPreferenceController.PREF_BRANCH_USER)
                    GeckoPreferenceController.setGeckoPref("network.proxy.socks_port", 9050, GeckoPreferenceController.PREF_BRANCH_USER)
                    GeckoPreferenceController.setGeckoPref("network.proxy.socks_remote_dns", true, GeckoPreferenceController.PREF_BRANCH_USER)
                    GeckoPreferenceController.setGeckoPref("network.proxy.socks_version", 5, GeckoPreferenceController.PREF_BRANCH_USER)
                } else {
                    GeckoPreferenceController.setGeckoPref("network.proxy.type", 0, GeckoPreferenceController.PREF_BRANCH_USER)
                }
            }
        }

        // Dynamically toggle Tor Profile settings
        viewModelScope.launch {
            appPreferences.torProfileFlow.collect { profile ->
                Log.d("BrowserViewModel", "Applying Tor Profile: $profile")
                when (profile) {
                    "Safer" -> {
                        GeckoPreferenceController.setGeckoPref("javascript.enabled", true, GeckoPreferenceController.PREF_BRANCH_USER)
                        GeckoPreferenceController.setGeckoPref("svg.disabled", true, GeckoPreferenceController.PREF_BRANCH_USER)
                    }
                    "Safest" -> {
                        GeckoPreferenceController.setGeckoPref("javascript.enabled", false, GeckoPreferenceController.PREF_BRANCH_USER)
                        GeckoPreferenceController.setGeckoPref("svg.disabled", true, GeckoPreferenceController.PREF_BRANCH_USER)
                    }
                    else -> { // Standard
                        GeckoPreferenceController.setGeckoPref("javascript.enabled", true, GeckoPreferenceController.PREF_BRANCH_USER)
                        GeckoPreferenceController.setGeckoPref("svg.disabled", false, GeckoPreferenceController.PREF_BRANCH_USER)
                    }
                }
            }
        }

        viewModelScope.launch {
            combine(luminaInfo.filterNotNull(), _isAnimationFinished) { info, finished ->
                if (finished) info else null
            }.filterNotNull().collect { info ->
                if (!isInitialized) {
                    if (!_geckoSession.isOpen) {
                        _geckoSession.open(globalGeckoRuntime)
                        _geckoSession.setActive(true)
                    }
                    applySettings(info)
                    
                    // Recover history state from SavedStateHandle if available
                    val sessionState = savedStateHandle.get<GeckoSession.SessionState>("persisted_session_state")
                    if (sessionState != null) {
                        Log.d("BrowserViewModel", "Initializing session from restored history state")
                        _geckoSession.restoreState(sessionState)
                    } else {
                        val initialUrl = if (lastCommittedUrl.isNotEmpty()) lastCommittedUrl else info.url
                        loadUrl(initialUrl)
                    }
                    isInitialized = true
                } else {
                    applySettings(info)
                }
            }
        }
    }

    private fun resetSecurityState() {
        _isSecure.value = false
        _securityInfo.value = null
    }

    fun onAnimationFinished() {
        _isAnimationFinished.value = true
    }

    fun onAppBackgrounded() {
        autoCloseJob?.cancel()
        autoCloseJob = viewModelScope.launch {
            val timeout = appPreferences.autoCloseTimeoutFlow.first()
            if (timeout.minutes > 0) {
                delay(timeout.minutes * 60 * 1000)
                if (_geckoSession.isOpen) _geckoSession.close()
                globalGeckoRuntime.storageController.clearDataForSessionContext(sessionContextId)
                globalGeckoRuntime.storageController.clearData(StorageController.ClearFlags.ALL)
                _shouldClose.value = true
            }
        }
    }

    fun onAppForegrounded() {
        if (!isInitialized) return
        autoCloseJob?.cancel()
        autoCloseJob = null

        val wasSessionClosed = !geckoSession.isOpen

        if (wasSessionClosed) {
            Log.d("BrowserViewModel", "GeckoSession not open on foreground. Re-initializing.")
            _geckoSession.open(globalGeckoRuntime)
        }
        _geckoSession.setActive(true)

        // If session was closed or UI is blank, force recovery from persisted state
        if (wasSessionClosed || _currentUrl.value.isEmpty()) {
            restoreSession()
        }
    }

    private fun restoreSession() {
        viewModelScope.launch {
            val sessionState = savedStateHandle.get<GeckoSession.SessionState>("persisted_session_state")
            if (sessionState != null) {
                Log.d("BrowserViewModel", "Restoring history state")
                _geckoSession.restoreState(sessionState)
            } else {
                val persistedUrl = savedStateHandle.get<String>("persisted_url")
                val urlToLoad = if (!persistedUrl.isNullOrEmpty()) persistedUrl 
                               else luminaInfo.value?.url ?: "about:blank"
                Log.d("BrowserViewModel", "Reloading URL (no history state found): $urlToLoad")
                loadUrl(urlToLoad)
            }
        }
    }

    private fun setupDelegates() {
        _geckoSession.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onProgressChange(session: GeckoSession, progress: Int) {
                _progress.value = progress
                _isLoading.value = progress < 100
                if (progress == 100 && isGoingBack) isGoingBack = false
            }

            override fun onSecurityChange(session: GeckoSession, securityInfo: GeckoSession.ProgressDelegate.SecurityInformation) {
                _isSecure.value = securityInfo.isSecure
                _securityInfo.value = securityInfo
            }

            override fun onSessionStateChange(session: GeckoSession, sessionState: GeckoSession.SessionState) {
                // SessionState is directly Parcelable in modern GeckoView
                savedStateHandle["persisted_session_state"] = sessionState
            }
        }

        _geckoSession.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                _canGoBack.value = canGoBack
            }

            override fun onLocationChange(session: GeckoSession, url: String?, permissions: List<GeckoSession.PermissionDelegate.ContentPermission>, hasUserGesture: Boolean) {
                if (!url.isNullOrEmpty() && url != "about:blank") {
                    lastCommittedUrl = url
                    _currentUrl.value = url
                    savedStateHandle["persisted_url"] = url 
                    
                    // Optimistic security check to prevent incorrect "not safe" warning on back navigation
                    if (url.startsWith("https://")) {
                        _isSecure.value = true
                    } else if (url.startsWith("http://")) {
                        _isSecure.value = false
                    }
                    
                    if (url.startsWith("https")) wasHttpsForced = false
                }
            }

            override fun onLoadRequest(session: GeckoSession, request: GeckoSession.NavigationDelegate.LoadRequest): GeckoResult<AllowOrDeny> {
                if (request.target == GeckoSession.NavigationDelegate.TARGET_WINDOW_NEW) {
                    loadUrl(request.uri)
                    return GeckoResult.fromValue(AllowOrDeny.DENY)
                }

                val host = try { android.net.Uri.parse(request.uri).host ?: "" } catch (e: Exception) { "" }

                if (!request.isRedirect) {
                    lastAttemptedUrl = request.uri
                    _currentUrl.value = request.uri
                    _lastError.value = null
                    _showInsecureWarning.value = null
                    _showPhishingWarning.value = null
                    _title.value = "" 
                    resetSecurityState()
                }

                val result = GeckoResult<AllowOrDeny>()
                viewModelScope.launch {
                    val isLocalMLEnabled = appPreferences.localPhishingModelEnabledFlow.first()
                    
                    if (isLocalMLEnabled && !allowedPhishingHosts.contains(host)) {
                        val isPhishing = phishingDetector.predict(request.uri)
                        if (isPhishing) {
                            _currentUrl.value = request.uri
                            _showPhishingWarning.value = request.uri
                            resetSecurityState()
                            _geckoSession.stop()
                            result.complete(AllowOrDeny.DENY)
                            return@launch
                        }
                    }

                    if (request.uri.startsWith("http://") && !request.isRedirect) {
                        if (!allowedInsecureHosts.contains(host)) {
                            _currentUrl.value = request.uri
                            _showInsecureWarning.value = request.uri
                            resetSecurityState()
                            _geckoSession.stop()
                            result.complete(AllowOrDeny.DENY)
                            return@launch
                        }
                    }

                    result.complete(AllowOrDeny.ALLOW)
                }
                return result
            }

            override fun onLoadError(session: GeckoSession, uri: String?, error: WebRequestError): GeckoResult<String>? {
                if (wasHttpsForced && uri?.startsWith("https://") == true) {
                    val httpFallback = uri.replaceFirst("https://", "http://")
                    wasHttpsForced = false
                    _geckoSession.load(GeckoSession.Loader().uri(httpFallback).flags(GeckoSession.LOAD_FLAGS_REPLACE_HISTORY))
                    return null
                }
                _lastError.value = error
                resetSecurityState()
                if (uri != null) {
                    _currentUrl.value = uri
                    lastAttemptedUrl = uri
                }
                return null
            }
        }

        _geckoSession.scrollDelegate = object : GeckoSession.ScrollDelegate {
            override fun onScrollChanged(session: GeckoSession, scrollX: Int, scrollY: Int) {
                _isAtTop.value = scrollY <= 0
            }
        }

        _geckoSession.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                _title.value = title ?: ""
                if (title?.contains("404", ignoreCase = true) == true && title.contains("Not Found", ignoreCase = true)) {
                    if (_lastError.value == null) {
                        _lastError.value = WebRequestError(WebRequestError.ERROR_FILE_NOT_FOUND, WebRequestError.ERROR_CATEGORY_URI)
                        resetSecurityState()
                    }
                } else if (_lastError.value == null) {
                    lastCommittedTitle = title ?: ""
                }
            }

            override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) {
                _isAppLevelFullscreen.value = fullScreen
            }

            override fun onCrash(session: GeckoSession) {
                Log.w("BrowserViewModel", "Renderer process crashed. Attempting state restoration.")
                restoreSession()
            }

            override fun onKill(session: GeckoSession) {
                Log.w("BrowserViewModel", "Renderer process killed. Attempting state restoration.")
                restoreSession()
            }
        }
    }

    @androidx.annotation.OptIn(ExperimentalGeckoViewApi::class)
    @OptIn(ExperimentalGeckoViewApi::class)
    private fun applySettings(info: LuminaInfo) {
        val desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:135.0) Gecko/20100101 Firefox/135.0"
        val androidUA = "Mozilla/5.0 (Android 15; Mobile; rv:135.0) Gecko/135.0 Firefox/135.0"

        _geckoSession.settings.apply {
            if (info.randomizeUserAgent && info.afpEnabled) {
                userAgentOverride = desktopUA
                GeckoPreferenceController.setGeckoPref("general.platform.override", "Win32", GeckoPreferenceController.PREF_BRANCH_USER)
                GeckoPreferenceController.setGeckoPref("general.appversion.override", "5.0 (Windows)", GeckoPreferenceController.PREF_BRANCH_USER)
                GeckoPreferenceController.setGeckoPref("general.oscpu.override", "Windows NT 10.0; Win64; x64", GeckoPreferenceController.PREF_BRANCH_USER)
            } else if (!info.randomizeUserAgent && info.afpEnabled) {
                userAgentOverride = androidUA
                GeckoPreferenceController.setGeckoPref("general.platform.override", "Android", GeckoPreferenceController.PREF_BRANCH_USER)
                GeckoPreferenceController.setGeckoPref("general.appversion.override", "5.0 (Android 15)", GeckoPreferenceController.PREF_BRANCH_USER)
                GeckoPreferenceController.setGeckoPref("general.oscpu.override", "Android 15", GeckoPreferenceController.PREF_BRANCH_USER)
            } else {
                userAgentOverride = null
                GeckoPreferenceController.setGeckoPref("general.platform.override", "", GeckoPreferenceController.PREF_BRANCH_USER)
                GeckoPreferenceController.setGeckoPref("general.appversion.override", "", GeckoPreferenceController.PREF_BRANCH_USER)
                GeckoPreferenceController.setGeckoPref("general.oscpu.override", "", GeckoPreferenceController.PREF_BRANCH_USER)
            }
            
            useTrackingProtection = info.afpEnabled
            allowJavascript = if (info.afpEnabled) !info.disableJavascript else true
        }

        // NOTE: RFP (Resist Fingerprinting) can trigger bot checks on sites like YouTube.
        val shouldResistFingerprinting = info.afpEnabled || torEnabled.value
        GeckoPreferenceController.setGeckoPref("privacy.resistFingerprinting", shouldResistFingerprinting, GeckoPreferenceController.PREF_BRANCH_USER)
        
        // If Tor is on, we definitely want UTC. If AFP is on and spoofTimezone is on, we also want UTC.
        val shouldSpoofTimezone = (info.spoofTimezone && info.afpEnabled) || torEnabled.value
        if (shouldSpoofTimezone) {
            // Gecko doesn't have a direct "set timezone" pref, but RFP forces UTC.
            // We ensure it's on if Tor is on.
            GeckoPreferenceController.setGeckoPref("privacy.resistFingerprinting", true, GeckoPreferenceController.PREF_BRANCH_USER)
        }

        GeckoPreferenceController.setGeckoPref("webgl.disabled", info.disableWebGl || !info.afpEnabled, GeckoPreferenceController.PREF_BRANCH_USER)
        GeckoPreferenceController.setGeckoPref("dom.audioContext.enabled", !info.disableAudioContext && info.afpEnabled, GeckoPreferenceController.PREF_BRANCH_USER)

        if (info.afpEnabled) {
            if (info.randomizeUserAgent) {
                GeckoPreferenceController.setGeckoPref("privacy.resistFingerprinting.target_video_card", "Intel(R) HD Graphics 620", GeckoPreferenceController.PREF_BRANCH_USER)
            } else {
                GeckoPreferenceController.setGeckoPref("privacy.resistFingerprinting.target_video_card", "", GeckoPreferenceController.PREF_BRANCH_USER)
            }
            GeckoPreferenceController.setGeckoPref("privacy.resistFingerprinting.canvasSerialization", info.randomizeCanvas, GeckoPreferenceController.PREF_BRANCH_USER)
            if (info.spoofHardware) {
                GeckoPreferenceController.setGeckoPref("dom.maxHardwareConcurrency", 2, GeckoPreferenceController.PREF_BRANCH_USER)
                GeckoPreferenceController.setGeckoPref("dom.enable_performance", false, GeckoPreferenceController.PREF_BRANCH_USER)
            } else {
                GeckoPreferenceController.setGeckoPref("dom.enable_performance", true, GeckoPreferenceController.PREF_BRANCH_USER)
            }
            if (info.spoofLocale) {
                GeckoPreferenceController.setGeckoPref("intl.accept_languages", "en-US, en", GeckoPreferenceController.PREF_BRANCH_USER)
            } else {
                GeckoPreferenceController.setGeckoPref("intl.accept_languages", "", GeckoPreferenceController.PREF_BRANCH_USER)
            }
            val paymentEnabled = !info.disablePayment
            GeckoPreferenceController.setGeckoPref("dom.payments.enabled", paymentEnabled, GeckoPreferenceController.PREF_BRANCH_USER)
            GeckoPreferenceController.setGeckoPref("dom.payment.request.enabled", paymentEnabled, GeckoPreferenceController.PREF_BRANCH_USER)
            GeckoPreferenceController.setGeckoPref("dom.payments.canMakePayment.enabled", paymentEnabled, GeckoPreferenceController.PREF_BRANCH_USER)
        } else {
            GeckoPreferenceController.setGeckoPref("dom.payments.enabled", true, GeckoPreferenceController.PREF_BRANCH_USER)
            GeckoPreferenceController.setGeckoPref("dom.payment.request.enabled", true, GeckoPreferenceController.PREF_BRANCH_USER)
            GeckoPreferenceController.setGeckoPref("dom.payments.canMakePayment.enabled", true, GeckoPreferenceController.PREF_BRANCH_USER)
            GeckoPreferenceController.setGeckoPref("privacy.resistFingerprinting.target_video_card", "", GeckoPreferenceController.PREF_BRANCH_USER)
            GeckoPreferenceController.setGeckoPref("privacy.resistFingerprinting.canvasSerialization", false, GeckoPreferenceController.PREF_BRANCH_USER)
            GeckoPreferenceController.setGeckoPref("dom.enable_performance", true, GeckoPreferenceController.PREF_BRANCH_USER)
            GeckoPreferenceController.setGeckoPref("intl.accept_languages", "", GeckoPreferenceController.PREF_BRANCH_USER)
        }

        GeckoPreferenceController.setGeckoPref("media.peerconnection.enabled", !info.isWebRtcDisabled, GeckoPreferenceController.PREF_BRANCH_USER)
    }

    private fun loadUrl(url: String, allowUpgrade: Boolean = true) {
        var targetUrl = url
        wasHttpsForced = false
        val host = try { android.net.Uri.parse(url).host ?: "" } catch (e: Exception) { "" }

        if (allowUpgrade && !allowedInsecureHosts.contains(host)) {
            if (!url.startsWith("http") && !url.startsWith("about:") && !url.startsWith("file:")) {
                targetUrl = "https://$url"
                wasHttpsForced = true
            } else if (url.startsWith("http://")) {
                targetUrl = url.replaceFirst("http://", "https://")
                wasHttpsForced = true
            }
        }

        lastAttemptedUrl = targetUrl
        _lastError.value = null
        _showInsecureWarning.value = null
        _showPhishingWarning.value = null
        _currentUrl.value = targetUrl
        _title.value = "" 
        resetSecurityState()
        _geckoSession.loadUri(targetUrl)
    }

    fun onSearchQuery(query: String) {
        if (query.isBlank()) return
        isGoingBack = false
        val url = if (query.equals("about:config", ignoreCase = true)) "about:config"
        else if (query.equals("about:support", ignoreCase = true)) "about:support"
        else if (query.contains(".") && !query.contains(" ")) query 
        else searchEngine.value.url + query
        loadUrl(url)
    }

    fun proceedToInsecureSite() {
        val url = _showInsecureWarning.value ?: return
        allowedInsecureHosts.add(try { android.net.Uri.parse(url).host ?: "" } catch (e: Exception) { "" })
        _showInsecureWarning.value = null
        _geckoSession.loadUri(url)
    }

    fun proceedToPhishingSite() {
        val url = _showPhishingWarning.value ?: return
        allowedPhishingHosts.add(try { android.net.Uri.parse(url).host ?: "" } catch (e: Exception) { "" })
        _showPhishingWarning.value = null
        _geckoSession.loadUri(url)
    }

    fun cancelUnsafeSite(): Boolean {
        _showInsecureWarning.value = null
        _showPhishingWarning.value = null
        return if (lastCommittedUrl.isNotEmpty() && lastCommittedUrl != "about:blank") {
            _currentUrl.value = lastCommittedUrl
            _title.value = lastCommittedTitle
            _geckoSession.stop()
            _geckoSession.reload()
            true
        } else {
            _shouldClose.value = true
            false
        }
    }

    fun goBack(): Boolean {
        if (_showInsecureWarning.value != null || _showPhishingWarning.value != null) {
            cancelUnsafeSite()
            return true
        }
        if (_lastError.value != null) {
            _lastError.value = null
            resetSecurityState()
            if (lastAttemptedUrl != lastCommittedUrl && lastCommittedUrl.isNotEmpty() && lastCommittedUrl != "about:blank") {
                _currentUrl.value = lastCommittedUrl
                _title.value = lastCommittedTitle
                _geckoSession.stop()
                _geckoSession.reload() 
                return true
            }
            return false
        }
        if (_geckoSession.isOpen && _canGoBack.value) {
            isGoingBack = true
            _lastError.value = null
            // resetSecurityState() removed to prevent incorrect "unsafe" warning on back navigation
            _geckoSession.goBack()
            return true
        }
        return false
    }

    fun reload() {
        if (_geckoSession.isOpen) {
            isGoingBack = false
            val hadError = _lastError.value != null
            _lastError.value = null
            resetSecurityState()
            if (hadError && lastAttemptedUrl != null) loadUrl(lastAttemptedUrl!!, allowUpgrade = false)
            else _geckoSession.reload()
        }
    }

    fun exitFullScreen() {
        if (_geckoSession.isOpen) _geckoSession.exitFullScreen()
        _isAppLevelFullscreen.value = false
    }

    override fun onCleared() {
        super.onCleared()
        autoCloseJob?.cancel()
        if (_geckoSession.isOpen) _geckoSession.close()
        globalGeckoRuntime.storageController.clearDataForSessionContext(sessionContextId)
        globalGeckoRuntime.storageController.clearData(StorageController.ClearFlags.ALL)
        System.gc()
    }
}
