package com.example.lumina

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeScannerScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var hasScanned by remember { mutableStateOf(false) }

    // Use Scaffold to match the structure of LuminaHomeScreen
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        // Consistent title style
                        "Scan QR Code",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp, // Match font size with "Lumina"
                        color = Color.White
                    )
                },
                navigationIcon = {}, // Keep it empty
                actions = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            // Use the same accent color as the home screen icons
                            tint = Color(0xFFBB86FC)
                        )
                    }
                },
                // Use a solid black TopAppBar to match the home screen
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        // Use a solid black container color
        containerColor = Color.Black
    ) { paddingValues ->
        QRCodeScannerView(
            modifier = Modifier.padding(paddingValues),
            onQrCodeScanned = { qrCodeValue ->
                if (!hasScanned) {
                    hasScanned = true
                    Toast.makeText(context, "Scanned: $qrCodeValue", Toast.LENGTH_LONG).show()
                    Log.d("QRCodeScanner", "Scanned value: $qrCodeValue")
                    onNavigateBack()
                }
            }
        )
    }
}


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
            CameraPreview(onQrCodeScanned = onQrCodeScanned)
        } else {
            // This UI remains consistent as it's on a black background
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

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasScanned by remember { mutableStateOf(false) }

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
                    if (hasScanned) return@MlKitAnalyzer

                    val barcodes = result?.getValue(barcodeScanner)
                    if (barcodes.isNullOrEmpty()) {
                        return@MlKitAnalyzer
                    }

                    val rawValue = barcodes.firstNotNullOfOrNull { it.rawValue }
                    if (!rawValue.isNullOrBlank()) {
                        hasScanned = true
                        onQrCodeScanned(rawValue)
                    }
                }
            )
            previewView
        }
    )
}
