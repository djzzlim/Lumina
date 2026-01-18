package com.example.lumina.navigation

// This object holds all the unique route strings for your app's navigation.
object ScreenRoutes {
    const val HOME = "home_screen"
    const val QR_SCANNER = "qr_code_scanner_screen"

    // This defines the base route and the optional argument for NewLuminaScreen
    const val NEW_LUMINA_BASE = "new_lumina_screen"
    const val NEW_LUMINA_URL_ARG = "url"
    const val NEW_LUMINA_ROUTE = "$NEW_LUMINA_BASE?$NEW_LUMINA_URL_ARG={$NEW_LUMINA_URL_ARG}"

    const val ADVANCED_OPTIONS = "advanced_options_screen"

    const val NEW_LUMINA_GRAPH = "new_lumina_graph"

    const val BROWSER_BASE = "browser_screen"
    const val BROWSER_ID_ARG = "luminaId"
    const val BROWSER_ROUTE = "$BROWSER_BASE/{$BROWSER_ID_ARG}"

    const val PROFILES_SCREEN = "profiles_screen"
}
