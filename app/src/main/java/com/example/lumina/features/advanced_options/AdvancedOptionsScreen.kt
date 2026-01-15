package com.example.lumina.features.advanced_options

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lumina.features.new_lumina.NewLuminaUiState
import com.example.lumina.features.new_lumina.NewLuminaViewModel
import com.example.lumina.ui.theme.LuminaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedOptionsScreen(
    viewModel: NewLuminaViewModel,
    onNavigateBack: () -> Unit
) {
    // Collect the state from the ViewModel. The UI will automatically
    // recompose whenever this state changes.
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { AdvancedOptionsTopAppBar(onNavigateBack = onNavigateBack) },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Advanced Options",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // --- Storage Section ---
            SectionTitle("STORAGE")
            ToggleRow(
                text = "Ephemeral",
                checked = uiState.isEphemeral,
                onCheckedChange = viewModel::setEphemeral // Send event to ViewModel
            )
            HelpText("Ephemeral lumina will automatically reset when they're closed and will not store any data")
            Spacer(modifier = Modifier.height(24.dp))

            // --- Antifingerprinting Section ---
            SectionTitle("ANTIFINGERPRINTING")
            AntifingerprintingToggles(
                afpState = uiState,
                onAfpEnabledChange = viewModel::setAfpEnabled,
                onRandomizeUserAgentChange = viewModel::setRandomizeUserAgent,
                onSpoofLocaleChange = viewModel::setSpoofLocale,
                onSpoofTimezoneChange = viewModel::setSpoofTimezone,
                onRandomizeCanvasChange = viewModel::setRandomizeCanvas,
                onDisableAudioContextChange = viewModel::setDisableAudioContext,
                onDisableWebGlChange = viewModel::setDisableWebGl,
                onRandomizeScreenChange = viewModel::setRandomizeScreen,
                onSpoofHardwareChange = viewModel::setSpoofHardware,
                onDisablePaymentChange = viewModel::setDisablePayment
            )
            HelpText("Lumina has many antifingerprinting measures. Some websites may not be compatible with some of these measures enabled.")
            Spacer(modifier = Modifier.height(8.dp))
            HelpText("If you're encountering issues with Cloudflare, try disabling \"Spoof system locale\"")
            Spacer(modifier = Modifier.height(24.dp))

            // --- WebRTC Section ---
            SectionTitle("WEBRTC")
            ToggleRow(
                text = "Disable WebRTC",
                checked = uiState.isWebRtcDisabled,
                onCheckedChange = viewModel::setWebRtcDisabled // Send event to ViewModel
            )
            HelpText(
                "WARNING: Lumina disables WebRTC by default, as enabling WebRTC will leak your real IP address. Unchecking this option will reveal your IP address to any website that uses WebRTC.",
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// This composable is now stateless and just receives the state and all callbacks.
@Composable
fun AntifingerprintingToggles(
    afpState: NewLuminaUiState,
    onAfpEnabledChange: (Boolean) -> Unit,
    onRandomizeUserAgentChange: (Boolean) -> Unit,
    onSpoofLocaleChange: (Boolean) -> Unit,
    onSpoofTimezoneChange: (Boolean) -> Unit,
    onRandomizeCanvasChange: (Boolean) -> Unit,
    onDisableAudioContextChange: (Boolean) -> Unit,
    onDisableWebGlChange: (Boolean) -> Unit,
    onRandomizeScreenChange: (Boolean) -> Unit,
    onSpoofHardwareChange: (Boolean) -> Unit,
    onDisablePaymentChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1C1E))
    ) {
        ToggleRowInternal(
            text = "Antifingerprinting Enabled",
            checked = afpState.afpEnabled,
            onCheckedChange = onAfpEnabledChange,
            enabled = true
        )
        ToggleRowInternal(
            text = "Randomize User Agent",
            checked = afpState.randomizeUserAgent,
            onCheckedChange = onRandomizeUserAgentChange,
            enabled = afpState.afpEnabled
        )
        ToggleRowInternal(
            text = "Spoof system locale",
            checked = afpState.spoofLocale,
            onCheckedChange = onSpoofLocaleChange,
            enabled = afpState.afpEnabled
        )
        ToggleRowInternal(
            text = "Spoof system timezone",
            checked = afpState.spoofTimezone,
            onCheckedChange = onSpoofTimezoneChange,
            enabled = afpState.afpEnabled
        )
        ToggleRowInternal(
            text = "Randomize Canvas",
            checked = afpState.randomizeCanvas,
            onCheckedChange = onRandomizeCanvasChange,
            enabled = afpState.afpEnabled
        )
        ToggleRowInternal(
            text = "Disable AudioContext",
            checked = afpState.disableAudioContext,
            onCheckedChange = onDisableAudioContextChange,
            enabled = afpState.afpEnabled
        )
        ToggleRowInternal(
            text = "Disable WebGL",
            checked = afpState.disableWebGl,
            onCheckedChange = onDisableWebGlChange,
            enabled = afpState.afpEnabled
        )
        ToggleRowInternal(
            text = "Randomize Screen Dimensions",
            checked = afpState.randomizeScreen,
            onCheckedChange = onRandomizeScreenChange,
            enabled = afpState.afpEnabled
        )
        ToggleRowInternal(
            text = "Spoof Hardware Info",
            checked = afpState.spoofHardware,
            onCheckedChange = onSpoofHardwareChange,
            enabled = afpState.afpEnabled
        )
        ToggleRowInternal(
            text = "Disable Payment APIs",
            checked = afpState.disablePayment,
            onCheckedChange = onDisablePaymentChange,
            showHorizontalDivider = false,
            enabled = afpState.afpEnabled
        )
    }
}

// --- The rest of the file contains stateless, reusable UI components ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedOptionsTopAppBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { }, // Title is handled by the large text in the Column
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
    )
}

// --- Reusable Components ---

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.Gray,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun HelpText(text: String, color: Color = Color.Gray) {
    Text(
        text = text,
        color = color,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

@Composable
fun ToggleRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1C1E))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, color = Color.White, fontSize = 16.sp)

        SwitchWithConsistentThumb(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun ToggleRowInternal(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showHorizontalDivider: Boolean = true,
    enabled: Boolean = true
) {
    // Determine the text color based on the enabled state
    val textColor = if (enabled) Color.White else Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, color = textColor, fontSize = 16.sp)
        SwitchWithConsistentThumb(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
    if (showHorizontalDivider) {
        HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
    }
}

@Composable
fun SwitchWithConsistentThumb(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true // --- 5. Add the `enabled` parameter here ---
) {
    val thumbSize = 24.dp // Define a fixed size for the thumb

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            // Define your ON state colors
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            checkedBorderColor = Color.Transparent,

            // Define your OFF state colors
            uncheckedThumbColor = Color(0xFFE5E5E5),
            uncheckedTrackColor = Color(0xFF3E3E3E),
            uncheckedBorderColor = Color.Transparent
        ),
        // By providing thumbContent, we override the default resizing behavior.
        thumbContent = {
            Box(
                modifier = Modifier
                    .size(thumbSize) // Always use the same size
                    .background(
                        color = if (checked) MaterialTheme.colorScheme.onPrimary else Color(
                            0xFFE5E5E5
                        ),
                        shape = CircleShape
                    )
            )
        }
    )
}

// --- Preview ---

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun AdvancedOptionsScreenPreview() {
    LuminaTheme {
        // Pass a dummy ViewModel for previewing
        AdvancedOptionsScreen(viewModel = viewModel(), onNavigateBack = {})
    }
}