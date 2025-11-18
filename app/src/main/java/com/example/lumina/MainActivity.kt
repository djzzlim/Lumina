package com.example.lumina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lumina.ui.theme.LuminaTheme

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
        // The exit and popEnter transitions here make the home screen fade slightly,
        // which looks good behind the sliding scanner screen.
        composable(
            "home_screen",
            exitTransition = { fadeOut(animationSpec = tween(350)) },
            popEnterTransition = { fadeIn(animationSpec = tween(350)) }
        ) {
            LuminaHomeScreen(
                onNavigateToScanner = {
                    navController.navigate("qr_code_scanner_screen") {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Composable for the QR code scanner screen with a vertical slide animation
        composable(
            route = "qr_code_scanner_screen",
            enterTransition = {
                // Screen slides up from the bottom
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(400)
                )
            },
            popExitTransition = {
                // Screen slides down to the bottom when navigating back
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(400)
                )
            }
        ) {
            QRCodeScannerScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}
