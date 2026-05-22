package com.prusoft.easybiglauncher.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
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

import androidx.compose.ui.res.stringResource
import com.prusoft.easybiglauncher.R

import com.prusoft.easybiglauncher.viewmodel.LauncherViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prusoft.easybiglauncher.utils.TTSManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val context = LocalContext.current
    val isFlashOn by ToolManager.isFlashlightOn.collectAsState()
    val soundMode by ToolManager.soundMode.collectAsState()

    val sharedPref = remember { context.getSharedPreferences("sos_prefs", Context.MODE_PRIVATE) }
    val showOtherTools = sharedPref.getBoolean("show_other_tools", true)

    LaunchedEffect(Unit) {
        ToolManager.updateState(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("${stringResource(R.string.tools_title)}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.go_back), modifier = Modifier.size(40.dp))
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
                    text = if (isFlashOn) stringResource(R.string.flashlight_on) else stringResource(R.string.flashlight_off),
                    icon = if (isFlashOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                    color = if (isFlashOn) Color(0xFFFFEB3B) else Color.DarkGray,
                    contentColor = if (isFlashOn) Color.Black else Color.White,
                    onClick = { ToolManager.toggleFlashlight(context) }
                )
            }
            item {
                val soundInfo = getSoundModeInfo(soundMode)
                val soundModeText = "${stringResource(R.string.sound_mode)}: ${stringResource(soundInfo.first)} / ${stringResource(soundInfo.second)}"
                ToolButton(
                    text = soundModeText,
                    icon = soundInfo.third,
                    color = soundInfo.fourth,
                    contentColor = Color.White,
                    onClick = { ToolManager.cycleSoundMode(context) }
                )
            }
            item {
                ToolButton(
                    text = stringResource(R.string.reminders_title),
                    icon = Icons.Default.Alarm,
                    color = Color(0xFF9C27B0), // Purple
                    contentColor = Color.White,
                    onClick = { navController.navigate("reminders") }
                )
            }
            item {
                ToolButton(
                    text = stringResource(R.string.magnifier),
                    icon = Icons.Default.ZoomIn,
                    color = Color(0xFF009688), // Teal
                    contentColor = Color.White,
                    onClick = { navController.navigate("magnifier") }
                )
            }
            item {
                ToolButton(
                    text = stringResource(R.string.panic_siren),
                    icon = Icons.Default.Warning,
                    color = Color(0xFFF44336), // Red
                    contentColor = Color.White,
                    onClick = { navController.navigate("siren") }
                )
            }
            
            if (showOtherTools) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.other_tools),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    val wifiSettingsText = stringResource(R.string.wifi_settings)
                    ToolButton(
                        text = wifiSettingsText,
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
                    val bluetoothSettingsText = stringResource(R.string.bluetooth_settings)
                    ToolButton(
                        text = bluetoothSettingsText,
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
                item {
                    val mobileDataSettingsText = stringResource(R.string.mobile_data_settings)
                    ToolButton(
                        text = mobileDataSettingsText,
                        icon = Icons.Default.CellTower,
                        color = Color(0xFFFF9800),
                        contentColor = Color.White,
                        onClick = {
                            val intent = Intent(Settings.ACTION_DATA_USAGE_SETTINGS)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val fallback = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(fallback)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolButton(
    text: String,
    icon: ImageVector,
    color: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        color = color,
        contentColor = contentColor,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
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

fun getSoundModeInfo(mode: Int): Triple4<Int, Int, ImageVector, Color> {
    return when (mode) {
        AudioManager.RINGER_MODE_NORMAL -> Triple4(R.string.ses_ring, R.string.sound_ring, Icons.Default.VolumeUp, Color(0xFF4CAF50))
        AudioManager.RINGER_MODE_VIBRATE -> Triple4(R.string.ses_vibrate, R.string.sound_vibrate, Icons.Default.Vibration, Color(0xFFFF9800))
        else -> Triple4(R.string.ses_silent, R.string.sound_silent, Icons.Default.VolumeOff, Color(0xFFF44336))
    }
}

data class Triple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
