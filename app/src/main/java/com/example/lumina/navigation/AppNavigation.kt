package com.example.lumina.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lumina.features.advanced_options.AdvancedOptionsScreen
import com.example.lumina.features.browser.BrowserScreen
import com.example.lumina.features.home.HomeViewModel
import com.example.lumina.features.home.LuminaHomeScreen
import com.example.lumina.features.new_lumina.NewLuminaScreen
import com.example.lumina.features.new_lumina.NewLuminaViewModel
import com.example.lumina.features.profiles.ProfilesScreen
import com.example.lumina.features.qr_scanner.QRCodeScannerScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    fun safeNavigate(route: String) {
        if (navController.currentDestination?.route != route) {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = ScreenRoutes.HOME
    ) {

        composable(
            route = ScreenRoutes.HOME,
            exitTransition = {
                fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.9f, animationSpec = tween(300))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.9f, animationSpec = tween(300))
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
                onNavigateToProfiles = {
                    safeNavigate(ScreenRoutes.PROFILES_SCREEN)
                }
            )
        }

        composable(ScreenRoutes.PROFILES_SCREEN) {
            ProfilesScreen()
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
                        vm.onSave()
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
                AdvancedOptionsScreen(
                    viewModel = vm,
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
                fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.8f, animationSpec = tween(400))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.8f, animationSpec = tween(400))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 1.1f, animationSpec = tween(400))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 1.1f, animationSpec = tween(400))
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
