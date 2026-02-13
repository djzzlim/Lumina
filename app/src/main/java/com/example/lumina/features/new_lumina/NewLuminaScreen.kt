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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Wallet
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Composable representing the screen for creating a new Lumina instance.
 *
 * This screen allows users to input a name and URL, select an icon and theme color,
 * and navigate to advanced configuration options before saving.
 *
 * @param viewModel The [NewLuminaViewModel] providing state and handling user actions.
 * @param onNavigateBack Callback for the "Close" navigation action.
 * @param onSaveLumina Callback to trigger the saving of the new Lumina instance.
 * @param onNavigateToAdvancedOptions Callback to navigate to the advanced options screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLuminaScreen(
    viewModel: NewLuminaViewModel,
    onNavigateBack: () -> Unit,
    onSaveLumina: () -> Unit,
    onNavigateToAdvancedOptions: () -> Unit
) {
    // Collect the state from the ViewModel.
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            NewLuminaTopAppBar(
                onClose = onNavigateBack,
                onSave = { viewModel.onSave(onSuccess = onSaveLumina) },
                saveEnabled = !uiState.isSaving
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
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
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

/**
 * Top app bar for the New Lumina screen.
 *
 * @param onClose Callback for the close button.
 * @param onSave Callback for the save button.
 * @param saveEnabled Whether the save button is enabled.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLuminaTopAppBar(onClose: () -> Unit, onSave: () -> Unit, saveEnabled: Boolean = true) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "Close", tint = Color.White)
            }
        },
        actions = {
            IconButton(onClick = onSave, enabled = saveEnabled) {
                Icon(
                    Icons.Default.Check,
                    "Save",
                    tint = if (saveEnabled) Color(0xFFBB86FC) else Color.Gray
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
    )
}

/**
 * Section for inputting the website's name and URL.
 *
 * @param name The current name input.
 * @param url The current URL input.
 * @param onNameChange Callback when name changes.
 * @param onUrlChange Callback when URL changes.
 */
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
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    autoCorrect = false,
                    platformImeOptions = androidx.compose.ui.text.input.PlatformImeOptions(
                        privateImeOptions = "noPersonalizedLearning"
                    )
                ),
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
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done,
                    autoCorrect = false,
                    platformImeOptions = androidx.compose.ui.text.input.PlatformImeOptions(
                        privateImeOptions = "noPersonalizedLearning"
                    )
                ),
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

/**
 * Section for selecting an icon and color theme.
 *
 * @param selectedIcon The currently selected icon.
 * @param selectedColor The currently selected theme color.
 * @param onIconSelected Callback when an icon is selected.
 * @param onColorSelected Callback when a color is selected.
 */
@Composable
fun IconAndThemeSection(
    selectedIcon: ImageVector,
    selectedColor: Color,
    onIconSelected: (ImageVector) -> Unit,
    onColorSelected: (Color) -> Unit
) {
    // Optimization: Reduced to 50 icons to improve performance and fix lag.
    val icons = remember {
        listOf(
            // Social & Communication
            Icons.Default.Language, Icons.Default.Email,
            Icons.AutoMirrored.Filled.Chat, Icons.Default.Forum,
            Icons.Default.Groups, Icons.Default.Person, Icons.Default.Public, Icons.Default.Share,
            
            // Entertainment & Media
            Icons.Default.Star, Icons.Default.Favorite, Icons.Default.PlayCircle, Icons.Default.Movie,
            Icons.Default.MusicNote, Icons.Default.VideogameAsset, Icons.Default.Tv, Icons.Default.Radio,
            
            // Photography & Art
            Icons.Default.CameraAlt, Icons.Default.PhotoLibrary, Icons.Default.Brush, Icons.Default.Palette,
            
            // Utility & Tools
            Icons.Default.Home, Icons.Default.Settings, Icons.Default.Build, Icons.Default.Search,
            Icons.Default.Notifications, Icons.Default.Lock, Icons.Default.Shield, Icons.Default.Key,
            
            // Commerce & Finance
            Icons.Default.ShoppingCart, Icons.Default.Storefront, Icons.Default.Wallet, Icons.Default.CreditCard,
            
            // Travel & Transport
            Icons.Default.DirectionsCar, Icons.Default.Flight, Icons.Default.Explore, Icons.Default.Map,
            
            // Food & Drink
            Icons.Default.Restaurant, Icons.Default.LocalCafe, Icons.Default.Fastfood, Icons.Default.LocalFlorist,
            
            // Health & Nature
            Icons.Default.MedicalServices, Icons.Default.FitnessCenter, Icons.Default.Eco, Icons.Default.Cloud,
            
            // Tech & Science
            Icons.Default.Science, Icons.Default.Adb, Icons.Default.Laptop, Icons.Default.Smartphone,
            
            // Reading & Writing
            Icons.AutoMirrored.Filled.MenuBook, Icons.Default.Edit
        )
    }

    val colors = remember {
        listOf(
            Color(0xFF00A2FF), Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFFCC00),
            Color(0xFF4CD964), Color(0xFF5AC8FA), Color(0xFF007AFF), Color(0xFF5856D6),
            Color(0xFFFF2D55), Color(0xFF8E8E93), Color(0xFFAF52DE), Color(0xFFBB86FC)
        )
    }

    Column {
        Text("ICON & THEME", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF1C1C1E)).padding(16.dp)) {
            Text("Pick a color", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = colors,
                    key = { it.toArgb() }
                ) { color ->
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
                columns = GridCells.Fixed(5), // Use fixed columns to reduce measurement overhead
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.heightIn(max = 240.dp)
            ) {
                itemsIndexed(
                    items = icons,
                    key = { index, icon -> "${icon.name}_$index" } // Ensure unique keys to fix selection bug
                ) { _, icon ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedIcon.name == icon.name) Color.White.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { onIconSelected(icon) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (selectedIcon.name == icon.name) selectedColor else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clickable row that navigates to the Advanced Options screen.
 *
 * @param onClick Callback when the row is clicked.
 */
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
