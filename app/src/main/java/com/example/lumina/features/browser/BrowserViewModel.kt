package com.example.lumina.features.browser

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.StorageController
import javax.inject.Inject

/**
 * ViewModel for the Browser screen.
 * Implements RAM protection and aggressive memory management to prevent data recovery.
 *
 * @property luminaRepository Repository for accessing Lumina profile data.
 * @property profileManager Manager for handling Lumina profiles.
 * @property globalGeckoRuntime The shared GeckoRuntime instance.
 * @property applicationContext The application context.
 * @param savedStateHandle Handle to saved state, used to retrieve the luminaId.
 */
@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val luminaRepository: LuminaRepository,
    private val profileManager: ProfileManager,
    private val globalGeckoRuntime: GeckoRuntime,
    @ApplicationContext private val applicationContext: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /**
     * The ID of the Lumina profile being browsed.
     */
    private val luminaId: Long = savedStateHandle.get<Long>("luminaId")!!
    
    /**
     * [StateFlow] emitting the [LuminaInfo] for the current profile.
     */
    val luminaInfo: StateFlow<LuminaInfo?> = luminaRepository.getLuminaById(luminaId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    /**
     * The global [GeckoRuntime] used by this session.
     */
    val geckoRuntime: GeckoRuntime = globalGeckoRuntime

    // Initialize GeckoSession with Private Mode enabled.
    // This ensures history, cookies, and cache are not persisted to disk.
    private val _geckoSession = GeckoSession(
        GeckoSessionSettings.Builder()
            .usePrivateMode(true)
            .build()
    )
    
    /**
     * The [GeckoSession] instance used for browsing.
     * Initialized in private mode to ensure data is not persisted.
     */
    val geckoSession: GeckoSession get() = _geckoSession

    private val _progress = MutableStateFlow(0)
    /**
     * [StateFlow] emitting the current page loading progress (0-100).
     */
    val progress: StateFlow<Int> = _progress.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    /**
     * [StateFlow] emitting whether a page is currently loading.
     */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentUrl = MutableStateFlow("")
    /**
     * [StateFlow] emitting the current URL of the loaded page.
     */
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _title = MutableStateFlow("")
    /**
     * [StateFlow] emitting the title of the current page.
     */
    val title: StateFlow<String> = _title.asStateFlow()

    private val _isAtTop = MutableStateFlow(true)
    /**
     * [StateFlow] emitting whether the page is scrolled to the top.
     */
    val isAtTop: StateFlow<Boolean> = _isAtTop.asStateFlow()

    private val _isSecure = MutableStateFlow(false)
    /**
     * [StateFlow] emitting whether the current connection is secure (HTTPS).
     */
    val isSecure: StateFlow<Boolean> = _isSecure.asStateFlow()

    private val _securityInfo = MutableStateFlow<GeckoSession.ProgressDelegate.SecurityInformation?>(null)
    /**
     * [StateFlow] emitting detailed security information for the current page.
     */
    val securityInfo: StateFlow<GeckoSession.ProgressDelegate.SecurityInformation?> = _securityInfo.asStateFlow()

    private val _canGoBack = MutableStateFlow(false)
    /**
     * [StateFlow] emitting whether the browser can navigate back in history.
     */
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    /**
     * Internal state to track if the screen transition animation has finished.
     * Prevents the browser from loading content until the UI is ready to avoid stutter.
     */
    private val _isAnimationFinished = MutableStateFlow(false)

    /**
     * Flag to ensure the [GeckoSession] is opened and the initial URL is loaded only once.
     */
    private var isInitialized = false

    init {
        setupDelegates()
        
        // Only start loading once we have the info AND the entry animation is finished
        viewModelScope.launch {
            combine(luminaInfo.filterNotNull(), _isAnimationFinished) { info, finished ->
                if (finished) info else null
            }.filterNotNull().collect { info ->
                if (!isInitialized) {
                    if (!_geckoSession.isOpen) {
                        _geckoSession.open(globalGeckoRuntime)
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
     * Signals that the screen transition animation has finished.
     * This triggers the initial page load if [luminaInfo] is available.
     */
    fun onAnimationFinished() {
        _isAnimationFinished.value = true
    }

    /**
     * Sets up delegates for the [GeckoSession] to monitor progress, navigation, scrolling, and content changes.
     */
    private fun setupDelegates() {
        _geckoSession.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onProgressChange(session: GeckoSession, progress: Int) {
                _progress.value = progress
                _isLoading.value = progress < 100
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
                isRedirection: Boolean
            ) {
                _currentUrl.value = url ?: ""
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                _canGoBack.value = canGoBack
            }
        }

        _geckoSession.scrollDelegate = object : GeckoSession.ScrollDelegate {
            fun onScrollChange(session: GeckoSession, scrollX: Int, scrollY: Int) {
                _isAtTop.value = scrollY <= 0
            }
        }

        _geckoSession.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                _title.value = title ?: ""
            }
        }
    }

    /**
     * Applies settings from [LuminaInfo] to the [GeckoSession].
     *
     * @param info The [LuminaInfo] containing settings like user agent randomization and tracking protection.
     */
    private fun applySettings(info: LuminaInfo) {
        _geckoSession.settings.userAgentMode = if (info.randomizeUserAgent)
            GeckoSessionSettings.USER_AGENT_MODE_MOBILE
        else
            GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
        _geckoSession.settings.useTrackingProtection = info.afpEnabled
        _geckoSession.settings.allowJavascript = true
    }

    /**
     * Navigates to a new URL or performs a search query.
     *
     * @param query The URL or search term entered by the user.
     */
    fun onSearchQuery(query: String) {
        if (query.isBlank()) return
        
        val url = if (query.contains(".") && !query.contains(" ")) {
            if (query.startsWith("http")) query else "https://$query"
        } else {
            "https://www.google.com/search?q=$query"
        }
        _geckoSession.loadUri(url)
    }

    /**
     * Navigates back in history if possible.
     */
    fun goBack() {
        _geckoSession.goBack()
    }

    /**
     * Reloads the current page in the [GeckoSession].
     */
    fun reload() {
        _geckoSession.reload()
    }

    /**
     * Wipes session data from RAM and disk (if anything was cached) and closes the session.
     * This is called when the ViewModel is destroyed to ensure no forensic trace remains.
     */
    override fun onCleared() {
        super.onCleared()
        
        // 1. Close the session to release Gecko resources
        if (_geckoSession.isOpen) {
            _geckoSession.close()
        }

        // 2. Aggressively clear session-related data from the storage controller
        // Even in private mode, this ensures any in-memory buffers are purged.
        globalGeckoRuntime.storageController.clearData(StorageController.ClearFlags.ALL)

        // 3. Clear our own state flows to remove strings from the heap
        _currentUrl.value = ""
        _title.value = ""
        _securityInfo.value = null
        
        // 4. Suggest Garbage Collection (though not guaranteed, it hints at sensitivity)
        System.gc()
    }
}
