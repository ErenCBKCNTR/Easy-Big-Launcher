package com.prusoft.easybiglauncher.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BigFooterButton(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    isTtsEnabled: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val ttsManager = remember { TTSManager.getInstance(context) }
    val currentOnClick by rememberUpdatedState(onClick)

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .combinedClickable(
                onClick = {
                    if (isTtsEnabled) {
                        ttsManager.speak(text)
                    } else {
                        currentOnClick()
                    }
                },
                onDoubleClick = if (isTtsEnabled) currentOnClick else null
            ),
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp))
            Text(
                text = text, 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
