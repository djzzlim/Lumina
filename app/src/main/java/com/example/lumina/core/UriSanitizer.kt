package com.example.lumina.core

import android.util.Log

/**
 * UriSanitizer performs a fast, synchronous blocklist check on any URI before it is
 * passed to GeckoView. It defends against several injection/escalation attack vectors:
 *
 *  1. **Dangerous scheme injection** – blocks `javascript:`, `intent:`, `android-app:`,
 *     `jar:`, and `vbscript:` URIs that could execute arbitrary code or trigger
 *     Android system actions.
 *
 *  2. **Dangerous `data:` URIs** – blocks `data:` URIs whose content-type can execute
 *     scripts (e.g. `text/html`, `image/svg+xml`). Plain data types (images, text) are
 *     allowed.
 *
 *  3. **Private file access** – blocks `file://` URIs that point into app-private data
 *     directories (`/data/data/`, `/data/user/`), preventing database or credential
 *     exfiltration. Extension loading from internal files is unaffected because those
 *     are loaded via the WebExtension API, not via `loadUri()`.
 *
 *  4. **Null-byte injection** – blocks URIs containing a literal null byte (`\u0000`)
 *     or its percent-encoded form (`%00`), which can be used to truncate paths and
 *     bypass host-matching checks.
 */
object UriSanitizer {

    private const val TAG = "UriSanitizer"

    /**
     * Schemes that must never be navigated to by the user-facing browser.
     * These can execute code, trigger OS-level intents, or access the Java class-loader.
     */
    private val BLOCKED_SCHEMES = setOf(
        "javascript",
        "intent",
        "android-app",
        "jar",
        "vbscript"
    )

    /**
     * `data:` content-types that are capable of running scripts or parsing markup.
     * Plain data types (e.g. `image/png`, `text/plain`) are not blocked.
     */
    private val DANGEROUS_DATA_MIME_PREFIXES = listOf(
        "text/html",
        "text/xml",
        "application/xhtml+xml",
        "application/xml",
        "image/svg+xml",    // SVG documents can embed <script> elements
        "application/javascript",
        "text/javascript"
    )

    /**
     * Path prefixes within `file://` URIs that expose app-private data.
     * Blocking these prevents attackers from reading databases, SharedPreferences,
     * or cached credentials via a crafted URL.
     */
    private val PRIVATE_PATH_PREFIXES = listOf(
        "/data/data/",
        "/data/user/",
        "/sdcard/Android/data/",
        "/sdcard/Android/obb/"
    )

    /** Reason a URI was blocked, for logging and UI messaging. */
    enum class BlockReason {
        DANGEROUS_SCHEME,
        DANGEROUS_DATA_URI,
        PRIVATE_FILE_ACCESS,
        NULL_BYTE_INJECTION
    }

    /** Result of a [check] call. */
    data class Result(val isBlocked: Boolean, val reason: BlockReason? = null)

    private val SAFE = Result(isBlocked = false)

    /**
     * Checks [uri] against the blocklist.
     *
     * @param uri The raw URI string to inspect.
     * @return [Result.isBlocked] `true` if the URI should be denied; `false` if it is safe
     *         to pass to GeckoView.
     */
    fun check(uri: String): Result {
        // ── 1. Null-byte injection ────────────────────────────────────────────────
        // A null byte can truncate the URI in C-based parsers, allowing
        // "https://safe.com%00.evil.com" to be seen as "https://safe.com" by one
        // layer but routed to "evil.com" by another.
        if (uri.contains('\u0000') || uri.contains("%00", ignoreCase = true)) {
            Log.w(TAG, "Blocked null-byte injection in URI: ${uri.take(120)}")
            return Result(isBlocked = true, reason = BlockReason.NULL_BYTE_INJECTION)
        }

        // Extract the scheme robustly (before the first ':')
        val schemeEnd = uri.indexOf(':')
        val scheme = if (schemeEnd > 0) uri.substring(0, schemeEnd).trim().lowercase() else ""

        // ── 2. Blocked schemes ────────────────────────────────────────────────────
        if (scheme in BLOCKED_SCHEMES) {
            Log.w(TAG, "Blocked dangerous scheme '$scheme': ${uri.take(120)}")
            return Result(isBlocked = true, reason = BlockReason.DANGEROUS_SCHEME)
        }

        // ── 3. Dangerous data: URIs ───────────────────────────────────────────────
        // data:[<mediatype>][;base64],<data>
        // We inspect the declared media-type to decide whether it can run scripts.
        if (scheme == "data") {
            val afterScheme = uri.substringAfter("data:").lowercase()
            // Strip optional ;base64 and everything after the comma (the payload)
            val mediaType = afterScheme.substringBefore(",").substringBefore(";").trim()
            if (DANGEROUS_DATA_MIME_PREFIXES.any { mediaType.startsWith(it) }) {
                Log.w(TAG, "Blocked dangerous data: URI (type='$mediaType'): ${uri.take(120)}")
                return Result(isBlocked = true, reason = BlockReason.DANGEROUS_DATA_URI)
            }
        }

        // ── 4. Private file:// access ─────────────────────────────────────────────
        // Extensions are loaded through the WebExtension API (not via loadUri), so
        // blocking file:// here does not affect extension functionality.
        if (scheme == "file") {
            val path = try {
                android.net.Uri.parse(uri).path ?: ""
            } catch (_: Exception) { "" }
            if (PRIVATE_PATH_PREFIXES.any { path.startsWith(it) }) {
                Log.w(TAG, "Blocked private file access: ${uri.take(120)}")
                return Result(isBlocked = true, reason = BlockReason.PRIVATE_FILE_ACCESS)
            }
        }

        return SAFE
    }
}
