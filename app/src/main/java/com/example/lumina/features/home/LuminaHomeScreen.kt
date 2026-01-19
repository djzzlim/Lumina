package com.example.lumina.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SelectAll
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.lumina.core.database.LuminaInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuminaHomeScreen(
    viewModel: HomeViewModel,
    onNavigateToScanner: () -> Unit,
    onNavigateToAddLumina: () -> Unit,
    onNavigateToBrowser: (Long) -> Unit,
    onNavigateToProfiles: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isNavigating by remember { mutableStateOf(false) }

    // Reset isNavigating when the screen is resumed (e.g., navigating back to it)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isNavigating = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            if (uiState.selectionMode) {
                SelectionTopAppBar(
                    selectedItemCount = uiState.selectedItems.size,
                    onCancel = viewModel::toggleSelectionMode,
                    onDelete = viewModel::deleteSelectedItems
                )
            } else {
                HomeTopAppBar(
                    onNavigateToScanner = { if (!isNavigating) { isNavigating = true; onNavigateToScanner() } },
                    onNavigateToAddLumina = { if (!isNavigating) { isNavigating = true; onNavigateToAddLumina() } },
                    onToggleSelectionMode = viewModel::toggleSelectionMode,
                    onNavigateToProfiles = { if (!isNavigating) { isNavigating = true; onNavigateToProfiles() } }
                )
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            LuminaItemsGrid(
                items = uiState.luminaItems,
                selectionMode = uiState.selectionMode,
                selectedItems = uiState.selectedItems,
                onItemClick = {
                    if (uiState.selectionMode) {
                        viewModel.toggleItemSelection(it.id)
                    } else if (!isNavigating) {
                        isNavigating = true
                        onNavigateToBrowser(it.id)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    onNavigateToScanner: () -> Unit,
    onNavigateToAddLumina: () -> Unit,
    onToggleSelectionMode: () -> Unit,
    onNavigateToProfiles: () -> Unit
) {
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
            IconButton(onClick = onToggleSelectionMode) {
                Icon(
                    Icons.Default.SelectAll,
                    contentDescription = "Select",
                    tint = Color(0xFFBB86FC)
                )
            }
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
            IconButton(onClick = onNavigateToProfiles) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profiles",
                    tint = Color(0xFFBB86FC)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopAppBar(
    selectedItemCount: Int,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "$selectedItemCount selected",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.White
            )
        },
        navigationIcon = {
            IconButton(onClick = onCancel) {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = "Cancel",
                    tint = Color(0xFFBB86FC)
                )
            }
        },
        actions = {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFBB86FC)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black
        )
    )
}

@Composable
fun LuminaItemsGrid(
    items: List<LuminaInfo>,
    selectionMode: Boolean,
    selectedItems: Set<Long>,
    onItemClick: (LuminaInfo) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(
            items = items,
            key = { it.id } // Adding keys improves performance
        ) { item ->
            LuminaItemCard(
                item = item,
                isSelected = selectedItems.contains(item.id),
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
fun LuminaItemCard(
    item: LuminaInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Optimization: remember expensive operations
    val iconVector = remember(item.icon) { getIconVector(item.icon) }
    val maskedUrl = remember(item.url) { maskUrl(item.url) }
    val iconColor = remember(item.color) { Color(item.color.toInt()) }

    Card(
        modifier = modifier
            .aspectRatio(0.8f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        )
    ) {
        Box {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = item.name,
                        tint = iconColor,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = maskedUrl,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                )
            }
        }
    }
}

private fun getIconVector(iconName: String): ImageVector {
    return when (iconName) {
        "Language" -> Icons.Default.Language
        "Star" -> Icons.Default.Star
        "Favorite" -> Icons.Default.Favorite
        "Home" -> Icons.Default.Home
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "Flight" -> Icons.Default.Flight
        "ShoppingCart" -> Icons.Default.ShoppingCart
        "Notifications" -> Icons.Default.Notifications
        "Delete" -> Icons.Default.Delete
        "LocalFireDepartment" -> Icons.Default.LocalFireDepartment
        "FlashOn" -> Icons.Default.FlashOn
        "Cloud" -> Icons.Default.Cloud
        "WbSunny" -> Icons.Default.WbSunny
        "Nightlight" -> Icons.Default.Nightlight
        "AccessTime" -> Icons.Default.AccessTime
        "Settings" -> Icons.Default.Settings
        "VideogameAsset" -> Icons.Default.VideogameAsset
        "Face" -> Icons.Default.Face
        "Visibility" -> Icons.Default.Visibility
        "Sailing" -> Icons.Default.Sailing
        "Tv" -> Icons.Default.Tv
        "Flag" -> Icons.Default.Flag
        "SportsSoccer" -> Icons.Default.SportsSoccer
        "SportsBaseball" -> Icons.Default.SportsBaseball
        "SportsBasketball" -> Icons.Default.SportsBasketball
        "SportsFootball" -> Icons.Default.SportsFootball
        "SportsTennis" -> Icons.Default.SportsTennis
        "DownhillSkiing" -> Icons.Default.DownhillSkiing
        "Circle" -> Icons.Default.Circle
        "Sports" -> Icons.Default.Sports
        "EmojiEvents" -> Icons.Default.EmojiEvents
        "Pets" -> Icons.Default.Pets
        "Adb" -> Icons.Default.Adb
        "FlutterDash" -> Icons.Default.FlutterDash
        "CrueltyFree" -> Icons.Default.CrueltyFree
        "BugReport" -> Icons.Default.BugReport
        "WaterDrop" -> Icons.Default.WaterDrop
        "Eco" -> Icons.Default.Eco
        "LocalFlorist" -> Icons.Default.LocalFlorist
        "Park" -> Icons.Default.Park
        "FilterVintage" -> Icons.Default.FilterVintage
        "Science" -> Icons.Default.Science
        else -> Icons.Default.Language
    }
}

private fun maskUrl(url: String): String {
    if (url.isEmpty()) return ""
    return try {
        val parsedUrl = java.net.URL(url)
        var host = parsedUrl.host
        if (host.startsWith("www.")) {
            host = host.substring(4)
        }
        if (host.length > 15) {
            val parts = host.split('.')
            if (parts.size > 1) {
                val name = parts.first()
                val tld = parts.last()
                val prefix = name.take(6)
                "$prefix***.$tld"
            } else {
                host
            }
        } else {
            host
        }
    } catch (_: Exception) {
        url
    }
}
