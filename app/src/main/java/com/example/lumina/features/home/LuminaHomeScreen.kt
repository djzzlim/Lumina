package com.example.lumina.features.home

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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

    // Handle back button to exit selection mode if active
    BackHandler(enabled = uiState.selectionMode) {
        viewModel.toggleSelectionMode()
    }

    // Handle back button to show exit dialog if NOT in selection mode
    BackHandler(enabled = !uiState.selectionMode) {
        showExitDialog = true
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
                    onToggleSelectionMode = viewModel::toggleSelectionMode,
                    onNavigateToProfiles = { if (!isNavigating) { isNavigating = true; onNavigateToProfiles() } },
                    onNavigateToSettings = { if (!isNavigating) { isNavigating = true; onNavigateToSettings() } }
                )
            }
        },
        floatingActionButton = {
            if (!uiState.selectionMode) {
                FloatingActionButton(
                    onClick = { if (!isNavigating) { isNavigating = true; onNavigateToAddLumina() } },
                    containerColor = Color(0xFFBB86FC),
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Lumina")
                }
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF000000),
                            Color(0xFF050510)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
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
    onToggleSelectionMode: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                "Lumina",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        },
        actions = {
            IconButton(onClick = onNavigateToScanner) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = "Scan QR",
                    tint = Color(0xFFBB86FC),
                    modifier = Modifier.size(24.dp)
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color(0xFFBB86FC),
                        modifier = Modifier.size(24.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF1A1A2E))
                ) {
                    DropdownMenuItem(
                        text = { Text("Selection Mode", color = Color.White) },
                        onClick = {
                            showMenu = false
                            onToggleSelectionMode()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, null, tint = Color(0xFFBB86FC))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Profiles", color = Color.White) },
                        onClick = {
                            showMenu = false
                            onNavigateToProfiles()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Person, null, tint = Color(0xFFBB86FC))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings", color = Color.White) },
                        onClick = {
                            showMenu = false
                            onNavigateToSettings()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Settings, null, tint = Color(0xFFBB86FC))
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
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
                fontSize = 20.sp,
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
                    tint = Color(0xFFFF4C4C)
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
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp, top = 12.dp)
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
            .aspectRatio(0.92f)
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151525)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Box {
            Column(
                modifier = Modifier
                    .padding(10.dp)
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
                    Surface(
                        modifier = Modifier.size(48.dp),
                        color = iconColor.copy(alpha = 0.15f),
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = item.name,
                                tint = iconColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    var fontSize by remember(item.name) { mutableStateOf(12.sp) }
                    Text(
                        text = item.name,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = fontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        onTextLayout = { textLayoutResult ->
                            if (textLayoutResult.hasVisualOverflow && fontSize > 9.sp) {
                                fontSize = fontSize * 0.9f
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = maskedUrl,
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 8.5.sp,
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
                        .background(Color(0xFFBB86FC).copy(alpha = 0.15f))
                        .border(2.dp, Color(0xFFBB86FC), RoundedCornerShape(20.dp))
                )
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(0xFFBB86FC),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
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
