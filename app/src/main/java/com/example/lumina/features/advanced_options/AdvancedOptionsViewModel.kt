package com.example.lumina.features.advanced_options

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// This data class holds the entire state for the AdvancedOptionsScreen.
data class AdvancedOptionsUiState(
    val isEphemeral: Boolean = false,
    val isWebRtcDisabled: Boolean = true,
    val afpEnabled: Boolean = true,
    val randomizeUserAgent: Boolean = true,
    val spoofLocale: Boolean = true,
    val spoofTimezone: Boolean = true,
    val randomizeCanvas: Boolean = true,
    val disableAudioContext: Boolean = true,
    val disableWebGl: Boolean = true,
    val randomizeScreen: Boolean = true,
    val spoofHardware: Boolean = true,
    val disablePayment: Boolean = true
)

class AdvancedOptionsViewModel : ViewModel() {

    // The private, mutable state that only the ViewModel can change.
    private val _uiState = MutableStateFlow(AdvancedOptionsUiState())
    // The public, read-only state that the UI observes.
    val uiState = _uiState.asStateFlow()

    // --- Events from the UI ---

    fun setEphemeral(enabled: Boolean) = _uiState.update { it.copy(isEphemeral = enabled) }
    fun setWebRtcDisabled(disabled: Boolean) = _uiState.update { it.copy(isWebRtcDisabled = disabled) }

    // --- Antifingerprinting Events ---
    fun setAfpEnabled(enabled: Boolean) = _uiState.update { it.copy(afpEnabled = enabled) }
    fun setRandomizeUserAgent(enabled: Boolean) = _uiState.update { it.copy(randomizeUserAgent = enabled) }
    fun setSpoofLocale(enabled: Boolean) = _uiState.update { it.copy(spoofLocale = enabled) }
    fun setSpoofTimezone(enabled: Boolean) = _uiState.update { it.copy(spoofTimezone = enabled) }
    fun setRandomizeCanvas(enabled: Boolean) = _uiState.update { it.copy(randomizeCanvas = enabled) }
    fun setDisableAudioContext(enabled: Boolean) = _uiState.update { it.copy(disableAudioContext = enabled) }
    fun setDisableWebGl(enabled: Boolean) = _uiState.update { it.copy(disableWebGl = enabled) }
    fun setRandomizeScreen(enabled: Boolean) = _uiState.update { it.copy(randomizeScreen = enabled) }
    fun setSpoofHardware(enabled: Boolean) = _uiState.update { it.copy(spoofHardware = enabled) }
    fun setDisablePayment(enabled: Boolean) = _uiState.update { it.copy(disablePayment = enabled) }
}
