package com.example.lumina

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Switch

// 1. CREATE a data class to hold all the state in one place.
data class AntifingerprintingState(
    val afpEnabled: Boolean = true,
    val randomizeUserAgent: Boolean = true,
    val spoofLocale: Boolean = true,
    val spoofTimezone: Boolean = true,
    val randomizeCanvas: Boolean = true,
    val disableAudioContext: Boolean = true,
    val disableWebGl: Boolean = true,
    val randomizeScreen: Boolean = true,
    val spoofHardware: Boolean = true,
    val disablePayment: Boolean = true
)
// --- Advanced Options Screen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedOptionsScreen(onNavigateBack: () -> Unit) {
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

            // --- Default View Section ---
            SectionTitle("DEFAULT VIEW")
            DefaultViewToggle()
            HelpText("Browser view uses tranditional navigation UI that automatically shows and hides as you scroll. PWA view provides minimal navigation UI which is hidden by default.")
            Spacer(modifier = Modifier.height(24.dp))

            // --- Storage Section ---
            SectionTitle("STORAGE")
            var ephemeralState by remember { mutableStateOf(true) }
            ToggleRow(
                text = "Ephemeral",
                checked = ephemeralState,
                onCheckedChange = { ephemeralState = it }
            )
            HelpText("Ephemeral lumina will automatically reset when they're closed and will not store any data")
            Spacer(modifier = Modifier.height(24.dp))

            // --- Antifingerprinting Section ---
            SectionTitle("ANTIFINGERPRINTING")
            AntifingerprintingToggles()
            HelpText(
                "Lumina has many antifingerprinting measures. Some " +
                        "websites may not be compatible with some of these " +
                        "measures enabled."
            )
            Spacer(modifier = Modifier.height(8.dp))
            HelpText(
                "If you're encountering issues with Cloudflare, try " +
                        "disabling \"Spoof system locale\""
            )
            Spacer(modifier = Modifier.height(24.dp))

            // --- WebRTC Section ---
            SectionTitle("WEBRTC")
            var webrtcState by remember { mutableStateOf(true) }
            ToggleRow(
                text = "Disable WebRTC",
                checked = webrtcState,
                onCheckedChange = { webrtcState = it }
            )
            HelpText(
                "WARNING: Lumina disables WebRTC by default, as " +
                        "enabling WebRTC will leak your real IP address. " +
                        "Unchecking this option will reveal your IP address to " +
                        "any website that uses WebRTC.",
                color = Color.Gray // Warning text is slightly dimmer
            )
            Spacer(modifier = Modifier.height(24.dp)) // Padding at the bottom
        }
    }
}

// --- Top App Bar ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedOptionsTopAppBar(
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "New Lumina",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black
        )
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
fun DefaultViewToggle() {
    var selectedView by remember { mutableStateOf("Browser") }
    val views = listOf("Browser", "PWA")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1C1C1E))
            .padding(4.dp)
    ) {
        views.forEach { view ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (selectedView == view) Color(0xFF3A3A3C) else Color.Transparent
                    )
                    .clickable { selectedView = view }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = view,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AntifingerprintingToggles() {
    var afpState by remember { mutableStateOf(AntifingerprintingState()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1C1E))
    ) {
        // --- 1. The Main Toggle ---
        // This one is always enabled.
        key("afpEnabled") {
            ToggleRowInternal(
                text = "Antifingerprinting Enabled",
                checked = afpState.afpEnabled,
                onCheckedChange = { afpState = afpState.copy(afpEnabled = it) },
                enabled = true // Explicitly enabled
            )
        }

        // --- 2. The Sub-Toggles ---
        // Pass the `afpState.afpEnabled` value to the `enabled` parameter of all other toggles.

        key("randomizeUserAgent") {
            ToggleRowInternal(
                text = "Randomize User Agent",
                checked = afpState.randomizeUserAgent,
                onCheckedChange = { afpState = afpState.copy(randomizeUserAgent = it) },
                enabled = afpState.afpEnabled // <-- This is the key change
            )
        }

        key("spoofLocale") {
            ToggleRowInternal(
                text = "Spoof system locale",
                checked = afpState.spoofLocale,
                onCheckedChange = { afpState = afpState.copy(spoofLocale = it) },
                enabled = afpState.afpEnabled // <-- This is the key change
            )
        }

        key("spoofTimezone") {
            ToggleRowInternal(
                text = "Spoof system timezone",
                checked = afpState.spoofTimezone,
                onCheckedChange = { afpState = afpState.copy(spoofTimezone = it) },
                enabled = afpState.afpEnabled // <-- This is the key change
            )
        }

        key("randomizeCanvas") {
            ToggleRowInternal(
                text = "Randomize Canvas",
                checked = afpState.randomizeCanvas,
                onCheckedChange = { afpState = afpState.copy(randomizeCanvas = it) },
                enabled = afpState.afpEnabled // <-- This is the key change
            )
        }

        key("disableAudioContext") {
            ToggleRowInternal(
                text = "Disable AudioContext",
                checked = afpState.disableAudioContext,
                onCheckedChange = { afpState = afpState.copy(disableAudioContext = it) },
                enabled = afpState.afpEnabled // <-- This is the key change
            )
        }

        key("disableWebGl") {
            ToggleRowInternal(
                text = "Disable WebGL",
                checked = afpState.disableWebGl,
                onCheckedChange = { afpState = afpState.copy(disableWebGl = it) },
                enabled = afpState.afpEnabled // <-- This is the key change
            )
        }

        key("randomizeScreen") {
            ToggleRowInternal(
                text = "Randomize Screen Dimensions",
                checked = afpState.randomizeScreen,
                onCheckedChange = { afpState = afpState.copy(randomizeScreen = it) },
                enabled = afpState.afpEnabled // <-- This is the key change
            )
        }

        key("spoofHardware") {
            ToggleRowInternal(
                text = "Spoof Hardware Info",
                checked = afpState.spoofHardware,
                onCheckedChange = { afpState = afpState.copy(spoofHardware = it) },
                enabled = afpState.afpEnabled // <-- This is the key change
            )
        }

        key("disablePayment") {
            ToggleRowInternal(
                text = "Disable Payment APIs",
                checked = afpState.disablePayment,
                onCheckedChange = { afpState = afpState.copy(disablePayment = it) },
                showHorizontalDivider = false,
                enabled = afpState.afpEnabled // <-- This is the key change
            )
        }
    }
}

@Composable
fun ToggleRowInternal(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showHorizontalDivider: Boolean = true,
    enabled: Boolean = true // --- 3. Add the `enabled` parameter here ---
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
        SwitchWithConsistentThumb(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled // --- 4. Pass it to the Switch ---
        )
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
                        color = if (checked) MaterialTheme.colorScheme.onPrimary else Color(0xFFE5E5E5),
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
    LuminaAppTheme {
        AdvancedOptionsScreen(onNavigateBack = {})
    }
}