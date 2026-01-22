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
import org.mozilla.geckoview.WebRequestError
import javax.inject.Inject

/**
 * ViewModel for the [BrowserScreen].
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

    val luminaInfo: StateFlow<LuminaInfo?> = luminaRepository.getLuminaById(luminaId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _geckoSession = GeckoSession(
        GeckoSessionSettings.Builder()
            .usePrivateMode(true)
            .build()
    )

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

    private val _lastError = MutableStateFlow<WebRequestError?>(null)
    val lastError: StateFlow<WebRequestError?> = _lastError.asStateFlow()

    private val _isAnimationFinished = MutableStateFlow(false)
    private var isInitialized = false
    private var isGoingBack = false
    
    private var lastAttemptedUrl: String? = null
    private var lastCommittedUrl: String = ""
    private var lastCommittedTitle: String = ""

    private val searchEngine = appPreferences.searchEngineFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.example.lumina.core.SearchEngine.Google)

    init {
        setupDelegates()

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
                    resetSecurityState()
                    _geckoSession.loadUri(info.url)
                    lastAttemptedUrl = info.url
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
                    lastCommittedUrl = url
                    _currentUrl.value = url
                }
            }

            override fun onLoadRequest(session: GeckoSession, request: GeckoSession.NavigationDelegate.LoadRequest): GeckoResult<AllowOrDeny> {
                if (request.target == GeckoSession.NavigationDelegate.TARGET_WINDOW_NEW) {
                    session.loadUri(request.uri)
                    return GeckoResult.fromValue(AllowOrDeny.DENY)
                }
                
                lastAttemptedUrl = request.uri
                _lastError.value = null
                _title.value = "" 
                resetSecurityState()
                
                return GeckoResult.fromValue(AllowOrDeny.ALLOW)
            }

            override fun onLoadError(session: GeckoSession, uri: String?, error: WebRequestError): GeckoResult<String>? {
                Log.e("BrowserViewModel", "Load error: ${error.code} URI: $uri")
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
        }
    }

    @androidx.annotation.OptIn(ExperimentalGeckoViewApi::class)
    @OptIn(ExperimentalGeckoViewApi::class)
    private fun applySettings(info: LuminaInfo) {
        _geckoSession.settings.userAgentMode = if (info.randomizeUserAgent)
            GeckoSessionSettings.USER_AGENT_MODE_MOBILE
        else
            GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
        _geckoSession.settings.useTrackingProtection = info.afpEnabled
        _geckoSession.settings.allowJavascript = true

        val webRtcEnabled = !info.isWebRtcDisabled
        GeckoPreferenceController.setGeckoPref(
            "media.peerconnection.enabled",
            webRtcEnabled,
            GeckoPreferenceController.PREF_BRANCH_USER
        )
    }

    fun onSearchQuery(query: String) {
        if (query.isBlank()) return
        isGoingBack = false
        val url = if (query.equals("about:config", ignoreCase = true)) {
            "about:config"
        } else if (query.equals("about:support", ignoreCase = true)) {
            "about:support"
        } else if (query.contains(".") && !query.contains(" ")) {
            if (query.startsWith("http")) query else "https://$query"
        } else {
            searchEngine.value.url + query
        }
        
        lastAttemptedUrl = url
        _lastError.value = null
        _currentUrl.value = url
        _title.value = "" 
        resetSecurityState()
        _geckoSession.loadUri(url)
    }

    fun goBack(): Boolean {
        if (_lastError.value != null) {
            _lastError.value = null
            resetSecurityState()
            
            if (lastAttemptedUrl != lastCommittedUrl && lastCommittedUrl.isNotEmpty()) {
                _currentUrl.value = lastCommittedUrl
                _title.value = lastCommittedTitle
                _geckoSession.stop()
                _geckoSession.reload() 
                return true
            }
        }
        
        if (_geckoSession.isOpen && _canGoBack.value) {
            isGoingBack = true
            _lastError.value = null
            resetSecurityState()
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
            
            if (hadError && lastAttemptedUrl != null) {
                _geckoSession.loadUri(lastAttemptedUrl!!)
            } else {
                _geckoSession.reload()
            }
        }
    }

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
        System.gc()
    }
}
