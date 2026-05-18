package com.prusoft.easybiglauncher.ui.screens

import android.media.AudioManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BigDialerScreen(navController: NavController) {
    var number by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.btn_phone), fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(32.dp))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Number Display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = number,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
            }

            // Dial Pad
            val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#")
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(keys) { key ->
                    DialerButton(key) {
                        if (number.length < 15) number += key
                    }
                }
            }

            // Bottom Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete Button
                Button(
                    onClick = { if (number.isNotEmpty()) number = number.dropLast(1) },
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Backspace, contentDescription = null, modifier = Modifier.size(40.dp))
                }

                // Call Button
                Button(
                    onClick = {
                        if (number.isNotEmpty()) {
                            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                            audioManager.mode = AudioManager.MODE_IN_CALL
                            audioManager.isSpeakerphoneOn = true

                            val intent = Intent(Intent.ACTION_CALL).apply {
                                data = Uri.parse("tel:$number")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback to DIAL if CALL is not permitted
                                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$number")
                                }
                                context.startActivity(dialIntent)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(90.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.call), fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DialerButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(100.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Text(text = text, fontSize = 36.sp, fontWeight = FontWeight.Bold)
    }
}
