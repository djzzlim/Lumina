package com.example.lumina.features.profiles

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lumina.core.database.Profile

/**
 * Screen for managing user profiles.
 *
 * Allows users to view existing profiles, switch between them, create new
 * profiles, and delete existing ones.
 *
 * @param onNavigateBack Callback to go back to the previous screen (Home).
 * @param profilesViewModel The [ProfilesViewModel] that manages profile data and actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    onNavigateBack: () -> Unit,
    profilesViewModel: ProfilesViewModel = hiltViewModel()
) {
    val profiles by profilesViewModel.profiles.collectAsState(initial = emptyList())
    val currentProfileId by profilesViewModel.currentProfileId.collectAsState(initial = null)
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<Profile?>(null) }
    var profileNameInput by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                profileNameInput = ""
                showAddDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Profile")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Add Profile Dialog
            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text("New Profile") },
                    text = {
                        TextField(
                            value = profileNameInput,
                            onValueChange = { profileNameInput = it },
                            label = { Text("Profile Name") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (profileNameInput.isNotBlank()) {
                                    profilesViewModel.createProfile(profileNameInput)
                                    showAddDialog = false
                                    profileNameInput = ""
                                }
                            })
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (profileNameInput.isNotBlank()) {
                                    profilesViewModel.createProfile(profileNameInput)
                                    showAddDialog = false
                                    profileNameInput = ""
                                }
                            }
                        ) {
                            Text("Create")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showAddDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Edit Profile Dialog
            editingProfile?.let { profile ->
                AlertDialog(
                    onDismissRequest = { editingProfile = null },
                    title = { Text("Edit Profile") },
                    text = {
                        TextField(
                            value = profileNameInput,
                            onValueChange = { profileNameInput = it },
                            label = { Text("Profile Name") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (profileNameInput.isNotBlank()) {
                                    profilesViewModel.updateProfile(profile.copy(name = profileNameInput))
                                    editingProfile = null
                                    profileNameInput = ""
                                }
                            })
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (profileNameInput.isNotBlank()) {
                                    profilesViewModel.updateProfile(profile.copy(name = profileNameInput))
                                    editingProfile = null
                                    profileNameInput = ""
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { editingProfile = null }) {
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
                        canDelete = profiles.size > 1,
                        onSwitch = { 
                            profilesViewModel.switchProfile(profile.id)
                            onNavigateBack() // Go back to main screen after switching
                        },
                        onDelete = { profilesViewModel.deleteProfile(profile) },
                        onEdit = {
                            profileNameInput = profile.name
                            editingProfile = profile
                        }
                    )
                }
            }
        }
    }
}

/**
 * A list item representing a single profile.
 *
 * @param profile The [Profile] entity to display.
 * @param isCurrent Whether this is the currently active profile.
 * @param canDelete Whether this profile can be deleted.
 * @param onSwitch Callback to be invoked when the profile is clicked to be switched.
 * @param onDelete Callback to be invoked when the delete icon is clicked.
 * @param onEdit Callback to be invoked when the profile is long-clicked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileListItem(
    profile: Profile,
    isCurrent: Boolean,
    canDelete: Boolean,
    onSwitch: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onSwitch() },
                onLongClick = { onEdit() }
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.bodyLarge
            )
            if (isCurrent) {
                Text(
                    text = "Current Profile",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        if (canDelete) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Profile",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
