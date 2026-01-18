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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLuminaScreen(
    viewModel: NewLuminaViewModel,
    onNavigateBack: () -> Unit,
    onSaveLumina: () -> Unit,
    onNavigateToAdvancedOptions: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            NewLuminaTopAppBar(
                onClose = onNavigateBack,
                onSave = onSaveLumina
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            item {
                Text(
                    "New Lumina",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                WebsiteInputSection(
                    name = uiState.name,
                    url = uiState.url,
                    onNameChange = viewModel::onNameChange,
                    onUrlChange = viewModel::onUrlChange
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                IconAndThemeSection(
                    selectedIcon = uiState.selectedIcon,
                    selectedColor = uiState.selectedColor,
                    onIconSelected = viewModel::onIconSelected,
                    onColorSelected = viewModel::onColorSelected
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                AdvancedOptionsRow(onClick = onNavigateToAdvancedOptions)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

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
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
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
    selectedColor: Color,
    onIconSelected: (ImageVector) -> Unit,
    onColorSelected: (Color) -> Unit
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

    val colors = listOf(
        Color(0xFF00A2FF), Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFFCC00),
        Color(0xFF4CD964), Color(0xFF5AC8FA), Color(0xFF007AFF), Color(0xFF5856D6),
        Color(0xFFFF2D55), Color(0xFF8E8E93), Color(0xFFAF52DE), Color(0xFFBB86FC)
    )

    Column {
        Text("ICON & THEME", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF1C1C1E)).padding(16.dp)) {
            Text("Pick a color", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(colors) { color ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColor == color) 2.dp else 0.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(color) }
                    )
                }
            }
            
            HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 16.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.heightIn(max = 240.dp)
            ) {
                items(icons) { icon ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedIcon == icon) Color.White.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { onIconSelected(icon) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (selectedIcon == icon) selectedColor else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

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
