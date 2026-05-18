package com.prusoft.easybiglauncher.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prusoft.easybiglauncher.utils.TTSManager

@Composable
fun BigFooterButton(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    isTtsEnabled: Boolean = false,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val ttsManager = remember { TTSManager.getInstance(context) }

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .pointerInput(isTtsEnabled) {
                detectTapGestures(
                    onTap = {
                        if (isTtsEnabled) {
                            ttsManager.speak(text)
                        } else {
                            onClick()
                        }
                    },
                    onDoubleTap = {
                        if (isTtsEnabled) {
                            onClick()
                        }
                    }
                )
            },
        color = containerColor,
        contentColor = contentColor,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp))
            Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
