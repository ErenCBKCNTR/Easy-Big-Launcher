package com.prusoft.easybiglauncher.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun BigButton(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    badgeCount: Int = 0,
    customColor: String? = null,
    customImageUri: String? = null,
    onClick: () -> Unit
) {
    val finalBackgroundColor = if (customColor != null) Color(android.graphics.Color.parseColor(customColor)) else backgroundColor
    
    Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = finalBackgroundColor, contentColor = contentColor)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (customImageUri != null) {
                    AsyncImage(
                        model = customImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(icon, contentDescription = text, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (badgeCount > 0) {
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp),
                containerColor = Color.Red,
                contentColor = Color.White
            ) {
                Text(text = badgeCount.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
