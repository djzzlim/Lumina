package com.example.lumina.features.new_lumina

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

// This data class represents the entire state of the NewLuminaScreen.
data class NewLuminaUiState(
    val name: String = "",
    val url: String = "https://",
    val selectedIcon: ImageVector = Icons.Default.Language,
    val selectedColor: Color = Color(0xFF00A2FF)
    // Add other states like region, proxy, etc., here later
)

class NewLuminaViewModel : ViewModel() {

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
        // TODO: Implement actual save logic here.
        // You can access the complete state via `_uiState.value`.
        // For example: val nameToSave = _uiState.value.name
        println("Saving Lumina: ${_uiState.value}")
    }
}
