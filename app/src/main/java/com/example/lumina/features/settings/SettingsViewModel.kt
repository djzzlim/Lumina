package com.example.lumina.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumina.core.AutoCloseTimeout
import com.example.lumina.core.DnsProvider
import com.example.lumina.core.SearchEngine
import com.example.lumina.core.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val dnsProvider: StateFlow<String> = settingsDataStore.dnsProviderFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Cloudflare"
        )

    val customDnsUri: StateFlow<String> = settingsDataStore.customDnsUriFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val searchEngine: StateFlow<String> = settingsDataStore.searchEngineFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Google"
        )

    val autoCloseTimeout: StateFlow<Long> = settingsDataStore.autoCloseTimeoutFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    val dnsOptions = DnsProvider.allOptions
    val searchEngineOptions = SearchEngine.allOptions
    val autoCloseOptions = AutoCloseTimeout.allOptions.map { it.name }

    fun setDnsProvider(dnsProvider: String) {
        viewModelScope.launch {
            settingsDataStore.saveDnsProvider(dnsProvider)
        }
    }

    fun setCustomDnsUri(uri: String) {
        viewModelScope.launch {
            settingsDataStore.saveCustomDnsUri(uri)
        }
    }

    fun setSearchEngine(searchEngine: String) {
        viewModelScope.launch {
            settingsDataStore.saveSearchEngine(searchEngine)
        }
    }

    fun setAutoCloseTimeout(name: String) {
        val minutes = AutoCloseTimeout.allOptions.find { it.name == name }?.minutes ?: 0L
        viewModelScope.launch {
            settingsDataStore.saveAutoCloseTimeout(minutes)
        }
    }
}
