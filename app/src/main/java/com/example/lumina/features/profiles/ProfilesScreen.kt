package com.example.lumina.features.profiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lumina.core.database.Profile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    profilesViewModel: ProfilesViewModel = hiltViewModel()
) {
    val profiles by profilesViewModel.profiles.collectAsState(initial = emptyList())
    val currentProfileId by profilesViewModel.currentProfileId.collectAsState(initial = null)
    var showDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Profile")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("New Profile") },
                    text = {
                        TextField(
                            value = newProfileName,
                            onValueChange = { newProfileName = it },
                            label = { Text("Profile Name") }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                profilesViewModel.createProfile(newProfileName)
                                showDialog = false
                                newProfileName = ""
                            }
                        ) {
                            Text("Create")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(profiles) { profile ->
                    ProfileListItem(
                        profile = profile,
                        isCurrent = profile.id == currentProfileId,
                        onSwitch = { profilesViewModel.switchProfile(profile.id) },
                        onDelete = { profilesViewModel.deleteProfile(profile) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileListItem(
    profile: Profile,
    isCurrent: Boolean,
    onSwitch: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSwitch() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = profile.name,
            modifier = Modifier.weight(1f)
        )
        if (isCurrent) {
            Text(" (Current)", style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Profile")
        }
    }
}
