package com.example.lumina.features.new_lumina

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CrueltyFree
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DownhillSkiing
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlutterDash
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsFootball
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLuminaScreen(
    // It now receives the ViewModel as its source of truth
    viewModel: NewLuminaViewModel,
    onNavigateBack: () -> Unit,
    onSaveLumina: () -> Unit,
    onNavigateToAdvancedOptions: () -> Unit
) {
    // Collect the state from the ViewModel. The UI will automatically
    // recompose whenever this state changes.
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { NewLuminaTopAppBar(onClose = onNavigateBack, onSave = onSaveLumina) },
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

            // Pass the state down and the events up
            WebsiteInputSection(
                name = uiState.name,
                url = uiState.url,
                onNameChange = viewModel::onNameChange,
                onUrlChange = viewModel::onUrlChange
            )
            Spacer(modifier = Modifier.height(24.dp))
            IconAndThemeSection(
                selectedIcon = uiState.selectedIcon,
                onIconSelected = viewModel::onIconSelected
            )
            Spacer(modifier = Modifier.height(24.dp))

            AdvancedOptionsRow(onClick = onNavigateToAdvancedOptions)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- Top App Bar --- (Stateless)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLuminaTopAppBar(onClose: () -> Unit, onSave: () -> Unit) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "Close", tint = Color.White)
            }
        },
        actions = {
            IconButton(onClick = onSave) {
                Icon(Icons.Default.Check, "Save", tint = Color(0xFFBB86FC))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
    )
}

// --- Sub-sections are now stateless ---
@Composable
fun WebsiteInputSection(
    name: String,
    url: String,
    onNameChange: (String) -> Unit,
    onUrlChange: (String) -> Unit
) {
    Column {
        Text("WEBSITE", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF1C1C1E))) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
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
                onValueChange = onUrlChange,
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

@Composable
fun IconAndThemeSection(
    selectedIcon: ImageVector,
    onIconSelected: (ImageVector) -> Unit
) {
    val icons = listOf(
        Icons.Default.Language, Icons.Default.Star, Icons.Default.Favorite, Icons.Default.Home,
        Icons.Default.DirectionsCar, Icons.Default.Flight, Icons.Default.ShoppingCart, Icons.Default.Notifications,
        Icons.Default.Delete, Icons.Default.LocalFireDepartment, Icons.Default.FlashOn, Icons.Default.Cloud,
        Icons.Default.WbSunny, Icons.Default.Nightlight, Icons.Default.AccessTime, Icons.Default.Settings,
        Icons.Default.VideogameAsset, Icons.Default.Face, Icons.Default.Visibility, Icons.Default.Sailing,
        Icons.Default.Tv, Icons.Default.Flag, Icons.Default.SportsSoccer, Icons.Default.SportsBaseball,
        Icons.Default.SportsBasketball, Icons.Default.SportsFootball, Icons.Default.SportsTennis, Icons.Default.DownhillSkiing,
        Icons.Default.Circle, Icons.Default.Sports, Icons.Default.EmojiEvents, Icons.Default.Pets, Icons.Default.Adb,
        Icons.Default.FlutterDash, Icons.Default.CrueltyFree, Icons.Default.BugReport, Icons.Default.WaterDrop,
        Icons.Default.Eco, Icons.Default.LocalFlorist, Icons.Default.Park, Icons.Default.FilterVintage, Icons.Default.Science
    )

    Column {
        Text("ICON & THEME", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF1C1C1E)).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Pick a color", color = Color.White, fontSize = 16.sp)
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF00A2FF)).border(2.dp, Color.White, CircleShape))
            }
            HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 16.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                items(icons) { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selectedIcon == icon) Color(0xFFBB86FC) else Color.Gray,
                        modifier = Modifier.size(32.dp).clickable { onIconSelected(icon) }
                    )
                }
            }
        }
    }
}


// --- Advanced Options Row --- (Stateless)
@Composable
fun AdvancedOptionsRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF1C1C1E)).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Advanced Options", color = Color.White, fontSize = 16.sp)
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, "Open Advanced Options", tint = Color.Gray, modifier = Modifier.size(16.dp))
    }
}


