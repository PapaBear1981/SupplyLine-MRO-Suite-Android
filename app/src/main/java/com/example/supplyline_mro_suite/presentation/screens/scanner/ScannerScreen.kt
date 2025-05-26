package com.example.supplyline_mro_suite.presentation.screens.scanner

import android.Manifest
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.example.supplyline_mro_suite.ui.theme.*
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(navController: NavController) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var isScanning by remember { mutableStateOf(false) }
    var scannedCode by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "QR/Barcode Scanner",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.7f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                cameraPermissionState.status.isGranted -> {
                    CameraPreview(
                        onCodeScanned = { code ->
                            scannedCode = code
                            showResult = true
                            isScanning = false
                        },
                        isScanning = isScanning,
                        onScanningChange = { isScanning = it }
                    )
                }
                cameraPermissionState.status.shouldShowRationale -> {
                    PermissionRationale(
                        onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                    )
                }
                else -> {
                    PermissionRequest(
                        onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                    )
                }
            }
            
            // Scanning overlay
            if (cameraPermissionState.status.isGranted && !showResult) {
                ScanningOverlay(
                    isScanning = isScanning,
                    onStartScanning = { isScanning = true }
                )
            }
            
            // Result dialog
            if (showResult) {
                ScanResultDialog(
                    scannedCode = scannedCode,
                    onDismiss = { 
                        showResult = false
                        scannedCode = ""
                    },
                    onScanAgain = {
                        showResult = false
                        scannedCode = ""
                        isScanning = true
                    }
                )
            }
        }
    }
}

@Composable
fun CameraPreview(
    onCodeScanned: (String) -> Unit,
    isScanning: Boolean,
    onScanningChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                
                // Here you would add ML Kit barcode scanning
                // For now, we'll simulate scanning
                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    if (isScanning) {
                        // Simulate barcode detection
                        // In real implementation, use ML Kit BarcodeScanning
                        onScanningChange(false)
                        onCodeScanned("HT001") // Simulated scan result
                    }
                    imageProxy.close()
                }
                
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    // Handle camera binding error
                }
            }, executor)
            
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun ScanningOverlay(
    isScanning: Boolean,
    onStartScanning: () -> Unit
) {
    val scanLineAnimation by rememberInfiniteTransition(label = "scan_line").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_line_position"
    )
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Scanning frame
        Box(
            modifier = Modifier.size(250.dp)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val strokeWidth = 4.dp.toPx()
                val cornerLength = 30.dp.toPx()
                
                // Draw corner brackets
                // Top-left
                drawLine(
                    color = AerospaceAccent,
                    start = androidx.compose.ui.geometry.Offset(0f, cornerLength),
                    end = androidx.compose.ui.geometry.Offset(0f, 0f),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = AerospaceAccent,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(cornerLength, 0f),
                    strokeWidth = strokeWidth
                )
                
                // Top-right
                drawLine(
                    color = AerospaceAccent,
                    start = androidx.compose.ui.geometry.Offset(size.width - cornerLength, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = AerospaceAccent,
                    start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, cornerLength),
                    strokeWidth = strokeWidth
                )
                
                // Bottom-left
                drawLine(
                    color = AerospaceAccent,
                    start = androidx.compose.ui.geometry.Offset(0f, size.height - cornerLength),
                    end = androidx.compose.ui.geometry.Offset(0f, size.height),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = AerospaceAccent,
                    start = androidx.compose.ui.geometry.Offset(0f, size.height),
                    end = androidx.compose.ui.geometry.Offset(cornerLength, size.height),
                    strokeWidth = strokeWidth
                )
                
                // Bottom-right
                drawLine(
                    color = AerospaceAccent,
                    start = androidx.compose.ui.geometry.Offset(size.width - cornerLength, size.height),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = AerospaceAccent,
                    start = androidx.compose.ui.geometry.Offset(size.width, size.height),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height - cornerLength),
                    strokeWidth = strokeWidth
                )
                
                // Animated scan line
                if (isScanning) {
                    val lineY = size.height * scanLineAnimation
                    drawLine(
                        color = AerospaceAccent,
                        start = androidx.compose.ui.geometry.Offset(0f, lineY),
                        end = androidx.compose.ui.geometry.Offset(size.width, lineY),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }
        
        // Instructions
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isScanning) {
                Button(
                    onClick = onStartScanning,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AerospaceAccent
                    )
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Start Scanning",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                )
            ) {
                Text(
                    text = if (isScanning) "Scanning for QR codes and barcodes..." else "Tap the camera button to start scanning",
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ScanResultDialog(
    scannedCode: String,
    onDismiss: () -> Unit,
    onScanAgain: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AerospaceSuccess,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Successful")
            }
        },
        text = {
            Column {
                Text("Scanned code:")
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AerospacePrimary.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = scannedCode,
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold,
                        color = AerospacePrimary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onScanAgain,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AerospaceAccent
                )
            ) {
                Text("Scan Again")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun PermissionRequest(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = AerospacePrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Camera Permission Required",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "To scan QR codes and barcodes, please grant camera permission.",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = AerospacePrimary
            )
        ) {
            Text("Grant Permission")
        }
    }
}

@Composable
fun PermissionRationale(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = AerospaceWarning
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Camera Access Needed",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "The camera is essential for scanning tool and chemical barcodes. This helps you quickly identify and manage inventory items.",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = AerospaceWarning
            )
        ) {
            Text("Allow Camera Access")
        }
    }
}
