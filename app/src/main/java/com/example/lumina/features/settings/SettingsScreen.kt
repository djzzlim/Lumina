package com.example.lumina.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lumina.core.AutoCloseTimeout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToExtensions: () -> Unit
) {
    val selectedDns by viewModel.dnsProvider.collectAsStateWithLifecycle()
    val savedCustomDnsUri by viewModel.customDnsUri.collectAsStateWithLifecycle()
    val selectedSearchEngine by viewModel.searchEngine.collectAsStateWithLifecycle()
    val selectedAutoCloseTimeoutMinutes by viewModel.autoCloseTimeout.collectAsStateWithLifecycle()
    val safeBrowsingEnabled by viewModel.safeBrowsingEnabled.collectAsStateWithLifecycle()
    val localPhishingModelEnabled by viewModel.localPhishingModelEnabled.collectAsStateWithLifecycle()
    
    val selectedAutoCloseName = remember(selectedAutoCloseTimeoutMinutes) {
        AutoCloseTimeout.fromMinutes(selectedAutoCloseTimeoutMinutes).name
    }

    // Use local state for the text field to prevent cursor jumping
    var localCustomDnsUri by remember { mutableStateOf("") }
    
    // Update local state when the saved state changes (e.g. on initial load)
    LaunchedEffect(savedCustomDnsUri) {
        if (localCustomDnsUri != savedCustomDnsUri) {
            localCustomDnsUri = savedCustomDnsUri
        }
    }

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    "Settings",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                SettingsSection(title = "NETWORK") {
                    SettingsDropdownItem(
                        label = "DNS Provider",
                        currentValue = selectedDns,
                        options = viewModel.dnsOptions,
                        onOptionSelected = viewModel::setDnsProvider
                    )
                    
                    if (selectedDns == "Custom") {
                        HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Custom DNS Endpoint", color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            BasicTextField(
                                value = localCustomDnsUri,
                                onValueChange = { 
                                    localCustomDnsUri = it
                                    viewModel.setCustomDnsUri(it) // Save to disk
                                },
                                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                                cursorBrush = SolidColor(Color(0xFFBB86FC)),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (localCustomDnsUri.isEmpty()) {
                                            Text("https://example.com/dns-query", color = Color.DarkGray)
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SettingsSection(title = "SEARCH") {
                    SettingsDropdownItem(
                        label = "Search Engine",
                        currentValue = selectedSearchEngine,
                        options = viewModel.searchEngineOptions,
                        onOptionSelected = viewModel::setSearchEngine
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SettingsSection(title = "SECURITY") {
                    SettingsToggleItem(
                        label = "Safe Browsing (Google API)",
                        checked = safeBrowsingEnabled,
                        onCheckedChange = viewModel::setSafeBrowsingEnabled
                    )
                    HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
                    SettingsToggleItem(
                        label = "Offline AI Phishing Protection",
                        checked = localPhishingModelEnabled,
                        onCheckedChange = viewModel::setLocalPhishingModelEnabled
                    )
                }
                Text(
                    "Safe Browsing requires an internet connection to verify URLs. Offline AI protection runs locally on your device for maximum privacy.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                SettingsSection(title = "PRIVACY") {
                    SettingsDropdownItem(
                        label = "Auto-Close Inactive Tabs",
                        currentValue = selectedAutoCloseName,
                        options = viewModel.autoCloseOptions,
                        onOptionSelected = viewModel::setAutoCloseTimeout
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SettingsSection(title = "EXTENSIONS") {
                    SettingsItem(label = "Manage Extensions") { onNavigateToExtensions() }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SettingsItem(
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White, fontSize = 16.sp)
        Icon(
            Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}


@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
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
        ) {
            content()
        }
    }
}

@Composable
fun SettingsToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color(0xFFE5E5E5),
                uncheckedTrackColor = Color(0xFF3E3E3E)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDropdownItem(
    label: String,
    currentValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(label, color = Color.White, fontSize = 16.sp)
                Text(currentValue, color = Color.Gray, fontSize = 14.sp)
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF2C2C2E))
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(option, color = Color.White)
                            if (option == currentValue) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = Color(0xFFBB86FC),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
                if (option != options.last()) {
                    HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
                }
            }
        }
    }
}
