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
import com.prusoft.easybiglauncher.utils.rememberBatteryStatus
import com.prusoft.easybiglauncher.utils.rememberCurrentTime
import com.prusoft.easybiglauncher.utils.rememberSignalStrength
import android.content.Intent
import android.provider.AlarmClock
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import com.prusoft.easybiglauncher.utils.TTSManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatusBarWidget(isTtsEnabled: Boolean = false) {
    val context = LocalContext.current
    val ttsManager = remember { TTSManager.getInstance(context) }
    val time by rememberCurrentTime()
    val date = remember { SimpleDateFormat("dd MMMM EEEE", Locale.getDefault()).format(Date()) }
    val battery by rememberBatteryStatus(context)
    val signalLevel by rememberSignalStrength(context)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 24.dp), // Increased padding for better separation
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.clickable {
            if (isTtsEnabled) {
                ttsManager.speak("Bugün $date, saat şu an $time")
            } else {
                try {
                    val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Silent fail to prevent crash
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
                modifier = Modifier.semantics { contentDescription = "Pil yüzde ${battery.percentage}" }
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
