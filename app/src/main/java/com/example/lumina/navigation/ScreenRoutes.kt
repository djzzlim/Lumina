package com.example.lumina.navigation

/**
 * Constants defining the navigation routes and arguments for the application.
 */
object ScreenRoutes {
    /** Route for the main home screen. */
    const val HOME = "home_screen"

    /** Route for the QR code scanner screen. */
    const val QR_SCANNER = "qr_code_scanner_screen"

    /** Base route for the new lumina creation screen. */
    const val NEW_LUMINA_BASE = "new_lumina_screen"

    /** Argument key for passing a URL to the new lumina screen. */
    const val NEW_LUMINA_URL_ARG = "url"

    /** Combined route for new lumina with an optional URL argument. */
    const val NEW_LUMINA_ROUTE = "$NEW_LUMINA_BASE?$NEW_LUMINA_URL_ARG={$NEW_LUMINA_URL_ARG}"

    /** Route for the advanced options configuration screen. */
    const val ADVANCED_OPTIONS = "advanced_options_screen"

    /** Route for the nested navigation graph for creating a new lumina. */
    const val NEW_LUMINA_GRAPH = "new_lumina_graph"

    /** Base route for the browser screen. */
    const val BROWSER_BASE = "browser_screen"

    /** Argument key for the lumina ID to be opened in the browser. */
    const val BROWSER_ID_ARG = "luminaId"

    /** Combined route for the browser with a required ID argument. */
    const val BROWSER_ROUTE = "$BROWSER_BASE/{$BROWSER_ID_ARG}"

    /** Route for the profile management screen. */
    const val PROFILES_SCREEN = "profiles_screen"

    /** Base route for editing an existing lumina. */
    const val EDIT_LUMINA_BASE = "edit_lumina_screen"

    /** Argument key for the lumina ID to be edited. */
    const val EDIT_LUMINA_ID_ARG = "luminaId"

    /** Combined route for editing a lumina with a required ID argument. */
    const val EDIT_LUMINA_ROUTE = "$EDIT_LUMINA_BASE/{$EDIT_LUMINA_ID_ARG}"

    /** Route for the nested navigation graph for editing a lumina. */
    const val EDIT_LUMINA_GRAPH = "edit_lumina_graph"

    /** Route for edit advanced options screen. */
    const val EDIT_ADVANCED_OPTIONS = "edit_advanced_options_screen"
}
