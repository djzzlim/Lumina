package com.example.lumina.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    val dnsProviderFlow: Flow<DnsProvider> = combine(
        settingsDataStore.data,
        settingsDataStore.customDnsUriFlow
    ) { preferences, customUri ->
        val providerName = preferences[settingsDataStore.dnsProviderKey] ?: DnsProvider.Cloudflare.name
        DnsProvider.fromName(providerName, customUri)
    }

    val searchEngineFlow: Flow<SearchEngine> = settingsDataStore.data
        .map { preferences ->
            val engineName = preferences[settingsDataStore.searchEngineKey] ?: SearchEngine.Google.name
            SearchEngine.fromName(engineName)
        }

    val isolationStrategyFlow: Flow<Int> = settingsDataStore.webContentIsolationStrategyFlow

    val autoCloseTimeoutFlow: Flow<AutoCloseTimeout> = settingsDataStore.autoCloseTimeoutFlow
        .map { minutes ->
            AutoCloseTimeout.fromMinutes(minutes)
        }

    val safeBrowsingEnabledFlow: Flow<Boolean> = settingsDataStore.safeBrowsingEnabledFlow
    val localPhishingModelEnabledFlow: Flow<Boolean> = settingsDataStore.localPhishingModelEnabledFlow
}

sealed class DnsProvider(val name: String, val uri: String, val mode: Int) {
    object Cloudflare : DnsProvider("Cloudflare", "https://cloudflare-dns.com/dns-query", 2)
    object Google : DnsProvider("Google", "https://dns.google/dns-query", 2)
    object AdGuard : DnsProvider("AdGuard", "https://dns.adguard.com/dns-query", 2)
    object Quad9 : DnsProvider("Quad9", "https://dns.quad9.net/dns-query", 2)
    object System : DnsProvider("System", "", 0) // TRR_MODE_OFF
    data class Custom(val customUri: String) : DnsProvider("Custom", customUri, 2)

    companion object {
        val allOptions = listOf("Cloudflare", "Google", "AdGuard", "Quad9", "System", "Custom")
        
        fun fromName(name: String, customUri: String = ""): DnsProvider {
            return when (name) {
                "Cloudflare" -> Cloudflare
                "Google" -> Google
                "AdGuard" -> AdGuard
                "Quad9" -> Quad9
                "System" -> System
                "Custom" -> Custom(customUri)
                else -> Cloudflare
            }
        }
    }
}

sealed class SearchEngine(val name: String, val url: String) {
    object Google : SearchEngine("Google", "https://www.google.com/search?q=")
    object DuckDuckGo : SearchEngine("DuckDuckGo", "https://duckduckgo.com/?q=")
    object Brave : SearchEngine("Brave Search", "https://search.brave.com/search?q=")

    companion object {
        val allOptions = listOf("Google", "DuckDuckGo", "Brave Search")

        fun fromName(name: String): SearchEngine {
            return when (name) {
                "Google" -> Google
                "DuckDuckGo" -> DuckDuckGo
                "Brave Search" -> Brave
                else -> Google
            }
        }
    }
}

sealed class AutoCloseTimeout(val name: String, val minutes: Long) {
    object Never : AutoCloseTimeout("Never", 0L)
    object OneMinute : AutoCloseTimeout("1 Minute", 1L)
    object TwoMinutes : AutoCloseTimeout("2 Minutes", 2L)
    object FiveMinutes : AutoCloseTimeout("5 Minutes", 5L)
    object ThirtyMinutes : AutoCloseTimeout("30 Minutes", 30L)
    object OneHour : AutoCloseTimeout("1 Hour", 60L)

    companion object {
        val allOptions = listOf(Never, OneMinute, TwoMinutes, FiveMinutes, ThirtyMinutes, OneHour)
        
        fun fromMinutes(minutes: Long): AutoCloseTimeout {
            return allOptions.find { it.minutes == minutes } ?: Never
        }
    }
}
