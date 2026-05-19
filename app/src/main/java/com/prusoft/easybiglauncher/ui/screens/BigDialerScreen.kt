package com.prusoft.easybiglauncher.ui.screens

import android.media.AudioManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.prusoft.easybiglauncher.R
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BigDialerScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Dialer, 1: Contacts, 2: History
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.btn_phone), fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(32.dp))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Tab Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TabButton(stringResource(R.string.tab_numpad), isSelected = selectedTab == 0, modifier = Modifier.weight(1f)) { selectedTab = 0 }
                TabButton(stringResource(R.string.tab_favorites), isSelected = selectedTab == 1, modifier = Modifier.weight(1f)) { selectedTab = 1 }
                TabButton(stringResource(R.string.tab_contacts), isSelected = selectedTab == 2, modifier = Modifier.weight(1f)) { selectedTab = 2 }
                TabButton(stringResource(R.string.tab_history), isSelected = selectedTab == 3, modifier = Modifier.weight(1f)) { selectedTab = 3 }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> DialerContent(context)
                    1 -> FavoritesScreen()
                    2 -> BigContactsScreen()
                    3 -> CallHistoryScreen()
                }
            }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun DialerContent(context: Context) {
    var number by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Number Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }

        // Dial Pad
        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#")
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(keys) { key ->
                DialerButton(key) {
                    if (number.length < 15) number += key
                }
            }
        }

        // Bottom Actions
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Delete Button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (number.isNotEmpty()) number = number.dropLast(1)
                            },
                            onPress = {
                                val job = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                    delay(500)
                                    while (true) {
                                        if (number.isNotEmpty()) {
                                            number = number.dropLast(1)
                                        }
                                        delay(100)
                                    }
                                }
                                tryAwaitRelease()
                                job.cancel()
                            }
                        )
                    },
                color = Color.Gray,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Default.Backspace, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.White)
                }
            }

            // Call Button
            Button(
                onClick = {
                    if (number.isNotEmpty()) {
                        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        audioManager.mode = AudioManager.MODE_IN_CALL
                        audioManager.isSpeakerphoneOn = true

                        val intent = Intent(Intent.ACTION_CALL).apply {
                            data = Uri.parse("tel:$number")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to DIAL if CALL is not permitted
                            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$number")
                            }
                            context.startActivity(dialIntent)
                        }
                    }
                },
                modifier = Modifier
                    .weight(2f)
                    .height(90.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.call), fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DialerButton(text: String, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier.height(100.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Text(text = text, fontSize = 36.sp, fontWeight = FontWeight.Bold)
    }
}
