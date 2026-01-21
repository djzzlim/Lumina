package com.example.lumina.features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val dnsOptions = listOf("Cloudflare", "Google", "AdGuard", "Quad9")
    val searchEngineOptions = listOf("Google", "DuckDuckGo", "Brave Search")

    val selectedDns by viewModel.dnsProvider.collectAsStateWithLifecycle()
    var dnsExpanded by remember { mutableStateOf(false) }

    val selectedSearchEngine by viewModel.searchEngine.collectAsStateWithLifecycle()
    var searchEngineExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("DNS Provider", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = dnsExpanded,
                onExpandedChange = { dnsExpanded = !dnsExpanded }
            ) {
                TextField(
                    value = selectedDns,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dnsExpanded)
                    },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = dnsExpanded,
                    onDismissRequest = { dnsExpanded = false }
                ) {
                    dnsOptions.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                viewModel.setDnsProvider(it)
                                dnsExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Search Engine", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = searchEngineExpanded,
                onExpandedChange = { searchEngineExpanded = !searchEngineExpanded }
            ) {
                TextField(
                    value = selectedSearchEngine,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = searchEngineExpanded)
                    },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = searchEngineExpanded,
                    onDismissRequest = { searchEngineExpanded = false }
                ) {
                    searchEngineOptions.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                viewModel.setSearchEngine(it)
                                searchEngineExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
