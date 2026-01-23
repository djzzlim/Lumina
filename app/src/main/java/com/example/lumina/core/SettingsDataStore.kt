package com.example.lumina.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
}
