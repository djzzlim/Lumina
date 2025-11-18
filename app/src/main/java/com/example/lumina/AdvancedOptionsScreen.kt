package com.example.lumina

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF34C759), // Green
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF2C2C2E)
            )
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
    // State for all the toggles
    var afpEnabled by remember { mutableStateOf(true) }
    var randomizeUserAgent by remember { mutableStateOf(true) }
    var spoofLocale by remember { mutableStateOf(true) }
    var spoofTimezone by remember { mutableStateOf(true) }
    var randomizeCanvas by remember { mutableStateOf(true) }
    var disableAudioContext by remember { mutableStateOf(true) }
    var disableWebGl by remember { mutableStateOf(true) }
    var randomizeScreen by remember { mutableStateOf(true) }
    var spoofHardware by remember { mutableStateOf(true) }
    var disablePayment by remember { mutableStateOf(true) }

    // Column for the list of toggles
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1C1E))
    ) {
        ToggleRowInternal(
            text = "Antifingerprinting Enabled",
            checked = afpEnabled,
            onCheckedChange = { afpEnabled = it }
        )
        HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
        ToggleRowInternal(
            text = "Randomize User Agent",
            checked = randomizeUserAgent,
            onCheckedChange = { randomizeUserAgent = it }
        )
        HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
        ToggleRowInternal(
            text = "Spoof system locale",
            checked = spoofLocale,
            onCheckedChange = { spoofLocale = it }
        )
        HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
        ToggleRowInternal(
            text = "Spoof system timezone",
            checked = spoofTimezone,
            onCheckedChange = { spoofTimezone = it }
        )
        HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
        ToggleRowInternal(
            text = "Randomize Canvas",
            checked = randomizeCanvas,
            onCheckedChange = { randomizeCanvas = it }
        )
        HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
        ToggleRowInternal(
            text = "Disable AudioContext",
            checked = disableAudioContext,
            onCheckedChange = { disableAudioContext = it }
        )
        HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
        ToggleRowInternal(
            text = "Disable WebGL",
            checked = disableWebGl,
            onCheckedChange = { disableWebGl = it }
        )
        HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
        ToggleRowInternal(
            text = "Randomize Screen Dimensions",
            checked = randomizeScreen,
            onCheckedChange = { randomizeScreen = it }
        )
        HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
        ToggleRowInternal(
            text = "Spoof Hardware Info",
            checked = spoofHardware,
            onCheckedChange = { spoofHardware = it }
        )
        HorizontalDivider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
        ToggleRowInternal(
            text = "Disable Payment APIs",
            checked = disablePayment,
            onCheckedChange = { disablePayment = it },
            showHorizontalDivider = false // No divider on the last item
        )
    }
}

@Composable
fun ToggleRowInternal(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showHorizontalDivider: Boolean = true
) {
    // This is a variation of ToggleRow without the background or clip
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, color = Color.White, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF34C759), // Green
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF2C2C2E)
            )
        )
    }
}

// --- Preview ---

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun AdvancedOptionsScreenPreview() {
    LuminaAppTheme {
        AdvancedOptionsScreen(onNavigateBack = {})
    }
}