package com.example.lumina.navigation

object ScreenRoutes {
    const val HOME = "home"
    const val SETTINGS_SCREEN = "settings"
    const val PROFILES_SCREEN = "profiles"
    const val QR_SCANNER = "qr_scanner"
    const val TOR_SETTINGS = "tor_settings"

    const val NEW_LUMINA_GRAPH = "new_lumina_graph"
    const val NEW_LUMINA_BASE = "new_lumina"
    const val NEW_LUMINA_URL_ARG = "url"
    const val NEW_LUMINA_ROUTE = "$NEW_LUMINA_BASE?$NEW_LUMINA_URL_ARG={$NEW_LUMINA_URL_ARG}"
    
    const val ADVANCED_OPTIONS = "advanced_options"

    const val EDIT_LUMINA_GRAPH = "edit_lumina_graph"
    const val EDIT_LUMINA_BASE = "edit_lumina"
    const val EDIT_LUMINA_ID_ARG = "id"
    const val EDIT_LUMINA_ROUTE = "$EDIT_LUMINA_BASE/{$EDIT_LUMINA_ID_ARG}"
    
    const val EDIT_ADVANCED_OPTIONS = "edit_advanced_options"
    
    const val BROWSER_BASE = "browser"
    const val BROWSER_ID_ARG = "id"
    const val BROWSER_ROUTE = "$BROWSER_BASE/{$BROWSER_ID_ARG}"
    const val EXTENSIONS_SCREEN = "extensions"
}
