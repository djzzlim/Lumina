package com.example.lumina

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumina.ui.theme.LuminaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// 1. ADD the parameter to the function signature here
fun LuminaHomeScreen(
    onNavigateToScanner: () -> Unit
) {
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
                    // Now this onClick is valid because onNavigateToScanner is a known parameter
                    IconButton(onClick = onNavigateToScanner) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR",
                            tint = Color(0xFFBB86FC)
                        )
                    }
                    IconButton(onClick = { /* Handle add */ }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Lumina",
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
            LuminaItemsGrid()
        }
    }
}


// --- Simple data class to hold Lumina info ---
data class LuminaInfo(
    val icon: ImageVector,
    val title: String,
    val url: String
)

// --- Lumina Items Grid ---

@Composable
fun LuminaItemsGrid() {
    val luminaItems = listOf(
        LuminaInfo(Icons.Default.Visibility, "Facebook", "facebook.com"),
        LuminaInfo(Icons.Default.Language, "Instagram", "instagram.com"),
        LuminaInfo(Icons.Default.Language, "Github", "github.com"),
        LuminaInfo(Icons.Default.Language, "LinkedIn", "linkedin.com"),
        LuminaInfo(Icons.Default.Visibility, "Reddit", "reddit.com"),
        LuminaInfo(Icons.Default.Language, "Twitter", "twitter.com")
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(luminaItems) { item ->
            LuminaItemCard(
                icon = item.icon,
                title = item.title,
                url = item.url
            )
        }
    }
}

private fun maskUrl(url: String, nameLengthThreshold: Int = 6, prefixLength: Int = 3): String {
    val parts = url.split('.', limit = 2)
    val name = parts.getOrNull(0) ?: return url
    val suffix = if (parts.size > 1) "." + parts[1] else ""
    return if (name.length > nameLengthThreshold) {
        val prefix = name.take(prefixLength)
        "$prefix***$suffix"
    } else {
        url
    }
}

@Composable
fun LuminaItemCard(
    icon: ImageVector,
    title: String,
    url: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(0.8f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF00A2FF),
                modifier = Modifier.size(48.dp)
            )
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
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// --- Previews ---

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun LuminaHomeScreenPreview() {
    LuminaTheme {
        // 2. UPDATE the preview to pass an empty lambda for the new parameter
        LuminaHomeScreen(onNavigateToScanner = {})
    }
}

@Preview(showBackground = true)
@Composable
fun LuminaItemCardPreview() {
    LuminaTheme {
        LuminaItemCard(
            icon = Icons.Default.Visibility,
            title = "Instagram",
            url = "instagram.com"
        )
    }
}
