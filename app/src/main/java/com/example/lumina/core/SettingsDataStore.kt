package com.example.lumina.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mozilla.geckoview.GeckoRuntimeSettings
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    val dnsProviderKey = stringPreferencesKey("dns_provider")
    val customDnsUriKey = stringPreferencesKey("custom_dns_uri")
    val searchEngineKey = stringPreferencesKey("search_engine")
    val webContentIsolationStrategyKey = intPreferencesKey("web_content_isolation_strategy")
    val autoCloseTimeoutKey = longPreferencesKey("auto_close_timeout") // Timeout in minutes, 0 for Never
    
    val safeBrowsingEnabledKey = booleanPreferencesKey("safe_browsing_enabled")
    val localPhishingModelEnabledKey = booleanPreferencesKey("local_phishing_model_enabled")

    val torEnabledKey = booleanPreferencesKey("tor_enabled")
    val torProfileKey = stringPreferencesKey("tor_profile")

    val dnsProviderFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[dnsProviderKey] ?: "Cloudflare"
        }

    val customDnsUriFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[customDnsUriKey] ?: ""
        }

    val searchEngineFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[searchEngineKey] ?: "Google"
        }

    val webContentIsolationStrategyFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[webContentIsolationStrategyKey] ?: GeckoRuntimeSettings.STRATEGY_ISOLATE_EVERYTHING
        }

    val autoCloseTimeoutFlow: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[autoCloseTimeoutKey] ?: 0L // Default to Never
        }

    val safeBrowsingEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[safeBrowsingEnabledKey] ?: true
        }

    val localPhishingModelEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[localPhishingModelEnabledKey] ?: true
        }

    val torEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[torEnabledKey] ?: false
        }

    val torProfileFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[torProfileKey] ?: "Standard"
        }

    val data: Flow<Preferences> = context.dataStore.data

    suspend fun saveDnsProvider(dnsProvider: String) {
        context.dataStore.edit { settings ->
            settings[dnsProviderKey] = dnsProvider
        }
    }

    suspend fun saveCustomDnsUri(uri: String) {
        context.dataStore.edit { settings ->
            settings[customDnsUriKey] = uri
        }
    }

    suspend fun saveSearchEngine(searchEngine: String) {
        context.dataStore.edit { settings ->
            settings[searchEngineKey] = searchEngine
        }
    }

    suspend fun saveWebContentIsolationStrategy(strategy: Int) {
        context.dataStore.edit { settings ->
            settings[webContentIsolationStrategyKey] = strategy
        }
    }

    suspend fun saveAutoCloseTimeout(timeoutMinutes: Long) {
        context.dataStore.edit { settings ->
            settings[autoCloseTimeoutKey] = timeoutMinutes
        }
    }

    suspend fun saveSafeBrowsingEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[safeBrowsingEnabledKey] = enabled
        }
    }

    suspend fun saveLocalPhishingModelEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[localPhishingModelEnabledKey] = enabled
        }
    }

    suspend fun saveTorEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[torEnabledKey] = enabled
        }
    }

    suspend fun saveTorProfile(profile: String) {
        context.dataStore.edit { settings ->
            settings[torProfileKey] = profile
        }
    }
}
