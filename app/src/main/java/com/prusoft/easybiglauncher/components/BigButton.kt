package com.prusoft.easybiglauncher.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person

import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

import com.prusoft.easybiglauncher.utils.TTSManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun BigButton(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    badgeCount: Int = 0,
    customColor: String? = null,
    customImageUri: String? = null,
    isTtsEnabled: Boolean = false,
    appIconPackageName: String? = null,
    isContact: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val ttsManager = remember { TTSManager.getInstance(context) }
    
    val contactColor = Color(0xFF2196F3) // Blue for contacts
    val finalBackgroundColor = when {
        customColor != null -> Color(android.graphics.Color.parseColor(customColor))
        isContact -> contactColor
        else -> backgroundColor
    }
    
    val finalIcon = if (isContact) Icons.Default.Person else icon
    
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongClick by rememberUpdatedState(onLongClick)
    val currentText by rememberUpdatedState(text)
    
    Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(24.dp),
            color = finalBackgroundColor,
            contentColor = contentColor
        ) {
            Box(
                contentAlignment = Alignment.Center, 
                modifier = Modifier
                    .fillMaxSize()
                    .androidx.compose.foundation.combinedClickable(
                        onClick = {
                            if (isTtsEnabled) {
                                ttsManager.speak(currentText)
                            } else {
                                currentOnClick()
                            }
                        },
                        onDoubleClick = if (isTtsEnabled) {
                            { currentOnClick() }
                        } else null,
                        onLongClick = currentOnLongClick
                    )
            ) {
                if (customImageUri != null) {
                    AsyncImage(
                        model = customImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
                } else if (appIconPackageName != null) {
                    val appIcon = remember(appIconPackageName) {
                        try {
                            context.packageManager.getApplicationIcon(appIconPackageName)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (appIcon != null) {
                        AndroidView(
                            factory = { ctx ->
                                android.widget.ImageView(ctx)
                            },
                            update = { view ->
                                view.setImageDrawable(appIcon)
                            },
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
        
                // Show text and icon if no app icon or if icon is small
                if (appIconPackageName == null || customImageUri != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (customImageUri == null) {
                            Icon(finalIcon, contentDescription = text, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text(
                            text = text, 
                            fontSize = 24.sp, 
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            lineHeight = 28.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    // Just show label for apps below the icon
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = text, 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = contentColor,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(48.dp)
                    .background(Color.Red, shape = androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeCount.toString(), 
                    color = Color.White, 
                    fontSize = 24.sp, 
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
