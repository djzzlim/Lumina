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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
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
import javax.inject.Inject

/**
 * ViewModel for the [BrowserScreen].
 *
 * This class manages the state and logic for the GeckoView-based browser, including:
 * - Session lifecycle management.
 * - Navigation and history.
 * - Progress tracking and loading state.
 * - Security and certificate information.
 * - Full-screen state management.
 * - Integration with [LuminaRepository] for site-specific settings.
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
     * The [LuminaInfo] associated with the current browser session.
     */
    val luminaInfo: StateFlow<LuminaInfo?> = luminaRepository.getLuminaById(luminaId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _geckoSession = GeckoSession(
        GeckoSessionSettings.Builder()
            .usePrivateMode(true)
            .build()
    )

    /**
     * The current [GeckoSession] being used by the browser.
     */
    val geckoSession: GeckoSession get() = _geckoSession

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentUrl = MutableStateFlow("")
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

    private val _isAnimationFinished = MutableStateFlow(false)
    private var isInitialized = false
    private var isGoingBack = false

    private val searchEngine = appPreferences.searchEngineFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.example.lumina.core.SearchEngine.Google)

    init {
        setupDelegates()

        // Observe and apply DNS changes dynamically
        viewModelScope.launch {
            appPreferences.dnsProviderFlow.collect { dnsProvider ->
                globalGeckoRuntime.settings.setTrustedRecursiveResolverUri(dnsProvider.uri)
                globalGeckoRuntime.settings.setTrustedRecursiveResolverMode(dnsProvider.mode)
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
                    _geckoSession.loadUri(info.url)
                    isInitialized = true
                } else {
                    applySettings(info)
                }
            }
        }
    }

    /**
     * Signals that the entry animation has finished, allowing the browser to start loading.
     */
    fun onAnimationFinished() {
        _isAnimationFinished.value = true
    }

    /**
     * Sets up the GeckoView delegates to handle progress, navigation, history, scrolling, and content events.
     */
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
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                permissions: List<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                _currentUrl.value = url ?: ""
            }

            override fun onLoadRequest(session: GeckoSession, request: GeckoSession.NavigationDelegate.LoadRequest): GeckoResult<AllowOrDeny> {
                if (request.target == GeckoSession.NavigationDelegate.TARGET_WINDOW_NEW) {
                    session.loadUri(request.uri)
                    return GeckoResult.fromValue(AllowOrDeny.DENY)
                }
                if (request.hasUserGesture) {
                    isGoingBack = false
                }
                return GeckoResult.fromValue(AllowOrDeny.ALLOW)
            }

            override fun onNewSession(session: GeckoSession, uri: String): GeckoResult<GeckoSession>? {
                session.loadUri(uri)
                return null
            }
        }

        _geckoSession.historyDelegate = object : GeckoSession.HistoryDelegate {
            override fun onHistoryStateChange(
                session: GeckoSession,
                historyList: GeckoSession.HistoryDelegate.HistoryList
            ) {
                _canGoBack.value = historyList.currentIndex > 0
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
            }

            override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) {
                Log.d("BrowserViewModel", "onFullScreen: $fullScreen")
                _isAppLevelFullscreen.value = fullScreen
            }
        }
    }

    /**
     * Applies the settings from the [LuminaInfo] to the current [GeckoSession].
     */
    @androidx.annotation.OptIn(ExperimentalGeckoViewApi::class)
    @OptIn(ExperimentalGeckoViewApi::class)
    private fun applySettings(info: LuminaInfo) {
        _geckoSession.settings.userAgentMode = if (info.randomizeUserAgent)
            GeckoSessionSettings.USER_AGENT_MODE_MOBILE
        else
            GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
        _geckoSession.settings.useTrackingProtection = info.afpEnabled
        _geckoSession.settings.allowJavascript = true

        // Implement WebRTC disable if configured
        val webRtcEnabled = !info.isWebRtcDisabled
        GeckoPreferenceController.setGeckoPref(
            "media.peerconnection.enabled",
            webRtcEnabled,
            GeckoPreferenceController.PREF_BRANCH_USER
        )
    }

    /**
     * Handles a search query or URL entered by the user.
     *
     * @param query The search query or URL.
     */
    fun onSearchQuery(query: String) {
        if (query.isBlank()) return
        isGoingBack = false
        val url = if (query.equals("about:config", ignoreCase = true)) {
            "about:config"
        } else if (query.contains(".") && !query.contains(" ")) {
            if (query.startsWith("http")) query else "https://$query"
        } else {
            searchEngine.value.url + query
        }
        Log.d("BrowserViewModel", "Loading URL: $url using engine: ${searchEngine.value}")
        _geckoSession.loadUri(url)
    }

    /**
     * Navigates back in the browser history if possible.
     *
     * @return True if navigation was performed, false otherwise.
     */
    fun goBack(): Boolean {
        if (_geckoSession.isOpen && _canGoBack.value) {
            isGoingBack = true
            _geckoSession.goBack()
            return true
        }
        return false
    }

    /**
     * Stops the current page load.
     */
    fun stopLoading() {
        if (_geckoSession.isOpen) {
            _geckoSession.stop()
        }
    }

    /**
     * Reloads the current page.
     */
    fun reload() {
        if (_geckoSession.isOpen) {
            isGoingBack = false
            _geckoSession.reload()
        }
    }

    /**
     * Exits the browser's full-screen mode.
     */
    fun exitFullScreen() {
        if (_geckoSession.isOpen) {
            _geckoSession.exitFullScreen()
        }
        _isAppLevelFullscreen.value = false
    }

    override fun onCleared() {
        super.onCleared()
        if (_geckoSession.isOpen) {
            _geckoSession.close()
        }
        globalGeckoRuntime.storageController.clearData(StorageController.ClearFlags.ALL)
        _currentUrl.value = ""
        _title.value = ""
        System.gc()
    }
}
