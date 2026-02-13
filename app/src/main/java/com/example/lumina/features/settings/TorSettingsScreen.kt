package com.example.lumina.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TorSettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val torEnabled by viewModel.torEnabled.collectAsStateWithLifecycle()
    val torProfile by viewModel.torProfile.collectAsStateWithLifecycle()
    val useNetworkTimezone by viewModel.useNetworkTimezone.collectAsStateWithLifecycle()
    val torLogs by viewModel.torLogs.collectAsStateWithLifecycle()
    val torProgress by viewModel.torBootstrappingProgress.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tor Settings", color = Color.White) },
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
                    "Tor Network",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                SettingsSection(title = "STATUS") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (torEnabled) "Tor is Enabled" else "Tor is Disabled",
                                color = if (torEnabled) Color(0xFFBB86FC) else Color.Gray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (torEnabled) {
                                Text(
                                    text = "Bootstrapping: $torProgress%",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        if (torEnabled && torProgress == 100) {
                            IconButton(onClick = { viewModel.requestNewTorCircuit() }) {
                                Icon(Icons.Default.Refresh, "New Identity", tint = Color(0xFFBB86FC))
                            }
                        }
                    }
                    if (torEnabled && torProgress < 100) {
                        LinearProgressIndicator(
                            progress = { torProgress.toFloat() / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFBB86FC),
                            trackColor = Color(0xFF1C1C1E)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SettingsSection(title = "CONFIGURATION") {
                    SettingsToggleItem(
                        label = "Enable Tor",
                        checked = torEnabled,
                        onCheckedChange = viewModel::setTorEnabled
                    )
                    if (torEnabled) {
                        HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
                        SettingsDropdownItem(
                            label = "Security Profile",
                            currentValue = torProfile,
                            options = viewModel.torProfileOptions,
                            onOptionSelected = viewModel::setTorProfile
                        )
                    }
                }
                
                if (torEnabled) {
                    val securityHelpText = when (torProfile) {
                        "Standard" -> "All browser features are enabled. This provides the best usability and site compatibility."
                        "Safer" -> "Disables SVGs and some JavaScript optimizations. Protects against some types of fingerprinting and exploits."
                        "Safest" -> "Completely disables JavaScript and SVGs. Provides maximum security but many websites will not work properly."
                        else -> ""
                    }
                    Text(
                        securityHelpText,
                        color = Color(0xFFBB86FC),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SettingsSection(title = "TIMEZONE PRIVACY") {
                    SettingsToggleItem(
                        label = "Use Network Timezone",
                        checked = useNetworkTimezone,
                        onCheckedChange = viewModel::setUseNetworkTimezone
                    )
                }
                Text(
                    "When enabled, your browser timezone is spoofed to match your Tor exit node or active VPN location. When disabled, your real system time is used.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SettingsSection(title = "LIVE LOGS") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .padding(8.dp)
                    ) {
                        val scrollState = rememberScrollState()
                        LaunchedEffect(torLogs) {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                        Text(
                            text = if (torLogs.isEmpty()) "Waiting for logs..." else torLogs,
                            color = Color(0xFF00FF00),
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.verticalScroll(scrollState)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
