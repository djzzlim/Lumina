package com.example.lumina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.lumina.navigation.AppNavigation
import com.example.lumina.ui.theme.LuminaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LuminaTheme {
                // 2. The MainActivity is now extremely clean.
                // Its only job is to set the theme and call the navigation graph.
                AppNavigation()
            }
        }
    }
}
