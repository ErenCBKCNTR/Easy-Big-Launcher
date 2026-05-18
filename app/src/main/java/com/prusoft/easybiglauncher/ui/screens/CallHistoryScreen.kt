package com.prusoft.easybiglauncher.ui.screens

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CallLogInfo(
    val number: String,
    val name: String?,
    val type: Int,
    val date: Long,
    val duration: String
)

@Composable
fun CallHistoryScreen() {
    val context = LocalContext.current
    var callLogs by remember { mutableStateOf<List<CallLogInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        callLogs = withContext(Dispatchers.IO) {
            fetchCallLogs(context.contentResolver)
        }
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(callLogs) { log ->
                CallLogRow(log) {
                    val intent = Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:${log.number}")
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun CallLogRow(log: CallLogInfo, onClick: () -> Unit) {
    val backgroundColor = if (log.type == CallLog.Calls.MISSED_TYPE) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
    val icon = when (log.type) {
        CallLog.Calls.INCOMING_TYPE -> Icons.Default.CallReceived
        CallLog.Calls.OUTGOING_TYPE -> Icons.Default.CallMade
        CallLog.Calls.MISSED_TYPE -> Icons.Default.CallMissed
        else -> Icons.Default.CallReceived
    }
    val iconColor = when (log.type) {
        CallLog.Calls.INCOMING_TYPE -> Color(0xFF4CAF50)
        CallLog.Calls.OUTGOING_TYPE -> Color(0xFF2196F3)
        CallLog.Calls.MISSED_TYPE -> Color.Red
        else -> Color.Gray
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = log.name ?: log.number,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${getFormattedDate(log.date)} • ${log.number}",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun getFormattedDate(time: Long): String {
    return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString()
}

private fun fetchCallLogs(contentResolver: ContentResolver): List<CallLogInfo> {
    val logs = mutableListOf<CallLogInfo>()
    val cursor = contentResolver.query(
        CallLog.Calls.CONTENT_URI,
        null,
        null,
        null,
        CallLog.Calls.DATE + " DESC"
    )

    cursor?.use {
        val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
        val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
        val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
        val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
        val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)

        var count = 0
        while (it.moveToNext() && count < 50) {
            logs.add(
                CallLogInfo(
                    number = it.getString(numberIndex),
                    name = it.getString(nameIndex),
                    type = it.getInt(typeIndex),
                    date = it.getLong(dateIndex),
                    duration = it.getString(durationIndex)
                )
            )
            count++
        }
    }
    return logs
}
