package com.example.lumina

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Theme (Using the same dark theme as before) ---
@Composable
fun LuminaAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFBB86FC),
            background = Color(0xFF000000),
            surface = Color(0xFF121212),
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}

// --- New Lumina Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
// --- FIX 1: Add parameters to the function signature ---
fun NewLuminaScreen(
    // --- ACCEPT the scanned URL (it's nullable) ---
    scannedUrl: String?,
    onNavigateBack: () -> Unit,
    onSaveLumina: () -> Unit
) {
    Scaffold(
        topBar = {
            NewLuminaTopAppBar(
                onClose = onNavigateBack,
                onSave = onSaveLumina
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "New Lumina",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // --- PASS the URL to the input section ---
            WebsiteInputSection(initialUrl = scannedUrl)
            Spacer(modifier = Modifier.height(24.dp))
            IconAndThemeSection()
        }
    }
}

// --- Top App Bar ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
// --- FIX 3: Update the TopAppBar to accept the callbacks ---
fun NewLuminaTopAppBar(
    onClose: () -> Unit,
    onSave: () -> Unit
) {
    TopAppBar(
        title = { /* No title, handled by Text below */ },
        navigationIcon = {
            // --- FIX 4: Use the onClose callback ---
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        },
        actions = {
            // --- FIX 5: Use the onSave callback ---
            IconButton(onClick = onSave) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Save",
                    tint = Color(0xFFBB86FC) // Purple check
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black
        )
    )
}
// --- Website Input Section ---

@Composable
fun WebsiteInputSection(
    // --- ACCEPT the initial URL ---
    initialUrl: String?
) {
    var name by remember { mutableStateOf("") }
    // --- USE the scanned URL if it exists, otherwise default to "https://" ---
    var url by remember { mutableStateOf(initialUrl ?: "https://") }

    Column {
        Text(
            "WEBSITE",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1C1E)) // Dark grey
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Name", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFFBB86FC)
                )
            )
            HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                placeholder = { Text("URL", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFFBB86FC)
                )
            )
        }
    }
}

// --- Icon & Theme Section ---

@Composable
fun IconAndThemeSection() {
    val icons = listOf(
        Icons.Default.Language, Icons.Default.Star, Icons.Default.Favorite,
        Icons.Default.Home, Icons.Default.DirectionsCar, Icons.Default.Flight,
        Icons.Default.ShoppingCart, Icons.Default.Notifications, Icons.Default.Delete,
        Icons.Default.LocalFireDepartment, Icons.Default.FlashOn, Icons.Default.Phone,
        Icons.Default.Nightlight, Icons.Default.AccessTime, Icons.Default.VideogameAsset,
        Icons.Default.Visibility, Icons.Default.Flag, Icons.Default.Sailing
    )
    var selectedIcon by remember { mutableStateOf(Icons.Default.Language) }

    Column {
        Text(
            "ICON & THEME",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1C1E))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pick a color", color = Color.White, fontSize = 16.sp)
                // Placeholder Color Picker
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00A2FF)) // Blue from image
                        .border(2.dp, Color.White, CircleShape)
                )
            }
            HorizontalDivider(
                color = Color(0xFF3A3A3C),
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Icon Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                // Fix height to avoid nested scrolling issues, or make Column scrollable
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                items(icons) { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selectedIcon == icon) Color(0xFFBB86FC) else Color.Gray,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { selectedIcon = icon }
                    )
                }
            }
        }
    }
}

// --- Preview ---

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun NewLuminaScreenPreview() {
    LuminaAppTheme {
        NewLuminaScreen(
            // Pass a sample URL for previewing
            scannedUrl = "https://www.google.com",
            onNavigateBack = {},
            onSaveLumina = {}
        )
    }
}