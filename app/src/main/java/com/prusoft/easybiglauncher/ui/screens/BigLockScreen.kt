package com.prusoft.easybiglauncher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BigLockScreen(onUnlock: () -> Unit) {
    val time = remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    val date = remember { mutableStateOf(SimpleDateFormat("dd MMMM EEEE", Locale.getDefault()).format(Date())) }
    var offset by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offset += dragAmount.y
                    },
                    onDragEnd = {
                        if (offset < -200) {
                            onUnlock()
                        }
                        offset = 0f
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = time.value, fontSize = 96.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(text = date.value, fontSize = 32.sp, color = Color.White)
            Spacer(modifier = Modifier.height(100.dp))
            Text(text = "KİLİDİ AÇMAK İÇİN YUKARI KAYDIR", fontSize = 24.sp, color = Color.LightGray)
        }
    }
}
