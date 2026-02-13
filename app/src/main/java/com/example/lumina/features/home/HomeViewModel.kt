package com.example.lumina.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumina.core.AppPreferences
import com.example.lumina.core.LuminaRepository
import com.example.lumina.core.ProfileManager
import com.example.lumina.core.database.LuminaInfo
import com.example.lumina.core.tor.TorManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
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
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LuminaRepository,
    private val profileManager: ProfileManager,
    private val appPreferences: AppPreferences,
    private val torManager: TorManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    val torEnabled: StateFlow<Boolean> = appPreferences.torEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val torProgress: StateFlow<Int> = torManager.bootstrappingProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private var autoShutdownJob: Job? = null
    private var shouldAutoShutdownOnBackground: Boolean = true

    init {
        loadLuminaItems()
    }

    /**
     * Sets whether the app should auto-shutdown when backgrounded.
     */
    fun setShouldAutoShutdownOnBackground(shouldShutdown: Boolean) {
        shouldAutoShutdownOnBackground = shouldShutdown
    }

    /**
     * Called when the app goes to the background.
     * Starts a timer to shut down the app based on the "Auto Close Inactive Tabs" setting.
     */
    fun onAppBackgrounded() {
        if (!shouldAutoShutdownOnBackground) {
            android.util.Log.d("Lumina-Shutdown", "App backgrounded, but auto-shutdown is disabled for this screen.")
            return
        }

        autoShutdownJob?.cancel()
        autoShutdownJob = viewModelScope.launch {
            val timeout = appPreferences.autoCloseTimeoutFlow.first()
            if (timeout.minutes > 0) {
                android.util.Log.d("Lumina-Shutdown", "App backgrounded. Starting ${timeout.minutes}-minute auto-shutdown timer...")
                delay(timeout.minutes * 60 * 1000)
                android.util.Log.d("Lumina-Shutdown", "Auto-close timeout reached. Triggering global shutdown.")
                _uiState.update { it.copy(shouldExit = true) }
            } else {
                android.util.Log.d("Lumina-Shutdown", "App backgrounded. Auto-shutdown is set to 'Never'.")
            }
        }
    }

    /**
     * Called when the app returns to the foreground.
     */
    fun onAppForegrounded() {
        if (autoShutdownJob != null) {
            android.util.Log.d("Lumina-Shutdown", "App foregrounded. Cancelling auto-shutdown timer.")
            autoShutdownJob?.cancel()
            autoShutdownJob = null
        }
    }

    private fun loadLuminaItems() {
        profileManager.getCurrentProfileId()
            .flatMapLatest { profileId ->
                if (profileId != null) {
                    repository.getAllLuminas(profileId)
                } else {
                    flowOf(emptyList())
                }
            }
            .onEach { items ->
                _uiState.update { it.copy(luminaItems = items) }
            }
            .launchIn(viewModelScope)
    }

    fun toggleSelectionMode() {
        _uiState.update { it.copy(selectionMode = !it.selectionMode, selectedItems = emptySet()) }
    }

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

    fun deleteSelectedItems() {
        viewModelScope.launch {
            repository.deleteLuminasByIds(_uiState.value.selectedItems.toList())
            toggleSelectionMode()
        }
    }
}
