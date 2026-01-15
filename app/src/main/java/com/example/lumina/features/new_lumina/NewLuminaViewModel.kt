package com.example.lumina.features.new_lumina

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumina.core.LuminaRepository
import com.example.lumina.core.data.LuminaInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

// This data class represents the entire state of the NewLuminaScreen.
data class NewLuminaUiState(
    val name: String = "",
    val url: String = "https://",
    val selectedIcon: ImageVector = Icons.Default.Language,
    val selectedColor: Color = Color(0xFF00A2FF),
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

@HiltViewModel
class NewLuminaViewModel @Inject constructor(
    private val repository: LuminaRepository
) : ViewModel() {

    // The private, mutable state that only the ViewModel can change.
    private val _uiState = MutableStateFlow(NewLuminaUiState())
    // The public, read-only state that the UI observes.
    val uiState = _uiState.asStateFlow()

    // --- Events from the UI ---

    fun onNameChange(newName: String) {
        _uiState.update { currentState ->
            currentState.copy(name = newName)
        }
    }

    fun onUrlChange(newUrl: String) {
        _uiState.update { currentState ->
            currentState.copy(url = newUrl)
        }
    }

    fun onIconSelected(newIcon: ImageVector) {
        _uiState.update { currentState ->
            currentState.copy(selectedIcon = newIcon)
        }
    }

    // Called once when the screen is created with a URL from the QR scanner
    fun initializeFromScannedUrl(encodedUrl: String?) {
        if (!encodedUrl.isNullOrBlank()) {
            // Only update if the URL is the default, to avoid overwriting user input
            if (uiState.value.url == "https://") {
                val decodedUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
                _uiState.update { it.copy(url = decodedUrl) }
            }
        }
    }

    fun onSave() {
        viewModelScope.launch {
            val state = _uiState.value
            val luminaInfo = LuminaInfo(
                name = state.name,
                url = state.url,
                icon = getIconName(state.selectedIcon),
                color = state.selectedColor.value.toLong(),
                isEphemeral = state.isEphemeral,
                isWebRtcDisabled = state.isWebRtcDisabled,
                afpEnabled = state.afpEnabled,
                randomizeUserAgent = state.randomizeUserAgent,
                spoofLocale = state.spoofLocale,
                spoofTimezone = state.spoofTimezone,
                randomizeCanvas = state.randomizeCanvas,
                disableAudioContext = state.disableAudioContext,
                disableWebGl = state.disableWebGl,
                randomizeScreen = state.randomizeScreen,
                spoofHardware = state.spoofHardware,
                disablePayment = state.disablePayment
            )
            repository.insertLumina(luminaInfo)
        }
    }

    private fun getIconName(icon: ImageVector): String {
        return when (icon) {
            Icons.Default.Language -> "Language"
            Icons.Default.Visibility -> "Visibility"
            else -> "Language"
        }
    }

    // --- Events from Advanced Options ---
    fun setEphemeral(enabled: Boolean) = _uiState.update { it.copy(isEphemeral = enabled) }
    fun setWebRtcDisabled(disabled: Boolean) = _uiState.update { it.copy(isWebRtcDisabled = disabled) }
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
