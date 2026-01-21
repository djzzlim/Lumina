package com.example.lumina.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lumina.features.advanced_options.AdvancedOptionsScreen
import com.example.lumina.features.browser.BrowserScreen
import com.example.lumina.features.edit_lumina.EditLuminaScreen
import com.example.lumina.features.edit_lumina.EditLuminaViewModel
import com.example.lumina.features.home.HomeViewModel
import com.example.lumina.features.home.LuminaHomeScreen
import com.example.lumina.features.new_lumina.NewLuminaScreen
import com.example.lumina.features.new_lumina.NewLuminaViewModel
import com.example.lumina.features.profiles.ProfilesScreen
import com.example.lumina.features.qr_scanner.QRCodeScannerScreen
import com.example.lumina.features.settings.SettingsScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation(
    startUrl: String? = null,
    onUrlHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    LaunchedEffect(startUrl) {
        if (startUrl != null) {
            val encodedUrl = URLEncoder.encode(
                startUrl,
                StandardCharsets.UTF_8.toString()
            )
            navController.navigate(
                "${ScreenRoutes.NEW_LUMINA_BASE}?${ScreenRoutes.NEW_LUMINA_URL_ARG}=$encodedUrl"
            )
            onUrlHandled()
        }
    }

    fun safeNavigate(route: String) {
        if (navController.currentDestination?.route != route) {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    val slideDuration = 300

    val defaultEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(slideDuration)
        )
    }

    val defaultExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(slideDuration)
        )
    }

    val defaultPopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(slideDuration)
        )
    }

    val defaultPopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(slideDuration)
        )
    }

    NavHost(
        navController = navController,
        startDestination = ScreenRoutes.HOME,
        enterTransition = defaultEnter,
        exitTransition = defaultExit,
        popEnterTransition = defaultPopEnter,
        popExitTransition = defaultPopExit
    ) {

        composable(
            route = ScreenRoutes.HOME,
            exitTransition = {
                if (targetState.destination.route?.startsWith(ScreenRoutes.BROWSER_BASE) == true) {
                    fadeOut(animationSpec = tween(slideDuration))
                } else {
                    defaultExit()
                }
            },
            popEnterTransition = {
                if (initialState.destination.route?.startsWith(ScreenRoutes.BROWSER_BASE) == true) {
                    fadeIn(animationSpec = tween(slideDuration))
                } else {
                    defaultPopEnter()
                }
            }
        ) {
            val vm: HomeViewModel = hiltViewModel()
            LuminaHomeScreen(
                viewModel = vm,
                onNavigateToScanner = {
                    safeNavigate(ScreenRoutes.QR_SCANNER)
                },
                onNavigateToAddLumina = {
                    safeNavigate(ScreenRoutes.NEW_LUMINA_GRAPH)
                },
                onNavigateToBrowser = { luminaId ->
                    safeNavigate("${ScreenRoutes.BROWSER_BASE}/$luminaId")
                },
                onNavigateToEditLumina = { luminaId ->
                    safeNavigate("${ScreenRoutes.EDIT_LUMINA_BASE}/$luminaId")
                },
                onNavigateToProfiles = {
                    safeNavigate(ScreenRoutes.PROFILES_SCREEN)
                },
                onNavigateToSettings = {
                    safeNavigate(ScreenRoutes.SETTINGS_SCREEN)
                }
            )
        }

        composable(ScreenRoutes.SETTINGS_SCREEN) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(ScreenRoutes.PROFILES_SCREEN) {
            ProfilesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(ScreenRoutes.QR_SCANNER) {
            QRCodeScannerScreen(
                onUrlScanned = { scannedUrl ->
                    val encodedUrl = URLEncoder.encode(
                        scannedUrl,
                        StandardCharsets.UTF_8.toString()
                    )

                    navController.navigate(
                        "${ScreenRoutes.NEW_LUMINA_BASE}?${ScreenRoutes.NEW_LUMINA_URL_ARG}=$encodedUrl"
                    ) {
                        popUpTo(ScreenRoutes.QR_SCANNER) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        navigation(
            startDestination = ScreenRoutes.NEW_LUMINA_ROUTE,
            route = ScreenRoutes.NEW_LUMINA_GRAPH
        ) {
            composable(
                route = ScreenRoutes.NEW_LUMINA_ROUTE,
                arguments = listOf(
                    navArgument(ScreenRoutes.NEW_LUMINA_URL_ARG) {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(ScreenRoutes.NEW_LUMINA_GRAPH)
                }
                val vm: NewLuminaViewModel = hiltViewModel(parentEntry)
                val url = backStackEntry.arguments
                    ?.getString(ScreenRoutes.NEW_LUMINA_URL_ARG)

                LaunchedEffect(url) {
                    vm.initializeFromScannedUrl(url)
                }

                NewLuminaScreen(
                    viewModel = vm,
                    onNavigateBack = {
                        navController.navigateUp()
                    },
                    onSaveLumina = {
                        navController.navigateUp()
                    },
                    onNavigateToAdvancedOptions = {
                        safeNavigate(ScreenRoutes.ADVANCED_OPTIONS)
                    }
                )
            }

            composable(ScreenRoutes.ADVANCED_OPTIONS) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(ScreenRoutes.NEW_LUMINA_GRAPH)
                }
                val vm: NewLuminaViewModel = hiltViewModel(parentEntry)
                val uiState by vm.uiState.collectAsState()

                AdvancedOptionsScreen(
                    uiState = uiState,
                    onEphemeralChange = vm::setEphemeral,
                    onWebRtcDisabledChange = vm::setWebRtcDisabled,
                    onAfpEnabledChange = vm::setAfpEnabled,
                    onRandomizeUserAgentChange = vm::setRandomizeUserAgent,
                    onSpoofLocaleChange = vm::setSpoofLocale,
                    onSpoofTimezoneChange = vm::setSpoofTimezone,
                    onRandomizeCanvasChange = vm::setRandomizeCanvas,
                    onDisableAudioContextChange = vm::setDisableAudioContext,
                    onDisableWebGlChange = vm::setDisableWebGl,
                    onRandomizeScreenChange = vm::setRandomizeScreen,
                    onSpoofHardwareChange = vm::setSpoofHardware,
                    onDisablePaymentChange = vm::setDisablePayment,
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }
        }

        navigation(
            startDestination = ScreenRoutes.EDIT_LUMINA_ROUTE,
            route = ScreenRoutes.EDIT_LUMINA_GRAPH
        ) {
            composable(
                route = ScreenRoutes.EDIT_LUMINA_ROUTE,
                arguments = listOf(
                    navArgument(ScreenRoutes.EDIT_LUMINA_ID_ARG) {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(ScreenRoutes.EDIT_LUMINA_GRAPH)
                }
                val vm: EditLuminaViewModel = hiltViewModel(parentEntry)

                EditLuminaScreen(
                    viewModel = vm,
                    onNavigateBack = {
                        navController.navigateUp()
                    },
                    onSaveLumina = {
                        navController.navigateUp()
                    },
                    onNavigateToAdvancedOptions = {
                        safeNavigate(ScreenRoutes.EDIT_ADVANCED_OPTIONS)
                    }
                )
            }

            composable(ScreenRoutes.EDIT_ADVANCED_OPTIONS) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(ScreenRoutes.EDIT_LUMINA_GRAPH)
                }
                val vm: EditLuminaViewModel = hiltViewModel(parentEntry)
                val uiState by vm.uiState.collectAsState()

                AdvancedOptionsScreen(
                    uiState = uiState,
                    onEphemeralChange = vm::setEphemeral,
                    onWebRtcDisabledChange = vm::setWebRtcDisabled,
                    onAfpEnabledChange = vm::setAfpEnabled,
                    onRandomizeUserAgentChange = vm::setRandomizeUserAgent,
                    onSpoofLocaleChange = vm::setSpoofLocale,
                    onSpoofTimezoneChange = vm::setSpoofTimezone,
                    onRandomizeCanvasChange = vm::setRandomizeCanvas,
                    onDisableAudioContextChange = vm::setDisableAudioContext,
                    onDisableWebGlChange = vm::setDisableWebGl,
                    onRandomizeScreenChange = vm::setRandomizeScreen,
                    onSpoofHardwareChange = vm::setSpoofHardware,
                    onDisablePaymentChange = vm::setDisablePayment,
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }
        }

        composable(
            route = ScreenRoutes.BROWSER_ROUTE,
            arguments = listOf(
                navArgument(ScreenRoutes.BROWSER_ID_ARG) {
                    type = NavType.LongType
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(slideDuration)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(slideDuration))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(slideDuration))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(slideDuration)
                )
            }
        ) {
            BrowserScreen(
                onClose = {
                    navController.popBackStack()
                }
            )
        }
    }
}
