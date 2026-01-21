package com.example.lumina.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    val dnsProviderKey = stringPreferencesKey("dns_provider")
    val searchEngineKey = stringPreferencesKey("search_engine")

    val dnsProviderFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[dnsProviderKey] ?: "Cloudflare"
        }

    val searchEngineFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[searchEngineKey] ?: "Google"
        }

    val data: Flow<Preferences> = context.dataStore.data

    suspend fun saveDnsProvider(dnsProvider: String) {
        context.dataStore.edit { settings ->
            settings[dnsProviderKey] = dnsProvider
        }
    }

    suspend fun saveSearchEngine(searchEngine: String) {
        context.dataStore.edit { settings ->
            settings[searchEngineKey] = searchEngine
        }
    }
}
