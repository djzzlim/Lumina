package com.example.lumina.features.qr_scanner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.UUID

/**
 * Screen for scanning QR codes.
 *
 * This screen uses CameraX and ML Kit to scan for QR codes. When a valid URL is
 * detected, it triggers the [onUrlScanned] callback.
 *
 * @param onNavigateBack Callback to navigate back to the previous screen.
 * @param onUrlScanned Callback to be invoked when a URL is successfully scanned.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeScannerScreen(
    onNavigateBack: () -> Unit,
    onUrlScanned: (String) -> Unit
) {
    val context = LocalContext.current
    var hasScanned by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Scan QR Code",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {},
                actions = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFFBB86FC)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        QRCodeScannerView(
            modifier = Modifier.padding(paddingValues),
            onQrCodeScanned = { qrCodeValue ->
                if (!hasScanned) {
                    hasScanned = true

                    if (Patterns.WEB_URL.matcher(qrCodeValue).matches()) {
                        onUrlScanned(qrCodeValue)
                    } else {
                        Toast.makeText(context, "Failed: Scanned code is not a URL.", Toast.LENGTH_LONG).show()
                        onNavigateBack() // Navigate back on failure
                    }
                }
            }
        )
    }
}

/**
 * View component that handles camera permission and displays the scanner UI.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param onQrCodeScanned Callback for when a QR code is detected.
 */
@Composable
fun QRCodeScannerView(
    modifier: Modifier = Modifier,
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCameraPermission = isGranted
        }
    )

    LaunchedEffect(key1 = true) {
        val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
            hasCameraPermission = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (hasCameraPermission) {
            // Use a key to ensure the camera is fully re-initialized on subsequent navigations,
            // preventing the "disappearing TopAppBar" bug.
            key(UUID.randomUUID().toString()) {
                CameraPreview(onQrCodeScanned = onQrCodeScanned)
            }
            // Add the square border for QR code alignment
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .border(2.dp, Color.White),
                contentAlignment = Alignment.Center
            ) {}
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Camera permission is required to scan QR codes.",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

/**
 * Camera preview component that integrates with ML Kit for barcode scanning.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param onQrCodeScanned Callback for when a QR code is detected.
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                this.controller = cameraController
                cameraController.bindToLifecycle(lifecycleOwner)
            }

            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
            val barcodeScanner = BarcodeScanning.getClient(options)

            cameraController.setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(ctx),
                MlKitAnalyzer(
                    listOf(barcodeScanner),
                    ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                    ContextCompat.getMainExecutor(ctx)
                ) { result: MlKitAnalyzer.Result? ->
                    val barcodes = result?.getValue(barcodeScanner)
                    if (barcodes.isNullOrEmpty()) {
                        return@MlKitAnalyzer
                    }

                    val rawValue = barcodes.firstNotNullOfOrNull { it.rawValue }
                    if (!rawValue.isNullOrBlank()) {
                        onQrCodeScanned(rawValue)
                    }
                }
            )
            previewView
        }
    )
}
