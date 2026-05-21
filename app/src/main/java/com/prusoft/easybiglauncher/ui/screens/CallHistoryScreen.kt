package com.prusoft.easybiglauncher.ui.screens

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import com.prusoft.easybiglauncher.R
import com.prusoft.easybiglauncher.utils.FavoritesUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import androidx.compose.ui.res.stringResource

data class CallLogInfo(
    val number: String,
    val name: String?,
    val type: Int,
    val date: Long,
    val duration: String
)

@Composable
fun CallHistoryScreen(viewModel: com.prusoft.easybiglauncher.viewmodel.LauncherViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = LocalContext.current
    var callLogs by remember { mutableStateOf<List<CallLogInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) }

    val isHomeFavLockEnabled by viewModel.securityRepository.isHomeFavLockEnabled.collectAsState(initial = false)
    var favoriteContactToAdd by remember { mutableStateOf<CallLogInfo?>(null) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    val writePermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                context.contentResolver.delete(CallLog.Calls.CONTENT_URI, null, null)
                callLogs = emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            isLoading = true
        } else {
            isLoading = false
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            callLogs = withContext(Dispatchers.IO) {
                fetchCallLogs(context.contentResolver, context)
            }
            isLoading = false
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CALL_LOG)
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (!hasPermission) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = { permissionLauncher.launch(Manifest.permission.READ_CALL_LOG) }) {
                Text(stringResource(R.string.grant_permission_call_log))
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            if (callLogs.isNotEmpty()) {
                Button(
                    onClick = { showClearHistoryDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(70.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
                ) {
                    Text(stringResource(R.string.clear_history), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(callLogs) { log ->
                        CallLogRow(
                            log = log, 
                            onClick = {
                                val intent = Intent(Intent.ACTION_CALL).apply {
                                    data = Uri.parse("tel:${log.number}")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${log.number}")
                                    }
                                    context.startActivity(dialIntent)
                                }
                            }, 
                            onLongClick = {
                                if (!isHomeFavLockEnabled) {
                                    favoriteContactToAdd = log
                                }
                            }
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.history_empty), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }
        }
    }

    if (favoriteContactToAdd != null) {
        com.prusoft.easybiglauncher.ui.components.BigConfirmDialog(
            title = stringResource(R.string.add_to_favorites),
            message = stringResource(R.string.add_to_favorites_desc, favoriteContactToAdd!!.name ?: favoriteContactToAdd!!.number),
            onConfirm = {
                favoriteContactToAdd?.let {
                    FavoritesUtils.addFavorite(context, it.name ?: it.number, it.number)
                }
                favoriteContactToAdd = null
            },
            onCancel = { favoriteContactToAdd = null }
        )
    }

    if (showClearHistoryDialog) {
        com.prusoft.easybiglauncher.ui.components.BigConfirmDialog(
            title = stringResource(R.string.clear_history_title),
            message = stringResource(R.string.clear_history_confirm),
            onConfirm = {
                try {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                        context.contentResolver.delete(CallLog.Calls.CONTENT_URI, null, null)
                        callLogs = emptyList()
                    } else {
                        writePermissionLauncher.launch(Manifest.permission.WRITE_CALL_LOG)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                showClearHistoryDialog = false
            },
            onCancel = { showClearHistoryDialog = false }
        )
    }
}

@Composable
fun CallLogRow(log: CallLogInfo, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
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
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongClick() },
                    onTap = { onClick() }
                )
            },
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

private fun fetchCallLogs(contentResolver: ContentResolver, context: android.content.Context): List<CallLogInfo> {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
        return emptyList()
    }
    val logs = mutableListOf<CallLogInfo>()
    try {
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
                        number = it.getString(numberIndex) ?: "Unknown",
                        name = it.getString(nameIndex),
                        type = it.getInt(typeIndex),
                        date = it.getLong(dateIndex),
                        duration = it.getString(durationIndex) ?: "0"
                    )
                )
                count++
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return logs
}
