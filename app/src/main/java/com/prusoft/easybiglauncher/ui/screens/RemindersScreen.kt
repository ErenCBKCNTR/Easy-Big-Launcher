package com.prusoft.easybiglauncher.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.prusoft.easybiglauncher.R
import com.prusoft.easybiglauncher.data.Reminder
import com.prusoft.easybiglauncher.utils.AlarmScheduler
import com.prusoft.easybiglauncher.viewmodel.LauncherViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val reminders by viewModel.repository.allReminders.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reminders_title).uppercase(), fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(32.dp))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(32.dp)) },
                text = { Text(stringResource(R.string.add_reminder).uppercase(), fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (reminders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_reminders_yet), fontSize = 24.sp, fontWeight = FontWeight.Medium)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(reminders) { reminder ->
                        ReminderItem(
                            reminder = reminder,
                            onToggle = { isActive ->
                                val updated = reminder.copy(isActive = isActive)
                                scope.launch {
                                    viewModel.repository.updateReminder(updated)
                                    if (isActive) {
                                        AlarmScheduler.scheduleAlarm(context, updated)
                                    } else {
                                        AlarmScheduler.cancelAlarm(context, updated)
                                    }
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    AlarmScheduler.cancelAlarm(context, reminder)
                                    viewModel.repository.deleteReminder(reminder)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, timeInMillis ->
                scope.launch {
                    val reminder = Reminder(title = title, timeInMillis = timeInMillis)
                    val id = viewModel.repository.insertReminder(reminder).toInt()
                    AlarmScheduler.scheduleAlarm(context, reminder.copy(id = id))
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun ReminderItem(reminder: Reminder, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateSdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isActive) MaterialTheme.colorScheme.surfaceVariant else Color.LightGray.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sdf.format(Date(reminder.timeInMillis)),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = if (reminder.isActive) MaterialTheme.colorScheme.primary else Color.Gray
                )
                Text(
                    text = dateSdf.format(Date(reminder.timeInMillis)),
                    fontSize = 18.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = reminder.title.uppercase(),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (reminder.isActive) MaterialTheme.colorScheme.onSurface else Color.Gray
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Switch(
                    checked = reminder.isActive,
                    onCheckedChange = onToggle,
                    modifier = Modifier.scale(1.8f)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Red, modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

private fun Modifier.scale(scale: Float): Modifier = this // Helper if needed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(onDismiss: () -> Unit, onConfirm: (String, Long) -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf("") }
    var minute by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_reminder).uppercase(), fontSize = 28.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.reminder_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = hour,
                        onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 2) hour = it },
                        label = { Text(stringResource(R.string.hour)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = minute,
                        onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 2) minute = it },
                        label = { Text(stringResource(R.string.minute)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val h = hour.toIntOrNull() ?: 0
                    val m = minute.toIntOrNull() ?: 0
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, h)
                        set(Calendar.MINUTE, m)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        if (timeInMillis <= System.currentTimeMillis()) {
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                    }
                    val defaultTitle = context.getString(R.string.medicine_time)
                    onConfirm(title.ifEmpty { defaultTitle }, calendar.timeInMillis)
                },
                modifier = Modifier.height(70.dp).fillMaxWidth()
            ) {
                Text(stringResource(R.string.save).uppercase(), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}
