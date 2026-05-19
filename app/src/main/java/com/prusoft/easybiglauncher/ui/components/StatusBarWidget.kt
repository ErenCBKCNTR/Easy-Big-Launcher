package com.prusoft.easybiglauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.prusoft.easybiglauncher.utils.rememberBatteryStatus
import com.prusoft.easybiglauncher.utils.rememberCurrentTime
import com.prusoft.easybiglauncher.utils.rememberSignalStrength
import android.content.Intent
import android.provider.AlarmClock
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import com.prusoft.easybiglauncher.utils.TTSManager
import com.prusoft.easybiglauncher.data.SecurityRepository
import androidx.compose.runtime.collectAsState
import android.app.Application
import com.prusoft.easybiglauncher.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.style.TextAlign
import java.util.Calendar

@Composable
fun StatusBarWidget(isTtsEnabled: Boolean = false) {
    val context = LocalContext.current
    val securityRepository = remember { SecurityRepository(context.applicationContext as Application) }
    val clockTapAction by securityRepository.clockTapAction.collectAsState(initial = 2)
    
    val ttsManager = remember { TTSManager.getInstance(context) }
    val time by rememberCurrentTime()
    val date = remember { SimpleDateFormat("dd MMMM EEEE", Locale.getDefault()).format(Date()) }
    val battery by rememberBatteryStatus(context)
    val signalLevel by rememberSignalStrength(context)
    
    var showCalendarDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 24.dp), 
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.clickable {
            when (clockTapAction) {
                0 -> ttsManager.speak(context.getString(R.string.clock_read_template, date, time))
                1 -> showCalendarDialog = true
                2 -> {
                    ttsManager.speak(context.getString(R.string.clock_read_template, date, time))
                    showCalendarDialog = true
                }
            }
        }) {
            Text(text = time, style = MaterialTheme.typography.displayMedium, fontSize = 52.sp, fontWeight = FontWeight.Black, modifier = Modifier.semantics { contentDescription = "Saat $time" })
            Text(text = date, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { contentDescription = "Tarih $date" })
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Signal Strength Indicator
            SignalIndicator(level = signalLevel)
            
            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "${battery.percentage}%",
                style = MaterialTheme.typography.headlineLarge,
                color = if (battery.percentage <= 15 && !battery.isCharging) Color.Red else Color.Black,
                fontWeight = FontWeight.Black,
                modifier = Modifier.semantics { contentDescription = "Pil yüzde ${battery.percentage}" }.clickable {
                    if (isTtsEnabled) {
                        ttsManager.speak(context.getString(R.string.battery_level, battery.percentage))
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (battery.isCharging) Icons.Default.Bolt else Icons.Default.BatteryFull,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (battery.percentage <= 15 && !battery.isCharging) Color.Red else if (battery.isCharging) Color.Green else MaterialTheme.colorScheme.onSurface
            )
        }
    }
    
    if (showCalendarDialog) {
        CalendarDialog(onDismiss = { showCalendarDialog = false })
    }
}

@Composable
fun CalendarDialog(onDismiss: () -> Unit) {
    val calendar = Calendar.getInstance()
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    val currentMonthStr = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday...
    val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Adjust so week starts on Monday
    val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
    
    val days = mutableListOf<String>()
    
    // Add empty slots for days before 1st of month
    for (i in 0 until offset) {
        days.add("")
    }
    
    // Add actual days
    for (i in 1..maxDays) {
        days.add(i.toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = currentMonthStr.uppercase(), 
                fontSize = 32.sp, 
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Weekday headers
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    val weekDays = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
                    weekDays.forEach {
                        Text(it, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                ) {
                    items(days) { dayStr ->
                        val isToday = dayStr == currentDay.toString()
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .background(
                                    color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayStr,
                                fontSize = 24.sp,
                                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(60.dp)) {
                Text(stringResource(R.string.cancel).uppercase(), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun SignalIndicator(level: Int) {
    // level is 0..4
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(bottom = 4.dp).semantics { contentDescription = "Sinyal gücü $level" }
    ) {
        // 3 bars logic: level 0/1 -> 1 bar, level 2/3 -> 2 bars, level 4 -> 3 bars
        // Or user specific: 1 kademe: Kırmızı, 2 kademe: Turuncu, 3 kademe: Yeşil
        val color = when {
            level >= 4 -> Color.Green
            level >= 2 -> Color(0xFFFF9800) // Orange
            level >= 1 -> Color.Red
            else -> Color.Gray
        }

        val barCount = when {
            level >= 4 -> 3
            level >= 2 -> 2
            level >= 1 -> 1
            else -> 0
        }

        for (i in 1..3) {
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .height((i * 12).dp) // Rising bars
                    .background(
                        if (i <= barCount) color else Color.LightGray,
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}
