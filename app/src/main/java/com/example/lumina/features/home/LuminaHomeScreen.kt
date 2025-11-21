package com.example.lumina.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun LuminaHomeScreen(
    // The screen now accepts the ViewModel
    viewModel: HomeViewModel,
    // It still needs navigation callbacks
    onNavigateToScanner: () -> Unit,
    onNavigateToAddLumina: () -> Unit
) {
    // Collect the state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

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
                    IconButton(onClick = onNavigateToScanner) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR",
                            tint = Color(0xFFBB86FC)
                        )
                    }
                    IconButton(onClick = onNavigateToAddLumina) {
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
            // Pass the list from the state to the grid
            LuminaItemsGrid(items = uiState.luminaItems)
        }
    }
}

// The grid is now a stateless, reusable component that just displays the data it's given.
@Composable
fun LuminaItemsGrid(items: List<LuminaInfo>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(items) { item ->
            LuminaItemCard(
                icon = item.icon,
                title = item.title,
                url = item.url
            )
        }
    }
}

// These components are also stateless and can be moved to a `ui/components` package later if desired.
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

private fun maskUrl(url: String): String {
    val parts = url.split('.', limit = 2)
    val name = parts.getOrNull(0) ?: return url
    val suffix = if (parts.size > 1) ".${parts[1]}" else ""
    return if (name.length > 6) {
        val prefix = name.take(3)
        "$prefix***$suffix"
    } else {
        url
    }
}

// --- Preview ---
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun LuminaHomeScreenPreview() {
    LuminaTheme {
        LuminaHomeScreen(
            viewModel = HomeViewModel(), // Use a real ViewModel for an accurate preview
            onNavigateToScanner = {},
            onNavigateToAddLumina = {}
        )
    }
}
