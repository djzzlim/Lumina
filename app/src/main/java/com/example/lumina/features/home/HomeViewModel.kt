package com.example.lumina.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumina.core.LuminaRepository
import com.example.lumina.core.data.LuminaInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// The state for the home screen, containing the list of items.
data class HomeUiState(
    val luminaItems: List<LuminaInfo> = emptyList(),
    val selectionMode: Boolean = false,
    val selectedItems: Set<Long> = emptySet()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LuminaRepository
) : ViewModel() {

    // Private, mutable state
    private val _uiState = MutableStateFlow(HomeUiState())
    // Public, read-only state for the UI to observe
    val uiState = _uiState.asStateFlow()

    init {
        // Load the initial data when the ViewModel is created.
        loadLuminaItems()
    }

    private fun loadLuminaItems() {
        repository.getAllLuminas()
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
