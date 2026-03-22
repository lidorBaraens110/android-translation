package com.example.myapplication

import com.example.myapplication.data.model.RecentTranslation
import com.example.myapplication.ui.TranslatorViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.ui.theme.BackgroundLight
import com.example.myapplication.ui.theme.BackgroundWhite
import com.example.myapplication.ui.theme.Blue500
import com.example.myapplication.ui.theme.DividerGray
import com.example.myapplication.ui.theme.TextPrimary
import com.example.myapplication.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: TranslatorViewModel) {
    val sourceLanguage by viewModel.sourceLanguage.collectAsState()
    val targetLanguage by viewModel.targetLanguage.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val recentTranslations by viewModel.recentTranslations.collectAsState()

    var showSourceDropdown by remember { mutableStateOf(false) }
    var showTargetDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Translator", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Account", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundWhite)
            )
        },
        bottomBar = {
            TranslatorBottomNav(
                selectedTab = 0,
                onTranslateClick = {},
                onVoiceClick = { navController.navigate("mic") },
                onCameraClick = { navController.navigate("camera") }
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            // Language selector
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundWhite, RoundedCornerShape(12.dp))
                        .border(1.dp, DividerGray, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TextButton(
                            onClick = { showSourceDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(sourceLanguage, color = Blue500, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        DropdownMenu(
                            expanded = showSourceDropdown,
                            onDismissRequest = { showSourceDropdown = false },
                            modifier = Modifier.background(BackgroundWhite)
                        ) {
                            viewModel.languageOptions.keys.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang, color = TextPrimary) },
                                    onClick = {
                                        viewModel.setSourceLanguage(lang)
                                        showSourceDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(onClick = { viewModel.swapLanguages() }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Swap", tint = TextSecondary)
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        TextButton(
                            onClick = { showTargetDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(targetLanguage, color = Blue500, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        DropdownMenu(
                            expanded = showTargetDropdown,
                            onDismissRequest = { showTargetDropdown = false },
                            modifier = Modifier.background(BackgroundWhite)
                        ) {
                            viewModel.languageOptions.keys.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang, color = TextPrimary) },
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

            // Text input
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundWhite, RoundedCornerShape(12.dp))
                        .border(1.dp, DividerGray, RoundedCornerShape(12.dp))
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { viewModel.setInputText(it) },
                        placeholder = {
                            Text("Enter text to translate...", color = TextSecondary, fontSize = 16.sp)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(fontSize = 16.sp, color = TextPrimary),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (inputText.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.setInputText("") },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                }
            }

            // Inline translation result
            item {
                val translatedText by viewModel.translatedText.collectAsState()
                if (isProcessing || translatedText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundWhite, RoundedCornerShape(12.dp))
                            .border(1.dp, Blue500.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        if (isProcessing && translatedText.isEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = Blue500, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Translating...", color = TextSecondary, fontSize = 14.sp)
                            }
                        } else {
                            Text(
                                text = translatedText,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                lineHeight = 26.sp
                            )
                        }
                    }
                }
            }

            // Recent header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "RECENT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    if (recentTranslations.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearRecentTranslations() }) {
                            Text("Clear all", color = Blue500, fontSize = 13.sp)
                        }
                    }
                }
            }

            if (recentTranslations.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No recent translations", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                items(recentTranslations) { translation ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundWhite, RoundedCornerShape(8.dp))
                            .border(1.dp, DividerGray, RoundedCornerShape(8.dp))
                            .clickable {
                                viewModel.setInputText(translation.sourceText)
                                viewModel.setTranslatedText(translation.translatedText)
                                navController.navigate("result")
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                translation.preview,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(translation.translatedPreview, color = TextSecondary, fontSize = 12.sp)
                        }
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = DividerGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TranslatorBottomNav(
    selectedTab: Int,
    onTranslateClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = DividerGray, thickness = 1.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .background(BackgroundWhite)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Translate tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onTranslateClick)
                ) {
                    Icon(
                        Icons.Default.Translate,
                        contentDescription = "Translate",
                        tint = if (selectedTab == 0) Blue500 else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "תרגום",
                        fontSize = 10.sp,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) Blue500 else TextSecondary
                    )
                }

                // Center FAB mic — offset upward
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .offset(y = (-18).dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(elevation = 6.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(Blue500)
                            .clickable(onClick = onVoiceClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Voice",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "מיקרופון",
                        fontSize = 10.sp,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) Blue500 else TextSecondary
                    )
                }

                // Camera tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onCameraClick)
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = "Camera",
                        tint = if (selectedTab == 2) Blue500 else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "מצלמה",
                        fontSize = 10.sp,
                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 2) Blue500 else TextSecondary
                    )
                }
            }
        }
    }
}
