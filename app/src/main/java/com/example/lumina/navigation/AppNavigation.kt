package com.example.lumina.navigation

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

        composable(ScreenRoutes.HOME) {
            val vm: HomeViewModel = viewModel()
            LuminaHomeScreen(
                viewModel = vm,
                onNavigateToScanner = {
                    safeNavigate(ScreenRoutes.QR_SCANNER)
                },
                onNavigateToAddLumina = {
                    safeNavigate(ScreenRoutes.NEW_LUMINA_BASE)
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

        composable(
            route = ScreenRoutes.NEW_LUMINA_ROUTE,
            arguments = listOf(
                navArgument(ScreenRoutes.NEW_LUMINA_URL_ARG) {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val vm: NewLuminaViewModel = viewModel()
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

        composable(ScreenRoutes.ADVANCED_OPTIONS) {
            val vm: AdvancedOptionsViewModel = viewModel()
            AdvancedOptionsScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}
