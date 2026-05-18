package com.prusoft.easybiglauncher.ui.screens

import android.telephony.SmsManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

import android.provider.Telephony
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BigSmsScreen(navController: NavController) {
    val context = LocalContext.current
    var showReplyDialog by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf("") }
    var selectedNumber by remember { mutableStateOf("") }
    var replyText by remember { mutableStateOf("") }

    var showNewMessageDialog by remember { mutableStateOf(false) }
    var newMessageNumber by remember { mutableStateOf("") }
    var newMessageText by remember { mutableStateOf("") }

    var messages by remember { mutableStateOf<List<SmsMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        messages = withContext(Dispatchers.IO) {
            fetchSms(context.contentResolver)
        }
        isLoading = false
    }

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
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(messages) { msg ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedContact = msg.sender
                                selectedNumber = msg.number
                                showReplyDialog = true
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(text = msg.sender, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = msg.text, fontSize = 22.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReplyDialog) {
        AlertDialog(
            onDismissRequest = { showReplyDialog = false },
            title = { Text("$selectedContact", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text(stringResource(R.string.enter_message)) },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 22.sp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (replyText.isNotEmpty()) {
                            try {
                                val smsManager = context.getSystemService(SmsManager::class.java)
                                smsManager.sendTextMessage(selectedNumber, null, replyText, null, null)
                                showReplyDialog = false
                                replyText = ""
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

private fun fetchSms(contentResolver: android.content.ContentResolver): List<SmsMessage> {
    val smsList = mutableListOf<SmsMessage>()
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
        val personIndex = it.getColumnIndex(Telephony.Sms.PERSON)

        var count = 0
        while (it.moveToNext() && count < 30) {
            val address = it.getString(addressIndex) ?: "Bilinmeyen"
            val body = it.getString(bodyIndex) ?: ""
            smsList.add(SmsMessage(address, address, body))
            count++
        }
    }
    return smsList
}

data class SmsMessage(val sender: String, val number: String, val text: String)

