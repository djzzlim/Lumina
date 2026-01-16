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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumina.core.data.LuminaInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuminaHomeScreen(
    viewModel: HomeViewModel,
    onNavigateToScanner: () -> Unit,
    onNavigateToAddLumina: () -> Unit,
    onNavigateToBrowser: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    onNavigateToScanner = onNavigateToScanner,
                    onNavigateToAddLumina = onNavigateToAddLumina,
                    onToggleSelectionMode = viewModel::toggleSelectionMode
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
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            LuminaItemsGrid(
                items = uiState.luminaItems,
                selectionMode = uiState.selectionMode,
                selectedItems = uiState.selectedItems,
                onItemClick = {
                    if (uiState.selectionMode) {
                        viewModel.toggleItemSelection(it.id)
                    } else {
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
    onToggleSelectionMode: () -> Unit
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
        items(items) { item ->
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
                        imageVector = getIconVector(item.icon),
                        contentDescription = item.name,
                        tint = Color(item.color),
                        modifier = Modifier.size(44.dp) // Slightly smaller to give text room
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
                        maxLines = 2, // Limit name to 2 lines
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = maskUrl(item.url),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp, // Slightly smaller font for URL
                        maxLines = 1,     // Force URL to 1 line
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
        "Visibility" -> Icons.Default.Visibility
        else -> Icons.Default.Language
    }
}

private fun maskUrl(url: String): String {
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
        url // Return original URL if parsing fails
    }
}
