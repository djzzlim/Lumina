package com.example.lumina.features.new_lumina

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.Color
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
 *
 * @property name The name of the new Lumina instance.
 * @property url The URL for the new Lumina instance.
 * @property selectedIcon The selected [ImageVector] icon.
 * @property selectedColor The selected [Color] theme.
 * @property isEphemeral Whether to use ephemeral storage.
 * @property isWebRtcDisabled Whether to disable WebRTC.
 * @property afpEnabled Whether global anti-fingerprinting is enabled.
 * @property randomizeUserAgent Whether to randomize the User-Agent.
 * @property spoofLocale Whether to spoof the system locale.
 * @property spoofTimezone Whether to spoof the system timezone.
 * @property randomizeCanvas Whether to randomize canvas fingerprinting.
 * @property disableAudioContext Whether to disable AudioContext.
 * @property disableWebGl Whether to disable WebGL.
 * @property randomizeScreen Whether to randomize screen dimensions.
 * @property spoofHardware Whether to spoof hardware information.
 * @property disablePayment Whether to disable Payment APIs.
 */
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

/**
 * ViewModel for creating a new Lumina instance.
 *
 * Manages the state of the creation form, handles scanned URLs, and saves the
 * final configuration to the repository.
 *
 * @property repository Repository for lumina data operations.
 * @property profileManager Manager for the active profile.
 */
@HiltViewModel
class NewLuminaViewModel @Inject constructor(
    private val repository: LuminaRepository,
    private val profileManager: ProfileManager
) : ViewModel() {

    // The private, mutable state that only the ViewModel can change.
    private val _uiState = MutableStateFlow(NewLuminaUiState())
    /**
     * The public, read-only state that the UI observes.
     */
    val uiState = _uiState.asStateFlow()

    // --- Events from the UI ---

    /**
     * Updates the name in the UI state.
     */
    fun onNameChange(newName: String) {
        _uiState.update { currentState ->
            currentState.copy(name = newName)
        }
    }

    /**
     * Updates the URL in the UI state.
     */
    fun onUrlChange(newUrl: String) {
        _uiState.update { currentState ->
            currentState.copy(url = newUrl)
        }
    }

    /**
     * Updates the selected icon in the UI state.
     */
    fun onIconSelected(newIcon: ImageVector) {
        _uiState.update { currentState ->
            currentState.copy(selectedIcon = newIcon)
        }
    }

    /**
     * Initializes the form with a URL scanned from a QR code.
     *
     * @param encodedUrl The URL string, potentially URL-encoded.
     */
    fun initializeFromScannedUrl(encodedUrl: String?) {
        if (!encodedUrl.isNullOrBlank()) {
            // Only update if the URL is the default, to avoid overwriting user input
            if (uiState.value.url == "https://") {
                val decodedUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
                _uiState.update { it.copy(url = decodedUrl) }
            }
        }
    }

    /**
     * Saves the current form data as a new [LuminaInfo] entry in the repository.
     */
    fun onSave() {
        viewModelScope.launch {
            val profileId = profileManager.getCurrentProfileId().first() 
                ?: throw IllegalStateException("No profile selected")
            
            val state = _uiState.value
            val luminaInfo = LuminaInfo(
                profileId = profileId,
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

    /**
     * Maps an [ImageVector] to a string identifier for storage.
     */
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
