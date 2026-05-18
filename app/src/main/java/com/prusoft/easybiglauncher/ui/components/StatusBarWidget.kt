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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prusoft.easybiglauncher.utils.rememberBatteryStatus
import com.prusoft.easybiglauncher.utils.rememberCurrentTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.provider.AlarmClock
import android.content.Intent
import androidx.compose.foundation.clickable

@Composable
fun StatusBarWidget() {
    val context = LocalContext.current
    val time by rememberCurrentTime()
    val date = remember { SimpleDateFormat("dd MMMM EEEE", Locale.getDefault()).format(Date()) }
    val battery by rememberBatteryStatus(context)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.clickable {
            try {
                val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } catch (e: Exception) {
                // Silent fail to prevent crash
            }
        }) {
            Text(text = time, style = MaterialTheme.typography.displayMedium, fontSize = 48.sp, modifier = Modifier.semantics { contentDescription = "Saat $time" })
            Text(text = date, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.semantics { contentDescription = "Tarih $date" })
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${battery.percentage}%",
                style = MaterialTheme.typography.headlineMedium,
                color = if (battery.percentage <= 15 && !battery.isCharging) Color.Red else Color.Black,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.semantics { contentDescription = "Pil yüzde ${battery.percentage}" }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (battery.isCharging) Icons.Default.Bolt else Icons.Default.BatteryFull,
                contentDescription = null,
                tint = if (battery.percentage <= 15 && !battery.isCharging) Color.Red else if (battery.isCharging) Color.Green else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
