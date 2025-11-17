package com.example.lumina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.lumina.ui.theme.LuminaTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LuminaTheme {
                SilosHomeScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SilosHomeScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lumina",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = { /* Handle add */ }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Silo",
                            tint = Color(0xFFBB86FC)
                        )
                    }
                    IconButton(onClick = { /* Handle settings */ }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFFBB86FC)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            SiloItemsGrid()
        }
    }
}

// --- Silo Items Grid ---

@Composable
fun SiloItemsGrid() {
    // Using a Row for the 3-item layout.
    // For a scrolling grid, you'd use LazyVerticalGrid.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SiloItemCard(
            icon = Icons.Default.Visibility, // Placeholder for 'eye'
            title = "Facebook",
            url = "facebook.com",
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        SiloItemCard(
            icon = Icons.Default.Language, // Placeholder for 'globe'
            title = "instagram",
            url = "instagram.com",
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        SiloItemCard(
            icon = Icons.Default.Language, // Placeholder for 'globe'
            title = "Github",
            url = "github.com", // This one will not be masked
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Masks a URL if the domain name part is longer than [nameLengthThreshold].
 * Example: "instagram.com" -> "ins***.com"
 */
private fun maskUrl(url: String, nameLengthThreshold: Int = 6, prefixLength: Int = 3): String {
    // Split at the first dot to separate name from the rest (e.g., "instagram" and "com")
    val parts = url.split('.', limit = 2)
    val name = parts.getOrNull(0) ?: return url // Return original if no name part (e.g., "localhost")

    // Reconstruct the suffix (e.g., ".com" or ".co.uk")
    val suffix = if (parts.size > 1) "." + parts[1] else ""

    // Check if the name part is "too long"
    return if (name.length > nameLengthThreshold) {
        val prefix = name.take(prefixLength)
        "$prefix***$suffix" // Return masked version
    } else {
        url // Return original URL
    }
}

@Composable
fun SiloItemCard(
    icon: ImageVector,
    title: String,
    url: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(0.8f), // Make it taller than wide
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E) // Dark blueish-gray
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Changed from Arrangement.SpaceBetween
            verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing between items
        ) {
            // Added a Spacer at the top to push content down slightly
            Spacer(modifier = Modifier.weight(1f)) // Pushes content towards bottom

            // Center Icon
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF00A2FF),
                modifier = Modifier.size(48.dp)
            )

            // Bottom Text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = maskUrl(url),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp)) // Small spacer at the bottom
        }
    }
}

// --- Previews ---

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun SilosHomeScreenPreview() {
    LuminaTheme {
        SilosHomeScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SiloItemCardPreview() {
    LuminaTheme {
        SiloItemCard(
            icon = Icons.Default.Visibility,
            title = "Instagram",
            url = "instagram.com" // This will be masked in the preview
        )
    }
}