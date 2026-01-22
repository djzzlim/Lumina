package com.example.lumina.features.new_lumina

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumina.core.LuminaRepository
import com.example.lumina.core.ProfileManager
import com.example.lumina.core.database.LuminaInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

/**
 * UI State for the New Lumina screen.
 */
data class NewLuminaUiState(
    val name: String = "",
    val url: String = "https://",
    val selectedIcon: ImageVector = Icons.Default.Language,
    val selectedColor: Color = Color(0xFF00A2FF),
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
    val disablePayment: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for creating a new Lumina instance.
 */
@HiltViewModel
class NewLuminaViewModel @Inject constructor(
    private val repository: LuminaRepository,
    private val profileManager: ProfileManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewLuminaUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName, error = null) }
    }

    fun onUrlChange(newUrl: String) {
        _uiState.update { it.copy(url = newUrl, error = null) }
    }

    fun onIconSelected(newIcon: ImageVector) {
        _uiState.update { it.copy(selectedIcon = newIcon) }
    }

    fun onColorSelected(newColor: Color) {
        _uiState.update { it.copy(selectedColor = newColor) }
    }

    fun initializeFromScannedUrl(encodedUrl: String?) {
        if (!encodedUrl.isNullOrBlank()) {
            if (uiState.value.url == "https://") {
                val decodedUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
                _uiState.update { it.copy(url = decodedUrl) }
            }
        }
    }

    fun onSave(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "You have to put a name") }
            return
        }
        if (state.url.isBlank() || state.url == "https://") {
            _uiState.update { it.copy(error = "You have to put a website URL") }
            return
        }

        viewModelScope.launch {
            try {
                val profileId = profileManager.getCurrentProfileId().first() 
                    ?: throw IllegalStateException("No profile selected")
                
                val luminaInfo = LuminaInfo(
                    profileId = profileId,
                    name = state.name.trim(),
                    url = state.url.trim(),
                    icon = getIconName(state.selectedIcon),
                    color = state.selectedColor.toArgb().toLong(),
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
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error saving: ${e.message}") }
            }
        }
    }

    private fun getIconName(icon: ImageVector): String {
        return icon.name.substringAfterLast('.')
    }

    // --- Events from Advanced Options ---
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
