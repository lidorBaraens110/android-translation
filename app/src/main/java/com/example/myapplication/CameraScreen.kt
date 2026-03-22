package com.example.myapplication

import com.example.myapplication.ui.TranslatorViewModel
import com.example.myapplication.util.uriToBitmap
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.myapplication.ui.theme.BackgroundWhite
import com.example.myapplication.ui.theme.Blue500
import com.example.myapplication.ui.theme.DividerGray
import com.example.myapplication.ui.theme.SurfaceGray
import com.example.myapplication.ui.theme.TextPrimary
import com.example.myapplication.ui.theme.TextSecondary
import java.io.File

@Composable
fun CameraScreen(navController: NavController, viewModel: TranslatorViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sourceLanguage by viewModel.sourceLanguage.collectAsState()
    val targetLanguage by viewModel.targetLanguage.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var flashEnabled by remember { mutableStateOf(false) }
    var showSourceDropdown by remember { mutableStateOf(false) }
    var showTargetDropdown by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = uriToBitmap(context, it)
            viewModel.runOcr(bitmap) { navController.navigate("result") }
        }
    }

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(flashEnabled) {
        imageCapture.flashMode = if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }

    DisposableEffect(hasCameraPermission) {
        if (!hasCameraPermission) return@DisposableEffect onDispose {}
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val cameraProvider = future.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch (_: Exception) {}
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            try { ProcessCameraProvider.getInstance(context).get()?.unbindAll() } catch (_: Exception) {}
        }
    }

    // Animated scan line
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // Camera preview
        if (hasCameraPermission) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Camera permission required", color = Color.White, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant Permission")
                    }
                }
            }
        }

        // Focus brackets + scan line overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rectW = size.width * 0.65f
            val rectH = size.height * 0.42f
            val left = (size.width - rectW) / 2f
            val top = (size.height - rectH) / 2f - size.height * 0.06f
            val bracketLen = 48.dp.toPx()
            val strokeW = 2.dp.toPx()
            val bracketColor = Color.White.copy(alpha = 0.85f)

            // dim overall
            drawRect(color = Color.Black.copy(alpha = 0.25f), size = Size(size.width, size.height))

            // corner brackets — top-left
            drawLine(bracketColor, Offset(left, top), Offset(left + bracketLen, top), strokeW)
            drawLine(bracketColor, Offset(left, top), Offset(left, top + bracketLen), strokeW)
            // top-right
            drawLine(bracketColor, Offset(left + rectW - bracketLen, top), Offset(left + rectW, top), strokeW)
            drawLine(bracketColor, Offset(left + rectW, top), Offset(left + rectW, top + bracketLen), strokeW)
            // bottom-left
            drawLine(bracketColor, Offset(left, top + rectH - bracketLen), Offset(left, top + rectH), strokeW)
            drawLine(bracketColor, Offset(left, top + rectH), Offset(left + bracketLen, top + rectH), strokeW)
            // bottom-right
            drawLine(bracketColor, Offset(left + rectW - bracketLen, top + rectH), Offset(left + rectW, top + rectH), strokeW)
            drawLine(bracketColor, Offset(left + rectW, top + rectH - bracketLen), Offset(left + rectW, top + rectH), strokeW)

            // animated scan line
            val lineY = top + scanLineProgress * rectH
            drawLine(
                color = Blue500.copy(alpha = 0.85f),
                start = Offset(left + 8.dp.toPx(), lineY),
                end = Offset(left + rectW - 8.dp.toPx(), lineY),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Top bar (close | language pill | flash)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.Close, "Close", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            // Language pill with dropdowns
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Text(
                            sourceLanguage,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showSourceDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showSourceDropdown,
                            onDismissRequest = { showSourceDropdown = false },
                            modifier = Modifier.background(Color.DarkGray)
                        ) {
                            viewModel.languageOptions.keys.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang, color = Color.White) },
                                    onClick = {
                                        viewModel.setSourceLanguage(lang)
                                        showSourceDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = "Swap",
                        tint = Color.White,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { viewModel.swapLanguages() }
                    )
                    Spacer(Modifier.width(6.dp))
                    Box {
                        Text(
                            targetLanguage,
                            color = Blue500,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showTargetDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showTargetDropdown,
                            onDismissRequest = { showTargetDropdown = false },
                            modifier = Modifier.background(Color.DarkGray)
                        ) {
                            viewModel.languageOptions.keys.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang, color = Color.White) },
                                    onClick = {
                                        viewModel.setTargetLanguage(lang)
                                        showTargetDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Flash
            IconButton(
                onClick = { flashEnabled = !flashEnabled },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    "Flash",
                    tint = if (flashEnabled) Color.Yellow else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Hint text
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 240.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("כוון אל הטקסט לתרגום", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("מזהה שפה...", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
            }
        }

        // Bottom panel (white rounded-top sheet)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = BackgroundWhite,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Drag handle
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 4.dp)
                            .width(48.dp)
                            .height(4.dp)
                            .background(DividerGray, RoundedCornerShape(2.dp))
                    )

                    // Gallery / Capture / History
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gallery
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { galleryLauncher.launch("image/*") }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(SurfaceGray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Image, "Gallery", tint = TextPrimary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("גלריה", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        }

                        // Capture FAB
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .shadow(elevation = 6.dp, shape = CircleShape)
                                .clip(CircleShape)
                                .background(Blue500)
                                .clickable(enabled = !isProcessing) {
                                    val file = File(context.cacheDir, "ocr_${System.currentTimeMillis()}.jpg")
                                    val options = ImageCapture.OutputFileOptions.Builder(file).build()
                                    imageCapture.takePicture(
                                        options,
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                                if (bitmap != null) {
                                                    viewModel.runOcr(bitmap) {
                                                        navController.navigate("result")
                                                    }
                                                }
                                            }
                                            override fun onError(e: ImageCaptureException) {}
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp), strokeWidth = 2.5.dp)
                            } else {
                                Icon(Icons.Default.PhotoCamera, "Capture", tint = Color.White, modifier = Modifier.size(40.dp))
                            }
                        }

                        // History / Refresh
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { navController.navigate("home") }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(SurfaceGray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Refresh, "History", tint = TextPrimary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("History", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Simple bottom nav tabs (camera active)
                    HorizontalDivider(color = DividerGray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f).clickable { navController.navigate("home") }
                        ) {
                            Icon(Icons.Default.Translate, null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                            Text("תרגום", fontSize = 10.sp, color = TextSecondary)
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Mic, null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                            Text("מיקרופון", fontSize = 10.sp, color = TextSecondary)
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoCamera, null, tint = Blue500, modifier = Modifier.size(24.dp))
                            Text("מצלמה", fontSize = 10.sp, color = Blue500, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
