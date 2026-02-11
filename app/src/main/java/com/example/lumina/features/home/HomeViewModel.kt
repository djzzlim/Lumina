package com.example.lumina.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumina.core.LuminaRepository
import com.example.lumina.core.ProfileManager
import com.example.lumina.core.database.LuminaInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Home screen.
 *
 * @property luminaItems List of items to display on the home screen.
 * @property selectionMode Whether the UI is currently in item selection mode.
 * @property selectedItems Set of IDs for the items currently selected.
 * @property shouldExit Whether the app should shut down.
 */
data class HomeUiState(
    val luminaItems: List<LuminaInfo> = emptyList(),
    val selectionMode: Boolean = false,
    val selectedItems: Set<Long> = emptySet(),
    val shouldExit: Boolean = false
)

/**
 * ViewModel for the Home screen.
 *
 * Manages the data and logic for displaying and interacting with lumina items
 * based on the currently active profile.
 *
 * @property repository Repository for lumina data operations.
 * @property profileManager Manager for profile-related state.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LuminaRepository,
    private val profileManager: ProfileManager
) : ViewModel() {

    // Private, mutable state
    private val _uiState = MutableStateFlow(HomeUiState())
    /**
     * Public, read-only state for the UI to observe.
     */
    val uiState = _uiState.asStateFlow()

    private var autoShutdownJob: Job? = null

    init {
        // Load the initial data when the ViewModel is created.
        loadLuminaItems()
    }

    /**
     * Called when the app goes to the background.
     * Starts a 2-minute timer to shut down the app if it remains in the background.
     */
    fun onAppBackgrounded() {
        android.util.Log.d("Lumina-Shutdown", "App backgrounded. Starting 2-minute auto-shutdown timer...")
        autoShutdownJob?.cancel()
        autoShutdownJob = viewModelScope.launch {
            // Wait for 2 minutes
            delay(2 * 60 * 1000)
            android.util.Log.d("Lumina-Shutdown", "2 minutes reached. Triggering shutdown.")
            _uiState.update { it.copy(shouldExit = true) }
        }
    }

    /**
     * Called when the app returns to the foreground.
     * Cancels the auto-shutdown timer.
     */
    fun onAppForegrounded() {
        android.util.Log.d("Lumina-Shutdown", "App foregrounded. Cancelling auto-shutdown timer.")
        autoShutdownJob?.cancel()
        autoShutdownJob = null
    }

    /**
     * Loads lumina items for the current profile and updates the UI state.
     */
    private fun loadLuminaItems() {
        profileManager.getCurrentProfileId()
            .flatMapLatest { profileId ->
                if (profileId != null) {
                    repository.getAllLuminas(profileId)
                } else {
                    kotlinx.coroutines.flow.flowOf(emptyList())
                }
            }
            .onEach { items ->
                _uiState.update { it.copy(luminaItems = items) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Toggles the item selection mode on or off.
     */
    fun toggleSelectionMode() {
        _uiState.update { it.copy(selectionMode = !it.selectionMode, selectedItems = emptySet()) }
    }

    /**
     * Toggles the selection state of a specific item.
     *
     * @param itemId The ID of the item to toggle.
     */
    fun toggleItemSelection(itemId: Long) {
        _uiState.update {
            val selectedItems = it.selectedItems.toMutableSet()
            if (selectedItems.contains(itemId)) {
                selectedItems.remove(itemId)
            } else {
                selectedItems.add(itemId)
            }
            it.copy(selectedItems = selectedItems)
        }
    }

    /**
     * Deletes all currently selected items from the repository.
     */
    fun deleteSelectedItems() {
        viewModelScope.launch {
            repository.deleteLuminasByIds(_uiState.value.selectedItems.toList())
            toggleSelectionMode()
        }
    }
}
