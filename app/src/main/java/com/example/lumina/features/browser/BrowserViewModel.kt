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

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val luminaRepository: LuminaRepository,
    private val profileManager: ProfileManager,
    private val globalGeckoRuntime: GeckoRuntime,
    @ApplicationContext private val applicationContext: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val luminaId: Long = savedStateHandle.get<Long>("luminaId")!!
    val luminaInfo: StateFlow<LuminaInfo?> = luminaRepository.getLuminaById(luminaId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val geckoRuntime: GeckoRuntime = globalGeckoRuntime

    private val _geckoSession = GeckoSession()
    val geckoSession: GeckoSession get() = _geckoSession

    private val bangs = listOf(
        Bang("!g", "https://www.google.com/search?q=%s"),
        Bang("!ddg", "https://duckduckgo.com/?q=%s"),
        Bang("!yt", "https://www.youtube.com/results?search_query=%s"),
    )

    private var isInitialized = false

    init {
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

    private fun applySettings(info: LuminaInfo) {
        _geckoSession.settings.userAgentMode = if (info.randomizeUserAgent)
            GeckoSessionSettings.USER_AGENT_MODE_MOBILE
        else
            GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
        _geckoSession.settings.useTrackingProtection = info.afpEnabled
        _geckoSession.settings.allowJavascript = true
    }

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
