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
