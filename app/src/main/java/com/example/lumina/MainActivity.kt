package com.example.lumina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lumina.ui.theme.LuminaTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LuminaTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home_screen"
    ) {
        // Composable for the home screen
        composable(
            "home_screen",
            exitTransition = { null },
            popEnterTransition = { fadeIn(animationSpec = tween(350)) }
        ) {
            LuminaHomeScreen(
                onNavigateToScanner = {
                    navController.navigate("qr_code_scanner_screen") {
                        launchSingleTop = true
                    }
                },
                onNavigateToAddLumina = {
                    // Navigate to the new screen WITHOUT a URL
                    navController.navigate("new_lumina_screen") {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Composable for the QR code scanner screen
        composable(
            route = "qr_code_scanner_screen",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(400)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(400)
                )
            }
        ) {
            QRCodeScannerScreen(
                // --- THIS IS A KEY CHANGE ---
                onUrlScanned = { scannedUrl ->
                    // URL-encode the scanned URL to make it safe for navigation
                    val encodedUrl = URLEncoder.encode(scannedUrl, StandardCharsets.UTF_8.toString())
                    // --- THIS IS THE FIX ---
                    // Navigate to the new screen...
                    navController.navigate("new_lumina_screen?url=$encodedUrl") {
                        // ...and pop the scanner screen off the back stack.
                        popUpTo("qr_code_scanner_screen") {
                            inclusive = true // 'true' means the qr_code_scanner_screen itself is removed.
                        }
                    }
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // Composable for the New Lumina screen
        composable(
            // --- DEFINE THE ROUTE WITH AN OPTIONAL ARGUMENT ---
            route = "new_lumina_screen?url={url}",
            arguments = listOf(
                navArgument("url") {
                    type = NavType.StringType
                    nullable = true // Mark the argument as optional
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(400)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(400)
                )
            }
        ) { backStackEntry ->
            // --- RETRIEVE THE ARGUMENT ---
            val urlFromScanner = backStackEntry.arguments?.getString("url")
            NewLuminaScreen(
                scannedUrl = urlFromScanner,
                onNavigateBack = { navController.navigateUp() },
                onSaveLumina = {
                    // TODO: Add save logic here
                    navController.navigateUp()
                },
                // --- FIX 1: IMPLEMENT the navigation logic here ---
                onNavigateToAdvancedOptions = {
                    navController.navigate("advanced_options_screen")
                }
            )
        }

        composable(
            route = "advanced_options_screen",
            // --- THIS IS THE FIX: Change the animation direction ---
            enterTransition = {
                // Screen slides in from the RIGHT
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400)
                )
            },
            exitTransition = {
                // When navigating away, it slides out to the LEFT
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400)
                )
            },
            popEnterTransition = {
                // When coming back, it slides in from the LEFT
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(400)
                )
            },
            popExitTransition = {
                // When popping back, it slides out to the RIGHT
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(400)
                )
            }
        ) {
            AdvancedOptionsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
