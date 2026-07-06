package com.example.lumina.features.advanced_options

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumina.features.new_lumina.NewLuminaUiState
import com.example.lumina.ui.theme.LuminaTheme

/**
 * Composable representing the Advanced Options screen.
 *
 * This screen allows users to configure advanced privacy and browser settings
 * for a Lumina instance, such as anti-fingerprinting measures,
 * and WebRTC configuration.
 *
 * @param uiState The current state of advanced options.
 * @param onWebRtcDisabledChange Callback for toggling WebRTC.
 * @param onAfpEnabledChange Callback for toggling global anti-fingerprinting.
 * @param onRandomizeUserAgentChange Callback for toggling user agent randomization.
 * @param onSpoofLocaleChange Callback for toggling locale spoofing.
 * @param onRandomizeCanvasChange Callback for toggling canvas randomization.
 * @param onDisableAudioContextChange Callback for toggling AudioContext disabling.
 * @param onDisableWebGlChange Callback for toggling WebGL disabling.
 * @param onRandomizeScreenChange Callback for toggling screen dimensions randomization.
 * @param onDisableJavascriptChange Callback for toggling JavaScript disabling.
 * @param onNavigateBack Callback function to navigate back to the previous screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedOptionsScreen(
    uiState: NewLuminaUiState,
    onWebRtcDisabledChange: (Boolean) -> Unit,
    onAfpEnabledChange: (Boolean) -> Unit,
    onRandomizeUserAgentChange: (Boolean) -> Unit,
    onSpoofLocaleChange: (Boolean) -> Unit,
    onRandomizeCanvasChange: (Boolean) -> Unit,
    onDisableAudioContextChange: (Boolean) -> Unit,
    onDisableWebGlChange: (Boolean) -> Unit,
    onRandomizeScreenChange: (Boolean) -> Unit,
    onSpoofHardwareChange: (Boolean) -> Unit,
    onDisableJavascriptChange: (Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
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

            // --- Antifingerprinting Section ---
            SectionTitle("ANTIFINGERPRINTING")
            AntifingerprintingToggles(
                afpState = uiState,
                onAfpEnabledChange = onAfpEnabledChange,
                onRandomizeUserAgentChange = onRandomizeUserAgentChange,
                onSpoofLocaleChange = onSpoofLocaleChange,
                onRandomizeCanvasChange = onRandomizeCanvasChange,
                onDisableAudioContextChange = onDisableAudioContextChange,
                onDisableWebGlChange = onDisableWebGlChange,
                onRandomizeScreenChange = onRandomizeScreenChange,
                onSpoofHardwareChange = onSpoofHardwareChange,
                onDisableJavascriptChange = onDisableJavascriptChange
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
                onCheckedChange = onWebRtcDisabledChange
            )
            HelpText(
                "WARNING: Lumina disables WebRTC by default, as enabling WebRTC will leak your real IP address. Unchecking this option will reveal your IP address to any website that uses WebRTC.",
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AntifingerprintingToggles(
    afpState: NewLuminaUiState,
    onAfpEnabledChange: (Boolean) -> Unit,
    onRandomizeUserAgentChange: (Boolean) -> Unit,
    onSpoofLocaleChange: (Boolean) -> Unit,
    onRandomizeCanvasChange: (Boolean) -> Unit,
    onDisableAudioContextChange: (Boolean) -> Unit,
    onDisableWebGlChange: (Boolean) -> Unit,
    onRandomizeScreenChange: (Boolean) -> Unit,
    onSpoofHardwareChange: (Boolean) -> Unit,
    onDisableJavascriptChange: (Boolean) -> Unit,
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
            text = "Disable JavaScript",
            checked = afpState.disableJavascript,
            onCheckedChange = onDisableJavascriptChange,
            showHorizontalDivider = false,
            enabled = afpState.afpEnabled
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedOptionsTopAppBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { },
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
            .clickable { onCheckedChange(!checked) }
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
    val textColor = if (enabled) Color.White else Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
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
    enabled: Boolean = true
) {
    val thumbSize = 24.dp

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = Color(0xFFE5E5E5),
            uncheckedTrackColor = Color(0xFF3E3E3E),
            uncheckedBorderColor = Color.Transparent
        ),
        thumbContent = {
            Box(
                modifier = Modifier
                    .size(thumbSize)
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun AdvancedOptionsScreenPreview() {
    LuminaTheme {
        AdvancedOptionsScreen(
            uiState = NewLuminaUiState(),
            onWebRtcDisabledChange = {},
            onAfpEnabledChange = {},
            onRandomizeUserAgentChange = {},
            onSpoofLocaleChange = {},
            onRandomizeCanvasChange = {},
            onDisableAudioContextChange = {},
            onDisableWebGlChange = {},
            onRandomizeScreenChange = {},
            onSpoofHardwareChange = {},
            onDisableJavascriptChange = {},
            onNavigateBack = {}
        )
    }
}
