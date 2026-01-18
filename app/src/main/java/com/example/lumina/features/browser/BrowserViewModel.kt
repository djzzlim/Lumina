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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import javax.inject.Inject

/**
 * ViewModel for the [BrowserScreen].
 *
 * This class manages the [GeckoSession], handles URL loading and searching logic,
 * and applies privacy settings based on the associated [LuminaInfo].
 *
 * @property luminaRepository Repository for accessing lumina configuration.
 * @property profileManager Manager for the active profile.
 * @property globalGeckoRuntime The singleton [GeckoRuntime] used by the app.
 * @property applicationContext The application context.
 * @property savedStateHandle Handle for retrieving navigation arguments like `luminaId`.
 */
@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val luminaRepository: LuminaRepository,
    private val profileManager: ProfileManager,
    private val globalGeckoRuntime: GeckoRuntime,
    @ApplicationContext private val applicationContext: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val luminaId: Long = savedStateHandle.get<Long>("luminaId")!!
    /**
     * StateFlow emitting the [LuminaInfo] for the current browser session.
     */
    val luminaInfo: StateFlow<LuminaInfo?> = luminaRepository.getLuminaById(luminaId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    /**
     * Exposes the gecko runtime.
     */
    val geckoRuntime: GeckoRuntime = globalGeckoRuntime

    private val _geckoSession = GeckoSession()
    /**
     * The [GeckoSession] instance used for this browser screen.
     */
    val geckoSession: GeckoSession get() = _geckoSession

    private val bangs = listOf(
        Bang("!g", "https://www.google.com/search?q=%s"),
        Bang("!ddg", "https://duckduckgo.com/?q=%s"),
        Bang("!yt", "https://www.youtube.com/results?search_query=%s"),
    )

    private var isInitialized = false

    init {
        // Initialize the browser session once lumina info is available.
        viewModelScope.launch {
            luminaInfo.filterNotNull().collect { info ->
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
     * Applies privacy and browser settings to the current session based on [LuminaInfo].
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
     * Processes a search or navigation query.
     *
     * Supports "bangs" (e.g., !g for Google), direct URL entry, or general search.
     *
     * @param query The user's input string.
     */
    fun onSearchQuery(query: String) {
        if (query.isBlank()) return
        
        val bang = bangs.find { query.startsWith(it.trigger) }
        val url = when {
            bang != null -> {
                val searchQuery = query.substring(bang.trigger.length).trim()
                bang.urlTemplate.replace("%s", searchQuery)
            }
            query.contains(".") && !query.contains(" ") -> {
                if (query.startsWith("http")) query else "https://$query"
            }
            else -> {
                "https://www.google.com/search?q=$query"
            }
        }
        _geckoSession.loadUri(url)
    }
}
