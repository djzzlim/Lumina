package com.example.lumina.features.browser

/**
 * Represents a "bang" search shortcut.
 *
 * Bangs allow users to quickly search specific sites by prefixing their query
 * with a trigger (e.g., "!g " for Google).
 *
 * @property trigger The string that activates the bang (e.g., "!g").
 * @property urlTemplate The URL template for the search, where "%s" is replaced by the query.
 */
data class Bang(
    val trigger: String,
    val urlTemplate: String
)
