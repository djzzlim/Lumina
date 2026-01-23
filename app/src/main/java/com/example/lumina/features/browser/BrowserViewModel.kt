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
 *
 * This class manages the lifecycle and logic of a single browser "tab," including:
 * - Forensic session isolation using [sessionContextId].
 * - Secure background auto-close logic with data wiping.
 * - Robust back-navigation handling to prevent history skipping.
 * - HTTPS-First logic with automatic HTTP fallback and insecure warnings.
 * - Real-time security UI state management.
 */
@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val luminaRepository: LuminaRepository,
    @Suppress("UNUSED_PARAMETER") private val profileManager: ProfileManager,
    private val globalGeckoRuntime: GeckoRuntime,
    private val appPreferences: AppPreferences,
    @param:ApplicationContext private val applicationContext: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val luminaId: Long = savedStateHandle.get<Long>("luminaId")!!
    
    /**
     * Unique identifier for this session's data container.
     * Prevents cookies/history from leaking between different tabs.
     */
    private val sessionContextId = "lumina_session_$luminaId"

    /**
     * The profile info for the current site being browsed.
     */
    val luminaInfo: StateFlow<LuminaInfo?> = luminaRepository.getLuminaById(luminaId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _geckoSession = GeckoSession(
        GeckoSessionSettings.Builder()
            .usePrivateMode(true)
            .contextId(sessionContextId)
            .build()
    )

    /**
     * The GeckoView session instance for this tab.
     */
    val geckoSession: GeckoSession get() = _geckoSession

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentUrl = MutableStateFlow("")
    /**
     * The URL currently displayed in the address bar.
     */
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
    /**
     * Stores the last encounter [WebRequestError] to trigger the error UI.
     */
    val lastError: StateFlow<WebRequestError?> = _lastError.asStateFlow()

    private val _showInsecureWarning = MutableStateFlow<String?>(null)
    /**
     * Stores the URL that triggered an insecure (HTTP) warning.
     */
    val showInsecureWarning: StateFlow<String?> = _showInsecureWarning.asStateFlow()

    private val _shouldClose = MutableStateFlow(false)
    /**
     * Signal sent to the UI to navigate back to the home screen (e.g. after background timeout).
     */
    val shouldClose: StateFlow<Boolean> = _shouldClose.asStateFlow()

    private val _isAnimationFinished = MutableStateFlow(false)
    private var isInitialized = false
    private var isGoingBack = false
    
    // Internal tracking for history and fallback logic
    private var lastAttemptedUrl: String? = null
    private var lastCommittedUrl: String = ""
    private var lastCommittedTitle: String = ""
    private var wasHttpsForced = false
    private val allowedInsecureHosts = mutableSetOf<String>()
    
    private var autoCloseJob: Job? = null

    private val searchEngine = appPreferences.searchEngineFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.example.lumina.core.SearchEngine.Google)

    init {
        setupDelegates()

        // Apply DNS settings dynamically as they change in Settings
        viewModelScope.launch {
            appPreferences.dnsProviderFlow.collect { dnsProvider ->
                globalGeckoRuntime.settings.setTrustedRecursiveResolverUri(dnsProvider.uri)
                globalGeckoRuntime.settings.setTrustedRecursiveResolverMode(dnsProvider.mode)
            }
        }

        // Initialize the browser only after the entry animation is finished
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
                    loadUrl(info.url)
                    isInitialized = true
                } else {
                    applySettings(info)
                }
            }
        }
    }

    /**
     * Resets the security indicators. Called before new loads to prevent showing stale certificate info.
     */
    private fun resetSecurityState() {
        _isSecure.value = false
        _securityInfo.value = null
    }

    /**
     * Signals that the Compose entry animation is done, triggering the initial URL load.
     */
    fun onAnimationFinished() {
        _isAnimationFinished.value = true
    }

    /**
     * Handles background inactivity logic. Starts a timer based on user settings.
     * If the timeout is reached, it performs a forensic wipe of the session data.
     */
    fun onAppBackgrounded() {
        autoCloseJob?.cancel()
        autoCloseJob = viewModelScope.launch {
            val timeout = appPreferences.autoCloseTimeoutFlow.first()
            if (timeout.minutes > 0) {
                Log.d("BrowserViewModel", "App backgrounded. Auto-close scheduled in ${timeout.minutes} minutes.")
                delay(timeout.minutes * 60 * 1000)
                Log.d("BrowserViewModel", "Timeout reached. Performing forensic wipe.")
                
                if (_geckoSession.isOpen) {
                    _geckoSession.close()
                }
                
                // Forensic cleanup: wipe only this context's data
                globalGeckoRuntime.storageController.clearDataForSessionContext(sessionContextId)
                
                // Flush storage to ensure deletion persists immediately
                globalGeckoRuntime.storageController.clearData(StorageController.ClearFlags.ALL)
                
                _shouldClose.value = true
            }
        }
    }

    /**
     * Cancels the auto-close timer when the user returns to the app.
     */
    fun onAppForegrounded() {
        autoCloseJob?.cancel()
        autoCloseJob = null
    }

    private fun setupDelegates() {
        _geckoSession.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onProgressChange(session: GeckoSession, progress: Int) {
                _progress.value = progress
                _isLoading.value = progress < 100
                
                if (progress == 100 && isGoingBack) {
                    isGoingBack = false
                }
            }

            override fun onSecurityChange(
                session: GeckoSession,
                securityInfo: GeckoSession.ProgressDelegate.SecurityInformation
            ) {
                _isSecure.value = securityInfo.isSecure
                _securityInfo.value = securityInfo
            }
        }

        _geckoSession.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                _canGoBack.value = canGoBack
            }

            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                permissions: List<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                if (url != null && url.isNotEmpty()) {
                    // Page has successfully started rendering a new location
                    lastCommittedUrl = url
                    _currentUrl.value = url
                    if (url.startsWith("https")) wasHttpsForced = false
                }
            }

            override fun onLoadRequest(session: GeckoSession, request: GeckoSession.NavigationDelegate.LoadRequest): GeckoResult<AllowOrDeny> {
                if (request.target == GeckoSession.NavigationDelegate.TARGET_WINDOW_NEW) {
                    loadUrl(request.uri)
                    return GeckoResult.fromValue(AllowOrDeny.DENY)
                }

                val host = try { android.net.Uri.parse(request.uri).host ?: "" } catch (e: Exception) { "" }

                // Trigger the Insecure Connection Warning for HTTP sites not yet whitelisted
                if (request.uri.startsWith("http://") && !request.isRedirect) {
                    if (!allowedInsecureHosts.contains(host)) {
                        _currentUrl.value = request.uri
                        _showInsecureWarning.value = request.uri
                        resetSecurityState()
                        _geckoSession.stop()
                        return GeckoResult.fromValue(AllowOrDeny.DENY)
                    }
                }
                
                // Immediate UI update for user-triggered navigations
                if (!request.isRedirect) {
                    lastAttemptedUrl = request.uri
                    _currentUrl.value = request.uri
                    _lastError.value = null
                    _showInsecureWarning.value = null
                    _title.value = "" 
                    resetSecurityState()
                }
                
                return GeckoResult.fromValue(AllowOrDeny.ALLOW)
            }

            override fun onLoadError(session: GeckoSession, uri: String?, error: WebRequestError): GeckoResult<String>? {
                Log.e("BrowserViewModel", "Load error: ${error.code} URI: $uri")
                
                // Automatic fallback to HTTP if an upgraded HTTPS request failed
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
                
                // Fallback detection for HTTP 404 errors (not protocol errors)
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
        }
    }

    /**
     * Configures the [GeckoSession] based on the site-specific [LuminaInfo].
     */
    @androidx.annotation.OptIn(ExperimentalGeckoViewApi::class)
    @OptIn(ExperimentalGeckoViewApi::class)
    private fun applySettings(info: LuminaInfo) {
        val desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:133.0) Gecko/20100101 Firefox/133.0"
        val iphoneUA = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"

        _geckoSession.settings.apply {
            // User-Agent and Platform Spoofing
            if (info.randomizeUserAgent) {
                userAgentOverride = desktopUA
                GeckoPreferenceController.setGeckoPref("general.platform.override", "Win32", GeckoPreferenceController.PREF_BRANCH_USER)
                GeckoPreferenceController.setGeckoPref("general.appversion.override", "5.0 (Windows)", GeckoPreferenceController.PREF_BRANCH_USER)
                GeckoPreferenceController.setGeckoPref("general.oscpu.override", "Windows NT 10.0; Win64; x64", GeckoPreferenceController.PREF_BRANCH_USER)
            } else {
                userAgentOverride = iphoneUA
                GeckoPreferenceController.setGeckoPref("general.platform.override", "iPhone", GeckoPreferenceController.PREF_BRANCH_USER)
                GeckoPreferenceController.setGeckoPref("general.appversion.override", "5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1", GeckoPreferenceController.PREF_BRANCH_USER)
                GeckoPreferenceController.setGeckoPref("general.oscpu.override", "iPhone OS 17.0", GeckoPreferenceController.PREF_BRANCH_USER)
            }
            
            useTrackingProtection = info.afpEnabled
            allowJavascript = true
        }

        // Global Anti-Fingerprinting (Resist Fingerprinting)
        GeckoPreferenceController.setGeckoPref(
            "privacy.resistFingerprinting",
            info.afpEnabled,
            GeckoPreferenceController.PREF_BRANCH_USER
        )

        if (info.afpEnabled) {
            // Force reported platform to match User-Agent in RFP mode
            if (info.randomizeUserAgent) {
                GeckoPreferenceController.setGeckoPref("privacy.resistFingerprinting.target_video_card", "Intel(R) HD Graphics 620", GeckoPreferenceController.PREF_BRANCH_USER)
            }

            // Canvas Protection
            GeckoPreferenceController.setGeckoPref(
                "privacy.resistFingerprinting.canvasSerialization",
                info.randomizeCanvas,
                GeckoPreferenceController.PREF_BRANCH_USER
            )

            // WebGL Protection
            GeckoPreferenceController.setGeckoPref(
                "webgl.disabled",
                info.disableWebGl,
                GeckoPreferenceController.PREF_BRANCH_USER
            )

            // Hardware Spoofing
            if (info.spoofHardware) {
                GeckoPreferenceController.setGeckoPref("dom.maxHardwareConcurrency", 2, GeckoPreferenceController.PREF_BRANCH_USER)
                GeckoPreferenceController.setGeckoPref("dom.enable_performance", false, GeckoPreferenceController.PREF_BRANCH_USER)
            }

            // Locale Spoofing
            if (info.spoofLocale) {
                GeckoPreferenceController.setGeckoPref("intl.accept_languages", "en-US, en", GeckoPreferenceController.PREF_BRANCH_USER)
            }

            // Payment API Protection (Disabling all related hooks)
            val paymentEnabled = !info.disablePayment
            GeckoPreferenceController.setGeckoPref("dom.payments.enabled", paymentEnabled, GeckoPreferenceController.PREF_BRANCH_USER)
            GeckoPreferenceController.setGeckoPref("dom.payment.request.enabled", paymentEnabled, GeckoPreferenceController.PREF_BRANCH_USER)
            GeckoPreferenceController.setGeckoPref("dom.payments.canMakePayment.enabled", paymentEnabled, GeckoPreferenceController.PREF_BRANCH_USER)
        } else {
            // Reset to default if AFP is off
            GeckoPreferenceController.setGeckoPref("dom.payments.enabled", true, GeckoPreferenceController.PREF_BRANCH_USER)
            GeckoPreferenceController.setGeckoPref("dom.payment.request.enabled", true, GeckoPreferenceController.PREF_BRANCH_USER)
            GeckoPreferenceController.setGeckoPref("dom.payments.canMakePayment.enabled", true, GeckoPreferenceController.PREF_BRANCH_USER)
        }

        // WebRTC protection
        val webRtcEnabled = !info.isWebRtcDisabled
        GeckoPreferenceController.setGeckoPref(
            "media.peerconnection.enabled",
            webRtcEnabled,
            GeckoPreferenceController.PREF_BRANCH_USER
        )
    }

    /**
     * Loads a URL with optional protocol upgrading.
     * @param allowUpgrade If true, automatically attempts to upgrade http:// to https://.
     */
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
        _currentUrl.value = targetUrl
        _title.value = "" 
        resetSecurityState()
        _geckoSession.loadUri(targetUrl)
    }

    /**
     * Processes a search query or URL entered by the user.
     */
    fun onSearchQuery(query: String) {
        if (query.isBlank()) return
        isGoingBack = false
        val url = if (query.equals("about:config", ignoreCase = true)) {
            "about:config"
        } else if (query.equals("about:support", ignoreCase = true)) {
            "about:support"
        } else if (query.contains(".") && !query.contains(" ")) {
            query 
        } else {
            searchEngine.value.url + query
        }
        
        loadUrl(url)
    }

    /**
     * Dismisses the Insecure Warning and allows the [http://] load to proceed.
     */
    fun proceedToInsecureSite() {
        val url = _showInsecureWarning.value ?: return
        val host = try { android.net.Uri.parse(url).host ?: "" } catch (e: Exception) { "" }
        allowedInsecureHosts.add(host)
        _showInsecureWarning.value = null
        _geckoSession.loadUri(url)
    }

    /**
     * Cancels an insecure load and reverts the UI to the last safe page.
     */
    fun cancelInsecureSite() {
        _showInsecureWarning.value = null
        if (lastCommittedUrl.isNotEmpty()) {
            _currentUrl.value = lastCommittedUrl
            _title.value = lastCommittedTitle
            _geckoSession.stop()
            _geckoSession.reload()
        }
    }

    /**
     * Navigates back.
     * Priority:
     * 1. Dismiss Insecure Warning.
     * 2. Dismiss Error Screen.
     * 3. Navigate Gecko history.
     */
    fun goBack(): Boolean {
        if (_showInsecureWarning.value != null) {
            _showInsecureWarning.value = null
            if (lastCommittedUrl.isNotEmpty()) {
                _currentUrl.value = lastCommittedUrl
                _title.value = lastCommittedTitle
                _geckoSession.stop()
                _geckoSession.reload()
            }
            return true
        }

        if (_lastError.value != null) {
            _lastError.value = null
            resetSecurityState()
            
            if (lastAttemptedUrl != lastCommittedUrl && lastCommittedUrl.isNotEmpty()) {
                // Navigation to new page failed. Return to last good page.
                _currentUrl.value = lastCommittedUrl
                _title.value = lastCommittedTitle
                _geckoSession.stop()
                _geckoSession.reload() 
                return true
            } else {
                // Error is on an already committed page. History back is required.
                if (_geckoSession.isOpen && _canGoBack.value) {
                    isGoingBack = true
                    _geckoSession.goBack()
                    return true
                }
                return false
            }
        }
        
        // Standard browser back
        if (_geckoSession.isOpen && _canGoBack.value) {
            isGoingBack = true
            _lastError.value = null
            resetSecurityState()
            _geckoSession.goBack()
            return true
        }
        return false
    }

    /**
     * Reloads the current page. Specifically handles re-triggering loads from error screens.
     */
    fun reload() {
        if (_geckoSession.isOpen) {
            isGoingBack = false
            val hadError = _lastError.value != null
            _lastError.value = null
            resetSecurityState()
            
            if (hadError && lastAttemptedUrl != null) {
                loadUrl(lastAttemptedUrl!!, allowUpgrade = false)
            } else {
                _geckoSession.reload()
            }
        }
    }

    /**
     * Forces an exit from media fullscreen mode.
     */
    fun exitFullScreen() {
        if (_geckoSession.isOpen) {
            _geckoSession.exitFullScreen()
        }
        _isAppLevelFullscreen.value = false
    }

    /**
     * Lifecycle cleanup. Performs a final forensic wipe of session data.
     */
    override fun onCleared() {
        super.onCleared()
        autoCloseJob?.cancel()
        if (_geckoSession.isOpen) {
            _geckoSession.close()
        }
        globalGeckoRuntime.storageController.clearDataForSessionContext(sessionContextId)
        System.gc()
    }
}
