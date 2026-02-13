package com.example.lumina.features.edit_lumina

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumina.features.new_lumina.AdvancedOptionsRow
import com.example.lumina.features.new_lumina.IconAndThemeSection
import com.example.lumina.features.new_lumina.WebsiteInputSection

/**
 * Screen for editing an existing Lumina instance.
 *
 * It provides the same configuration options as the [NewLuminaScreen], including
 * name, URL, icon, theme color, and advanced settings, but pre-filled with the
 * existing data of the Lumina being edited.
 *
 * @param viewModel The [EditLuminaViewModel] managing the state and persistence of the edits.
 * @param onNavigateBack Callback for the "Close" navigation action.
 * @param onSaveLumina Callback to trigger the saving of the modified Lumina instance.
 * @param onNavigateToAdvancedOptions Callback to navigate to the advanced options screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLuminaScreen(
    viewModel: EditLuminaViewModel,
    onNavigateBack: () -> Unit,
    onSaveLumina: () -> Unit,
    onNavigateToAdvancedOptions: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, "Close", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onSave(onSuccess = onSaveLumina) }) {
                        Icon(Icons.Default.Check, "Save", tint = Color(0xFFBB86FC))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
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
                    "Edit Lumina",
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
