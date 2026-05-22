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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.MoreVert
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
fun CallHistoryScreen(viewModel: com.prusoft.easybiglauncher.viewmodel.LauncherViewModel = androidx.lifecycle.viewmodel.compose.viewModel(), navController: androidx.navigation.NavController? = null) {
    val context = LocalContext.current
    var callLogs by remember { mutableStateOf<List<CallLogInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) }

    val isHomeFavLockEnabled by viewModel.securityRepository.isHomeFavLockEnabled.collectAsState(initial = false)
    var favoriteContactToAdd by remember { mutableStateOf<CallLogInfo?>(null) }
    var selectedLogForOptions by remember { mutableStateOf<CallLogInfo?>(null) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    
    val sharedPref = remember { context.getSharedPreferences("blocked_prefs", android.content.Context.MODE_PRIVATE) }
    
    var showBlockedList by remember { mutableStateOf(false) }

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
            if (showBlockedList) {
                BlockedNumbersList(
                    sharedPref = sharedPref,
                    onBack = { showBlockedList = false }
                )
            } else if (callLogs.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showClearHistoryDialog = true },
                        modifier = Modifier.weight(1f).height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
                    ) {
                        Text(stringResource(R.string.clear_history), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(32.dp))
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.blocked_list), fontSize = 20.sp) },
                                onClick = {
                                    menuExpanded = false
                                    showBlockedList = true
                                }
                            )
                        }
                    }
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
                                selectedLogForOptions = log
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

    if (selectedLogForOptions != null) {
        val log = selectedLogForOptions!!
        AlertDialog(
            onDismissRequest = { selectedLogForOptions = null },
            title = { Text(log.name ?: log.number, fontSize = 24.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_CALL).apply { data = Uri.parse("tel:${log.number}") }
                            try { context.startActivity(intent) } catch(e: Exception) {
                                val dialIntent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:${log.number}") }
                                context.startActivity(dialIntent)
                            }
                            selectedLogForOptions = null
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) { Text(stringResource(R.string.call), fontSize = 20.sp) }
                    
                    Button(
                        onClick = {
                            if (navController != null) {
                                navController.navigate("sms?number=${Uri.encode(log.number)}")
                            } else {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("sms:${log.number}")
                                }
                                context.startActivity(intent)
                            }
                            selectedLogForOptions = null
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp)
                    ) { Text(stringResource(R.string.send_message), fontSize = 20.sp) }
                    
                    Button(
                        onClick = {
                            if (!isHomeFavLockEnabled) {
                                favoriteContactToAdd = log
                            }
                            selectedLogForOptions = null
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp)
                    ) { Text(stringResource(R.string.add_to_favorites), fontSize = 20.sp) }
                    
                    if (log.name == null) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_INSERT).apply {
                                    type = android.provider.ContactsContract.RawContacts.CONTENT_TYPE
                                    putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, log.number)
                                }
                                context.startActivity(intent)
                                selectedLogForOptions = null
                            },
                            modifier = Modifier.fillMaxWidth().height(60.dp)
                        ) { Text(stringResource(R.string.add_to_contacts), fontSize = 20.sp) }
                    }
                    
                    Button(
                        onClick = {
                            val editor = sharedPref.edit()
                            val set = sharedPref.getStringSet("blocked_numbers", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
                            set.add(log.number)
                            editor.putStringSet("blocked_numbers", set)
                            editor.apply()
                            selectedLogForOptions = null
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text(stringResource(R.string.block_number), fontSize = 20.sp) }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedLogForOptions = null }) { Text(stringResource(R.string.cancel), fontSize = 20.sp) }
            }
        )
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
            .clickable { onClick() },
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

fun getContactNameByNumber(contentResolver: ContentResolver, phoneNumber: String): String? {
    val uri = Uri.withAppendedPath(
        android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
        Uri.encode(phoneNumber)
    )
    val projection = arrayOf(android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME)
    try {
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME)
                if (nameIndex != -1) {
                    return cursor.getString(nameIndex)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

@Composable
fun BlockedNumbersList(
    sharedPref: android.content.SharedPreferences,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val blockedNumbers = sharedPref.getStringSet("blocked_numbers", emptySet())?.toList() ?: emptyList()
    var numberToUnblock by remember { mutableStateOf<String?>(null) }
    
    // Cache mapped names asynchronously
    var blockedNames by remember { mutableStateOf<Map<String, String?>>(emptyMap()) }
    
    LaunchedEffect(blockedNumbers) {
        val names = withContext(Dispatchers.IO) {
            blockedNumbers.associateWith { number ->
                getContactNameByNumber(context.contentResolver, number)
            }
        }
        blockedNames = names
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(stringResource(R.string.blocked_list), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        
        if (blockedNumbers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.history_empty), fontSize = 24.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(blockedNumbers) { number ->
                    val contactName = blockedNames[number]
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { numberToUnblock = number },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = contactName ?: number,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (contactName != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = number,
                                    fontSize = 18.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (numberToUnblock != null) {
        AlertDialog(
            onDismissRequest = { numberToUnblock = null },
            title = { Text(stringResource(R.string.unblock_number), fontSize = 24.sp, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.unblock_number_desc, numberToUnblock!!), fontSize = 20.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        val editor = sharedPref.edit()
                        val set = sharedPref.getStringSet("blocked_numbers", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
                        set.remove(numberToUnblock)
                        editor.putStringSet("blocked_numbers", set)
                        editor.apply()
                        numberToUnblock = null
                    },
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(androidx.compose.material.icons.Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
            },
            dismissButton = {
                Button(
                    onClick = { numberToUnblock = null },
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }
        )
    }
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
