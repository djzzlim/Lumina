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

/**
 * Manages the persistence and retrieval of application-wide settings using Jetpack DataStore.
 * This class provides a reactive [Flow]-based API for accessing settings and suspend functions
 * for modifying them.
 */
class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    val dnsProviderKey = stringPreferencesKey("dns_provider")
    val customDnsUriKey = stringPreferencesKey("custom_dns_uri")
    val searchEngineKey = stringPreferencesKey("search_engine")
    val webContentIsolationStrategyKey = intPreferencesKey("web_content_isolation_strategy")
    /** Key for the auto-close timeout setting in minutes. 0 indicates 'Never'. */
    val autoCloseTimeoutKey = longPreferencesKey("auto_close_timeout") 
    
    val safeBrowsingEnabledKey = booleanPreferencesKey("safe_browsing_enabled")
    val localPhishingModelEnabledKey = booleanPreferencesKey("local_phishing_model_enabled")

    val torEnabledKey = booleanPreferencesKey("tor_enabled")
    val torProfileKey = stringPreferencesKey("tor_profile")
    val useNetworkTimezoneKey = booleanPreferencesKey("use_network_timezone")
    val lastExitTimeKey = longPreferencesKey("last_exit_time")

    /** A [Flow] of the selected DNS provider name. */
    val dnsProviderFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[dnsProviderKey] ?: "Cloudflare"
        }

    /** A [Flow] of the custom DNS URI. */
    val customDnsUriFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[customDnsUriKey] ?: ""
        }

    /** A [Flow] of the selected search engine name. */
    val searchEngineFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[searchEngineKey] ?: "Google"
        }

    /** A [Flow] of the web content isolation strategy constant. */
    val webContentIsolationStrategyFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[webContentIsolationStrategyKey] ?: GeckoRuntimeSettings.STRATEGY_ISOLATE_EVERYTHING
        }

    /** A [Flow] of the auto-close timeout in minutes. */
    val autoCloseTimeoutFlow: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[autoCloseTimeoutKey] ?: 0L // Default to Never
        }

    /** A [Flow] indicating if Google Safe Browsing is enabled. */
    val safeBrowsingEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[safeBrowsingEnabledKey] ?: true
        }

    /** A [Flow] indicating if the local AI phishing model is enabled. */
    val localPhishingModelEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[localPhishingModelEnabledKey] ?: true
        }

    /** A [Flow] indicating if Tor is globally enabled. */
    val torEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[torEnabledKey] ?: false
        }

    /** A [Flow] of the current Tor profile (e.g., "Standard", "Safer", "Safest"). */
    val torProfileFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[torProfileKey] ?: "Standard"
        }

    /** A [Flow] indicating if the network (VPN/Tor) should be used for timezone detection. */
    val useNetworkTimezoneFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[useNetworkTimezoneKey] ?: true
        }

    /** Access to the raw [Preferences] flow. */
    val data: Flow<Preferences> = context.dataStore.data

    /** Saves the selected DNS provider. */
    suspend fun saveDnsProvider(dnsProvider: String) {
        context.dataStore.edit { settings ->
            settings[dnsProviderKey] = dnsProvider
        }
    }

    /** Saves the custom DNS URI. */
    suspend fun saveCustomDnsUri(uri: String) {
        context.dataStore.edit { settings ->
            settings[customDnsUriKey] = uri
        }
    }

    /** Saves the selected search engine. */
    suspend fun saveSearchEngine(searchEngine: String) {
        context.dataStore.edit { settings ->
            settings[searchEngineKey] = searchEngine
        }
    }

    /** Saves the web content isolation strategy. */
    suspend fun saveWebContentIsolationStrategy(strategy: Int) {
        context.dataStore.edit { settings ->
            settings[webContentIsolationStrategyKey] = strategy
        }
    }

    /** Saves the auto-close timeout in minutes. */
    suspend fun saveAutoCloseTimeout(timeoutMinutes: Long) {
        context.dataStore.edit { settings ->
            settings[autoCloseTimeoutKey] = timeoutMinutes
        }
    }

    /** Saves whether Google Safe Browsing is enabled. */
    suspend fun saveSafeBrowsingEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[safeBrowsingEnabledKey] = enabled
        }
    }

    /** Saves whether the local AI phishing model is enabled. */
    suspend fun saveLocalPhishingModelEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[localPhishingModelEnabledKey] = enabled
        }
    }

    /** Saves whether Tor is globally enabled. */
    suspend fun saveTorEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[torEnabledKey] = enabled
        }
    }

    /** Saves the selected Tor profile. */
    suspend fun saveTorProfile(profile: String) {
        context.dataStore.edit { settings ->
            settings[torProfileKey] = profile
        }
    }

    /** Saves whether to use the network/VPN for timezone detection. */
    suspend fun saveUseNetworkTimezone(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[useNetworkTimezoneKey] = enabled
        }
    }

    /** Saves the timestamp of when the app was last exited/backgrounded. */
    suspend fun saveLastExitTime(timestamp: Long) {
        context.dataStore.edit { settings ->
            settings[lastExitTimeKey] = timestamp
        }
    }

    /** Retrieves the last recorded exit time. */
    val lastExitTimeFlow: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[lastExitTimeKey] ?: 0L
        }
}

