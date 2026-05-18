package com.prusoft.easybiglauncher.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.prusoft.easybiglauncher.utils.ToolManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(navController: NavController) {
    val context = LocalContext.current
    val isFlashOn by ToolManager.isFlashlightOn.collectAsState()
    val soundMode by ToolManager.soundMode.collectAsState()

    LaunchedEffect(Unit) {
        ToolManager.updateState(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ARAÇLAR / TOOLS", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", modifier = Modifier.size(40.dp))
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ToolButton(
                    text = if (isFlashOn) "FENER: AÇIK\nFLASHLIGHT: ON" else "FENER: KAPALI\nFLASHLIGHT: OFF",
                    icon = if (isFlashOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                    color = if (isFlashOn) Color(0xFFFFEB3B) else Color.DarkGray,
                    contentColor = if (isFlashOn) Color.Black else Color.White,
                    onClick = { ToolManager.toggleFlashlight(context) }
                )
            }
            item {
                val soundInfo = getSoundModeInfo(soundMode)
                ToolButton(
                    text = "SES MODU: ${soundInfo.first}\nSOUND: ${soundInfo.second}",
                    icon = soundInfo.third,
                    color = soundInfo.fourth,
                    contentColor = Color.White,
                    onClick = { ToolManager.cycleSoundMode(context) }
                )
            }
            item {
                ToolButton(
                    text = "WI-FI AYARLARI\nWI-FI SETTINGS",
                    icon = Icons.Default.Wifi,
                    color = Color(0xFF2196F3),
                    contentColor = Color.White,
                    onClick = {
                        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                )
            }
            item {
                ToolButton(
                    text = "BLUETOOTH AYARLARI\nBLUETOOTH SETTINGS",
                    icon = Icons.Default.Bluetooth,
                    color = Color(0xFF3F51B5),
                    contentColor = Color.White,
                    onClick = {
                        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun ToolButton(
    text: String,
    icon: ImageVector,
    color: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.cardColors(containerColor = color, contentColor = contentColor),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterHorizontally,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = text,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 28.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

fun getSoundModeInfo(mode: Int): Triple4<String, String, ImageVector, Color> {
    return when (mode) {
        AudioManager.RINGER_MODE_NORMAL -> Triple4("SESLİ", "RING", Icons.Default.VolumeUp, Color(0xFF4CAF50))
        AudioManager.RINGER_MODE_VIBRATE -> Triple4("TİTREŞİM", "VIBRATE", Icons.Default.Vibration, Color(0xFFFF9800))
        else -> Triple4("SESSİZ", "SILENT", Icons.Default.VolumeOff, Color(0xFFF44336))
    }
}

data class Triple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
