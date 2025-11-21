package com.example.lumina.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lumina.features.advanced_options.AdvancedOptionsScreen
import com.example.lumina.features.advanced_options.AdvancedOptionsViewModel
import com.example.lumina.features.home.HomeViewModel
import com.example.lumina.features.home.LuminaHomeScreen
import com.example.lumina.features.new_lumina.NewLuminaScreen
import com.example.lumina.features.new_lumina.NewLuminaViewModel
import com.example.lumina.features.qr_scanner.QRCodeScannerScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ScreenRoutes.HOME // Use the constant
    ) {
        // Composable for the home screen
        composable(
            ScreenRoutes.HOME, // Use the constant
            exitTransition = { null },
            popEnterTransition = { fadeIn(animationSpec = tween(350)) }
        ) {
            // Create an instance of the HomeViewModel
            val homeViewModel: HomeViewModel = viewModel()
            LuminaHomeScreen(
                // Pass the viewModel to the screen
                viewModel = homeViewModel,
                onNavigateToScanner = {
                    navController.navigate(ScreenRoutes.QR_SCANNER) { launchSingleTop = true }
                },
                onNavigateToAddLumina = {
                    // Navigate to the base route without arguments
                    navController.navigate(ScreenRoutes.NEW_LUMINA_BASE) { launchSingleTop = true }
                }
            )
        }
        // Composable for the QR code scanner screen
        composable(
            route = ScreenRoutes.QR_SCANNER, // Use the constant
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(400)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(400)) }
        ) {
            QRCodeScannerScreen(
                onUrlScanned = { scannedUrl ->
                    val encodedUrl = URLEncoder.encode(scannedUrl, StandardCharsets.UTF_8.toString())
                    // Build the route with the argument
                    navController.navigate("${ScreenRoutes.NEW_LUMINA_BASE}?${ScreenRoutes.NEW_LUMINA_URL_ARG}=$encodedUrl") {
                        popUpTo(ScreenRoutes.QR_SCANNER) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Composable for the New Lumina screen
        composable(
            route = ScreenRoutes.NEW_LUMINA_ROUTE, // Use the constant
            arguments = listOf(
                navArgument(ScreenRoutes.NEW_LUMINA_URL_ARG) {
                    type = NavType.StringType
                    nullable = true
                }
            ),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(400)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(400)) }
        ) { backStackEntry ->
            val luminaViewModel: NewLuminaViewModel = viewModel()
            val urlFromScanner = backStackEntry.arguments?.getString(ScreenRoutes.NEW_LUMINA_URL_ARG)

            LaunchedEffect(key1 = urlFromScanner) {
                luminaViewModel.initializeFromScannedUrl(urlFromScanner)
            }

            NewLuminaScreen(
                viewModel = luminaViewModel,
                onNavigateBack = { navController.navigateUp() },
                onSaveLumina = {
                    luminaViewModel.onSave()
                    navController.navigateUp()
                },
                onNavigateToAdvancedOptions = {
                    navController.navigate(ScreenRoutes.ADVANCED_OPTIONS)
                }
            )
        }

        // Composable for the Advanced Options screen
        composable(
            route = ScreenRoutes.ADVANCED_OPTIONS, // Use the constant
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) }
        ) {
            val advancedOptionsViewModel: AdvancedOptionsViewModel = viewModel()
            AdvancedOptionsScreen(
                viewModel = advancedOptionsViewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
