package com.example.lumina.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    val dnsProviderFlow: Flow<DnsProvider> = settingsDataStore.data
        .map { preferences ->
            when (preferences[settingsDataStore.dnsProviderKey]) {
                "Cloudflare" -> DnsProvider.Cloudflare
                "Google" -> DnsProvider.Google
                "AdGuard" -> DnsProvider.AdGuard
                "Quad9" -> DnsProvider.Quad9
                else -> DnsProvider.Cloudflare
            }
        }

    val searchEngineFlow: Flow<SearchEngine> = settingsDataStore.data
        .map { preferences ->
            when (preferences[settingsDataStore.searchEngineKey]) {
                "Google" -> SearchEngine.Google
                "DuckDuckGo" -> SearchEngine.DuckDuckGo
                "Brave Search" -> SearchEngine.Brave
                else -> SearchEngine.Google
            }
        }

    val isolationStrategyFlow: Flow<Int> = settingsDataStore.webContentIsolationStrategyFlow
}

sealed class DnsProvider(val uri: String, val mode: Int) {
    object Cloudflare : DnsProvider("https://cloudflare-dns.com/dns-query", 2)
    object Google : DnsProvider("https://dns.google/dns-query", 2)
    object AdGuard : DnsProvider("https://dns.adguard.com/dns-query", 2)
    object Quad9 : DnsProvider("https://dns.quad9.net/dns-query", 2)
}

sealed class SearchEngine(val url: String) {
    object Google : SearchEngine("https://www.google.com/search?q=")
    object DuckDuckGo : SearchEngine("https://duckduckgo.com/?q=")
    object Brave : SearchEngine("https://search.brave.com/search?q=")
}
