package com.prusoft.easybiglauncher.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import android.graphics.Bitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.prusoft.easybiglauncher.R
import com.prusoft.easybiglauncher.utils.ToolManager
import com.google.common.util.concurrent.ListenableFuture

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagnifierScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var zoom by remember { mutableStateOf(1f) }
    var isFlashOn by remember { mutableStateOf(false) }
    
    var frozenBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedFilter by remember { mutableStateOf(0) } // 0: Normal, 1: Grayscale, 2: Invert, 3: Yellow, 4: Blue, 5: Red
    var customPreviewView by remember { mutableStateOf<PreviewView?>(null) }
    
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? by remember { mutableStateOf(null) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.magnifier)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (hasCameraPermission && cameraProviderFuture != null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                        customPreviewView = previewView
                        
                        cameraProviderFuture?.addListener({
                            val cameraProvider = cameraProviderFuture?.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider?.unbindAll()
                                camera = cameraProvider?.bindToLifecycle(
                                    lifecycleOwner, cameraSelector, preview
                                )
                                // Observe flash state
                            } catch(e: Exception) {
                                // Handle exception
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        
                        previewView
                    },
                    update = { view ->
                        camera?.cameraControl?.setZoomRatio(zoom)
                        camera?.cameraControl?.enableTorch(isFlashOn)
                    }
                )
                
                frozenBitmap?.let { bitmap ->
                    val colorFilter = when (selectedFilter) {
                        1 -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                        2 -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                            -1f, 0f, 0f, 0f, 255f,
                            0f, -1f, 0f, 0f, 255f,
                            0f, 0f, -1f, 0f, 255f,
                            0f, 0f, 0f, 1f, 0f
                        )))
                        3 -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                            1f, 0f, 0f, 0f, 0f,
                            0f, 1f, 0f, 0f, 0f,
                            0f, 0f, 0f, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        ))) // Yellow (R+G)
                        4 -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                            0f, 0f, 0f, 0f, 0f,
                            0f, 0f, 0f, 0f, 0f,
                            0f, 0f, 1f, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        ))) // Blue
                        5 -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                            1f, 0f, 0f, 0f, 0f,
                            0f, 0f, 0f, 0f, 0f,
                            0f, 0f, 0f, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        ))) // Red
                        else -> null
                    }
                    
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Frozen",
                        modifier = Modifier.fillMaxSize(),
                        colorFilter = colorFilter,
                        contentScale = ContentScale.Crop
                    )
                }
            } else if (!hasCameraPermission) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Kamera izni gerekiyor.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("İzin Ver")
                    }
                }
            }

            // Controls at the bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Slider(
                    value = zoom,
                    onValueChange = { zoom = it },
                    valueRange = 1f..5f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { 
                            isFlashOn = !isFlashOn
                            camera?.cameraControl?.enableTorch(isFlashOn)
                        },
                        modifier = Modifier.size(80.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFlashOn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        )
                    ) {
                        Icon(
                            if (isFlashOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                            contentDescription = "Flaş",
                            modifier = Modifier.size(40.dp),
                            tint = if (isFlashOn) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Button(
                        onClick = { 
                            if (frozenBitmap == null) {
                                frozenBitmap = customPreviewView?.bitmap
                            } else {
                                frozenBitmap = null
                                selectedFilter = 0
                            }
                        },
                        modifier = Modifier.size(80.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (frozenBitmap != null) Color.Red else MaterialTheme.colorScheme.surfaceVariant,
                        )
                    ) {
                        Icon(
                            if (frozenBitmap != null) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Durdur",
                            modifier = Modifier.size(40.dp),
                            tint = if (frozenBitmap != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (frozenBitmap != null) {
                        val filterName = when (selectedFilter) {
                            0 -> stringResource(R.string.filter_normal)
                            1 -> stringResource(R.string.filter_bw)
                            2 -> stringResource(R.string.filter_invert)
                            3 -> stringResource(R.string.filter_yellow)
                            4 -> stringResource(R.string.filter_blue)
                            5 -> stringResource(R.string.filter_red)
                            else -> ""
                        }
                        Button(
                            onClick = { selectedFilter = (selectedFilter + 1) % 6 },
                            modifier = Modifier.size(80.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            )
                        ) {
                            Text(
                                text = filterName,
                                fontSize = 12.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        // Invisible placeholder to keep center alignment
                        Spacer(modifier = Modifier.size(80.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
