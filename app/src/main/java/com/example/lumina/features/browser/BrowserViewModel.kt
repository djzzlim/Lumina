package com.example.lumina.features.browser

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumina.core.LuminaRepository
import com.example.lumina.core.data.LuminaInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val luminaRepository: LuminaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val luminaId: Long = savedStateHandle.get<Long>("luminaId")!!
    val luminaInfo: StateFlow<LuminaInfo?> = luminaRepository.getLuminaById(luminaId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val geckoSession = GeckoSession()

    fun applySettings(luminaInfo: LuminaInfo) {
        val settings = geckoSession.settings
        if (luminaInfo.afpEnabled) {
            // Fix: Use GeckoSessionSettings to access the constants
            settings.userAgentMode = if (luminaInfo.randomizeUserAgent)
                GeckoSessionSettings.USER_AGENT_MODE_MOBILE
            else
                GeckoSessionSettings.USER_AGENT_MODE_DESKTOP

            settings.useTrackingProtection = luminaInfo.afpEnabled
        }
        settings.allowJavascript = true
    }
}