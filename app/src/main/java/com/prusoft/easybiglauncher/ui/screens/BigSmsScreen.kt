package com.prusoft.easybiglauncher.ui.screens

import android.telephony.SmsManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.prusoft.easybiglauncher.R
import com.prusoft.easybiglauncher.utils.NotificationTracker

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import android.provider.Telephony
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CustomQwertyKeyboard(
    onChar: (String) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val row1 = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
    val row2 = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
    val row3 = listOf("Z", "X", "C", "V", "B", "N", "M")

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            row1.forEach { char ->
                KeyButton(text = char, onClick = { onChar(char) }, modifier = Modifier.weight(1f))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            row2.forEach { char ->
                KeyButton(text = char, onClick = { onChar(char) }, modifier = Modifier.weight(1f))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            row3.forEach { char ->
                KeyButton(text = char, onClick = { onChar(char) }, modifier = Modifier.weight(1f))
            }
            KeyButton(
                text = "⌫",
                onClick = onBackspace,
                modifier = Modifier.weight(1.5f),
                containerColor = Color.DarkGray,
                contentColor = Color.White
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            KeyButton(
                text = "BOŞLUK",
                onClick = onSpace,
                modifier = Modifier.weight(1f),
                containerColor = Color.LightGray
            )
        }
    }
}

@Composable
fun KeyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    contentColor: Color = Color.Black
) {
    Button(
        onClick = onClick,
        modifier = modifier.padding(horizontal = 2.dp).height(60.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BigSmsScreen(navController: NavController, viewModel: com.prusoft.easybiglauncher.viewmodel.LauncherViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = LocalContext.current
    var showReplyDialog by remember { mutableStateOf(false) } // Still used for standard new msg dialog maybe, let's keep name
    var selectedMessage by remember { mutableStateOf<SmsMessage?>(null) }
    var replyText by remember { mutableStateOf("") }

    var showNewMessageDialog by remember { mutableStateOf(false) }
    var newMessageNumber by remember { mutableStateOf("") }
    var newMessageText by remember { mutableStateOf("") }

    var messages by remember { mutableStateOf<List<SmsMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) }

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
            messages = withContext(Dispatchers.IO) {
                fetchSms(context.contentResolver)
            }
            isLoading = false
        } else {
            permissionLauncher.launch(Manifest.permission.READ_SMS)
        }
    }

    val isSmsTtsEnabled by viewModel.securityRepository.isSmsTtsEnabled.collectAsState(initial = false)

    if (selectedMessage != null) {
        if (showReplyDialog) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.reply_message_title), fontSize = 28.sp, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { showReplyDialog = false }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(40.dp))
                            }
                        }
                    )
                }
            ) { padding ->
                Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(stringResource(R.string.reply_message_hint), color = Color.DarkGray) },
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        textStyle = LocalTextStyle.current.copy(fontSize = 32.sp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF0F0F0),
                            unfocusedContainerColor = Color(0xFFF0F0F0),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color.Black,
                            focusedIndicatorColor = Color.Black
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    CustomQwertyKeyboard(
                        onChar = { replyText += it },
                        onBackspace = { if (replyText.isNotEmpty()) replyText = replyText.dropLast(1) },
                        onSpace = { replyText += " " }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { showReplyDialog = false },
                            modifier = Modifier.weight(1f).height(80.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) {
                            Text(stringResource(R.string.reply_cancel_btn), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                if (replyText.isNotEmpty()) {
                                    try {
                                        val smsManager = context.getSystemService(android.telephony.SmsManager::class.java)
                                        smsManager.sendTextMessage(selectedMessage!!.number, null, replyText, null, null)
                                        selectedMessage = null
                                        replyText = ""
                                        showReplyDialog = false
                                    } catch (e: Exception) {
                                        // Handle exception
                                    }
                                }
                            },
                            modifier = Modifier.weight(2f).height(80.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(36.dp), tint = Color.White)
                            Spacer(Modifier.width(16.dp))
                            Text(stringResource(R.string.reply_send_btn), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                }
            }
        } else {
            // SMS Detail Full Screen
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(selectedMessage!!.sender, fontSize = 28.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                        navigationIcon = {
                            IconButton(onClick = { 
                                selectedMessage = null
                                replyText = ""
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(40.dp))
                            }
                        }
                    )
                }
            ) { padding ->
                Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                            Text(text = selectedMessage!!.text, fontSize = 28.sp, lineHeight = 36.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showReplyDialog = true },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("CEVAPLA", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        // List Screen
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.btn_messages), fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(32.dp))
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                Button(
                    onClick = { showNewMessageDialog = true },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.new_message).uppercase(), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (!hasPermission) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.READ_SMS) }) {
                            Text("Erişim İzni Ver (SMS)")
                        }
                    }
                } else if (messages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.no_messages), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(messages) { msg ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedMessage = msg
                                    if (isSmsTtsEnabled) {
                                        val ttsText = "${msg.sender} kişisinden gelen mesajı okuyorum: ${msg.text}"
                                        com.prusoft.easybiglauncher.utils.TTSManager.getInstance(context).speak(ttsText)
                                    }
                                },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(text = msg.sender, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = msg.text, fontSize = 22.sp, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showNewMessageDialog) {
            AlertDialog(
                onDismissRequest = { showNewMessageDialog = false },
                title = { Text(stringResource(R.string.new_message), fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = newMessageNumber,
                            onValueChange = { newMessageNumber = it },
                            label = { Text(stringResource(R.string.enter_number)) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(fontSize = 22.sp)
                        )
                        OutlinedTextField(
                            value = newMessageText,
                            onValueChange = { newMessageText = it },
                            label = { Text(stringResource(R.string.enter_message)) },
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 22.sp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newMessageNumber.isNotEmpty() && newMessageText.isNotEmpty()) {
                                try {
                                    val smsManager = context.getSystemService(SmsManager::class.java)
                                    smsManager.sendTextMessage(newMessageNumber, null, newMessageText, null, null)
                                    showNewMessageDialog = false
                                    newMessageNumber = ""
                                    newMessageText = ""
                                } catch (e: Exception) {
                                    // Handle error
                                }
                            }
                        },
                        modifier = Modifier.height(70.dp).fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.send), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

private fun fetchSms(contentResolver: android.content.ContentResolver): List<SmsMessage> {
    val smsList = mutableListOf<SmsMessage>()
    try {
        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            null,
            null,
            null,
            Telephony.Sms.DATE + " DESC"
        )

        cursor?.use {
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            
            var count = 0
            while (it.moveToNext() && count < 30) {
                val address = if (addressIndex != -1) it.getString(addressIndex) ?: "Bilinmeyen" else "Bilinmeyen"
                val body = if (bodyIndex != -1) it.getString(bodyIndex) ?: "" else ""
                smsList.add(SmsMessage(address, address, body))
                count++
            }
        }
    } catch (e: SecurityException) {
        // Permission not granted or other security issue
    } catch (e: Exception) {
        // Other issues
    }
    return smsList
}

data class SmsMessage(val sender: String, val number: String, val text: String)

