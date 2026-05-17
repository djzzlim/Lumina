package com.example.lumina.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A high-level preferences provider that combines raw settings from [SettingsDataStore]
 * into domain-specific objects like [DnsProvider] and [SearchEngine].
 *
 * This class acts as a bridge between the persistence layer and the UI/Logic layers,
 * providing reactive [Flow]s of configuration objects.
 */
@Singleton
class AppPreferences @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    /** A [Flow] of the resolved [DnsProvider], combining selection and custom URI. */
    val dnsProviderFlow: Flow<DnsProvider> = combine(
        settingsDataStore.data,
        settingsDataStore.customDnsUriFlow
    ) { preferences, customUri ->
        val providerName = preferences[settingsDataStore.dnsProviderKey] ?: DnsProvider.Cloudflare.name
        DnsProvider.fromName(providerName, customUri)
    }

    /** A [Flow] of the resolved [SearchEngine]. */
    val searchEngineFlow: Flow<SearchEngine> = settingsDataStore.data
        .map { preferences ->
            val engineName = preferences[settingsDataStore.searchEngineKey] ?: SearchEngine.Google.name
            SearchEngine.fromName(engineName)
        }

    /** A [Flow] of the web content isolation strategy constant. */
    val isolationStrategyFlow: Flow<Int> = settingsDataStore.webContentIsolationStrategyFlow

    /** A [Flow] of the resolved [AutoCloseTimeout] object. */
    val autoCloseTimeoutFlow: Flow<AutoCloseTimeout> = settingsDataStore.autoCloseTimeoutFlow
        .map { minutes ->
            AutoCloseTimeout.fromMinutes(minutes)
        }

    /** A [Flow] indicating if Google Safe Browsing is enabled. */
    val safeBrowsingEnabledFlow: Flow<Boolean> = settingsDataStore.safeBrowsingEnabledFlow
    /** A [Flow] indicating if the local AI phishing model is enabled. */
    val localPhishingModelEnabledFlow: Flow<Boolean> = settingsDataStore.localPhishingModelEnabledFlow
    /** A [Flow] indicating if Tor is globally enabled. */
    val torEnabledFlow: Flow<Boolean> = settingsDataStore.torEnabledFlow
    /** A [Flow] of the current Tor profile name. */
    val torProfileFlow: Flow<String> = settingsDataStore.torProfileFlow

    /** A [Flow] of the last exit timestamp. */
    val lastExitTimeFlow: Flow<Long> = settingsDataStore.lastExitTimeFlow

    /** Saves the current time as the last exit time. */
    suspend fun saveLastExitTime(timestamp: Long) {
        settingsDataStore.saveLastExitTime(timestamp)
    }
}

/**
 * Represents a DNS provider configuration.
 *
 * @property name The display name of the provider.
 * @property uri The DNS-over-HTTPS endpoint URI.
 * @property mode The Gecko TRR (Trusted Recursive Resolver) mode.
 */
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

/**
 * Represents a search engine configuration.
 *
 * @property name The display name of the search engine.
 * @property url The base search URL with a query parameter placeholder.
 */
sealed class SearchEngine(val name: String, val url: String) {
    object Google : SearchEngine("Google", "https://www.google.com/search?q=")
    object DuckDuckGo : SearchEngine("DuckDuckGo", "https://duckduckgo.com/?q=")
    object Brave : SearchEngine("Brave Search", "https://search.brave.com/search?q=")

    companion object {
        val allOptions = listOf("Google", "DuckDuckGo", "Brave Search")

        /** Creates a [SearchEngine] from a name string. Defaults to Google. */
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

/**
 * Represents a timeout duration for auto-closing inactive tabs.
 *
 * @property name The display name of the timeout option.
 * @property minutes The duration in minutes. 0 indicates 'Never'.
 */
sealed class AutoCloseTimeout(val name: String, val minutes: Long) {
    object Never : AutoCloseTimeout("Never", 0L)
    object OneMinute : AutoCloseTimeout("1 Minute", 1L)
    object TwoMinutes : AutoCloseTimeout("2 Minutes", 2L)
    object FiveMinutes : AutoCloseTimeout("5 Minutes", 5L)
    object TenMinutes : AutoCloseTimeout("10 Minutes", 10L)

    companion object {
        val allOptions = listOf(Never, OneMinute, TwoMinutes, FiveMinutes, TenMinutes)
        
        fun fromMinutes(minutes: Long): AutoCloseTimeout {
            return allOptions.find { it.minutes == minutes } ?: 
                   if (minutes > 0 && minutes <= 10) TenMinutes else Never
        }
    }
}
