package com.example.lumina.features.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// This is your existing data class, now part of the home feature's domain.
data class LuminaInfo(
    val icon: ImageVector,
    val title: String,
    val url: String
)

// The state for the home screen, containing the list of items.
data class HomeUiState(
    val luminaItems: List<LuminaInfo> = emptyList()
)

class HomeViewModel : ViewModel() {

    // Private, mutable state
    private val _uiState = MutableStateFlow(HomeUiState())
    // Public, read-only state for the UI to observe
    val uiState = _uiState.asStateFlow()

    init {
        // Load the initial data when the ViewModel is created.
        // Later, this could be a call to a database or network.
        loadLuminaItems()
    }

    private fun loadLuminaItems() {
        // This is the hardcoded list from your original LuminaHomeScreen.kt
        val items = listOf(
            LuminaInfo(Icons.Default.Visibility, "Facebook", "facebook.com"),
            LuminaInfo(Icons.Default.Language, "Instagram", "instagram.com"),
            LuminaInfo(Icons.Default.Language, "Github", "github.com"),
            LuminaInfo(Icons.Default.Language, "LinkedIn", "linkedin.com"),
            LuminaInfo(Icons.Default.Visibility, "Reddit", "reddit.com"),
            LuminaInfo(Icons.Default.Language, "Twitter", "twitter.com")
        )
        // Update the state with the loaded items.
        _uiState.value = HomeUiState(luminaItems = items)
    }

    // You could add functions here later like `addLuminaItem()` or `deleteLuminaItem()`.
}
