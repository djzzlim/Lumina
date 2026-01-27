package com.example.lumina.features.home

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.lumina.core.database.LuminaInfo
import com.example.lumina.core.utils.IconUtils


/**
 * The primary home screen of the Lumina app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuminaHomeScreen(
    viewModel: HomeViewModel,
    onNavigateToScanner: () -> Unit,
    onNavigateToAddLumina: () -> Unit,
    onNavigateToBrowser: (Long) -> Unit,
    onNavigateToEditLumina: (Long) -> Unit,
    onNavigateToProfiles: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isNavigating by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val activity = context as? Activity

    // Handle back button to show exit dialog
    BackHandler(enabled = !uiState.selectionMode) {
        showExitDialog = true
    }

    // Reset isNavigating when the screen is resumed
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
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
                    onNavigateToProfiles = { if (!isNavigating) { isNavigating = true; onNavigateToProfiles() } },
                    onNavigateToSettings = { if (!isNavigating) { isNavigating = true; onNavigateToSettings() } }
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
                .navigationBarsPadding()
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
                },
                onItemLongClick = {
                    if (!uiState.selectionMode && !isNavigating) {
                        isNavigating = true
                        onNavigateToEditLumina(it.id)
                    }
                }
            )
        }
        
        if (showExitDialog) {
            ForensicExitDialog(
                onConfirm = {
                    showExitDialog = false
                    activity?.finish()
                },
                onDismiss = { showExitDialog = false }
            )
        }
    }
}

/**
 * A custom forensic-themed dialog to confirm app exit and data wipe.
 */
@Composable
fun ForensicExitDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1A1A2E),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF4C4C),
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Close Session?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Exiting will trigger an immediate purge of all session data, including history, cookies, and temporary site configurations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Justify
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Stay", color = Color.White)
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF4C4C)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Exit", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    onNavigateToScanner: () -> Unit,
    onNavigateToAddLumina: () -> Unit,
    onToggleSelectionMode: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    onNavigateToSettings: () -> Unit
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
                    Icons.Default.Delete,
                    contentDescription = "Delete",
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
            IconButton(onClick = onNavigateToSettings) {
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
    onItemClick: (LuminaInfo) -> Unit,
    onItemLongClick: (LuminaInfo) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(
            items = items,
            key = { it.id }
        ) { item ->
            LuminaItemCard(
                item = item,
                isSelected = selectedItems.contains(item.id),
                onClick = { onItemClick(item) },
                onLongClick = { onItemLongClick(item) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LuminaItemCard(
    item: LuminaInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconVector = remember(item.icon) { IconUtils.getIconVector(item.icon) }
    val maskedUrl = remember(item.url) { maskUrl(item.url) }
    val iconColor = remember(item.color) { Color(item.color.toInt()) }

    Card(
        modifier = modifier
            .aspectRatio(0.8f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
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
