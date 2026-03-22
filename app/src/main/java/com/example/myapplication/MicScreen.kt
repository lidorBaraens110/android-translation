package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.myapplication.ui.TranslatorViewModel
import com.example.myapplication.ui.theme.BackgroundLight
import com.example.myapplication.ui.theme.Blue500
import com.example.myapplication.ui.theme.TextPrimary
import com.example.myapplication.ui.theme.TextSecondary

@Composable
fun MicScreen(navController: NavController, viewModel: TranslatorViewModel) {
    val context = LocalContext.current
    val sourceLanguage by viewModel.sourceLanguage.collectAsState()

    val isListening = remember { mutableStateOf(false) }
    val partialText = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf("") }
    val rmsLevel = remember { mutableFloatStateOf(0f) }

    val hasPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission.value = granted }

    val speechLocale = when (sourceLanguage) {
        "Hebrew" -> "iw-IL"
        "Arabic" -> "ar-SA"
        else -> "en-US"
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    fun startListening() {
        errorMessage.value = ""
        partialText.value = ""
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLocale)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer.startListening(intent)
    }

    DisposableEffect(Unit) {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening.value = true
                errorMessage.value = ""
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {
                rmsLevel.floatValue = (rmsdB.coerceIn(0f, 10f) / 10f)
            }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening.value = false
                rmsLevel.floatValue = 0f
            }
            override fun onError(error: Int) {
                isListening.value = false
                rmsLevel.floatValue = 0f
                errorMessage.value = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Tap mic to try again."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out. Tap mic to try again."
                    SpeechRecognizer.ERROR_NETWORK -> "Network error. Check your connection."
                    SpeechRecognizer.ERROR_AUDIO -> "Audio error. Try again."
                    else -> "Could not recognize speech. Tap mic to try again."
                }
            }
            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: ""
                isListening.value = false
                rmsLevel.floatValue = 0f
                if (text.isNotBlank()) {
                    viewModel.setInputText(text)
                    navController.popBackStack()
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: ""
                partialText.value = text
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        onDispose {
            speechRecognizer.destroy()
        }
    }

    LaunchedEffect(hasPermission.value) {
        if (hasPermission.value) startListening()
        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // Pulse animation tied to rms level when listening, idle pulse when not
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idlePulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Back button
        IconButton(
            onClick = {
                speechRecognizer.stopListening()
                navController.popBackStack()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 8.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Language label
            Text(
                text = "Listening in $sourceLanguage",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(48.dp))

            // Pulsing mic button
            val outerScale = if (isListening.value) 1f + rmsLevel.floatValue * 0.5f else idlePulse
            val midScale = if (isListening.value) 1f + rmsLevel.floatValue * 0.3f else 1f

            Box(contentAlignment = Alignment.Center) {
                // Outer ring
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(outerScale)
                        .background(
                            if (isListening.value) Blue500.copy(alpha = 0.15f)
                            else Blue500.copy(alpha = 0.08f),
                            CircleShape
                        )
                )
                // Mid ring
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(midScale)
                        .background(
                            if (isListening.value) Blue500.copy(alpha = 0.25f)
                            else Blue500.copy(alpha = 0.12f),
                            CircleShape
                        )
                )
                // Mic button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            if (isListening.value) Blue500 else Blue500.copy(alpha = 0.7f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isListening.value) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Mic",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // Status / partial text
            when {
                isListening.value && partialText.value.isEmpty() -> {
                    Text(
                        "Listening...",
                        color = Blue500,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                partialText.value.isNotEmpty() -> {
                    Text(
                        partialText.value,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    )
                }
                errorMessage.value.isNotEmpty() -> {
                    Text(
                        errorMessage.value,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { startListening() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue500)
                    ) {
                        Icon(Icons.Default.Mic, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Try again", color = Color.White)
                    }
                }
                else -> {
                    Text(
                        "Preparing microphone...",
                        color = TextSecondary,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
