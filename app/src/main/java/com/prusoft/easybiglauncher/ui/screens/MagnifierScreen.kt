package com.prusoft.easybiglauncher.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.prusoft.easybiglauncher.utils.ToolManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagnifierScreen(navController: NavController) {
    val context = LocalContext.current
    var zoom by remember { mutableStateOf(1f) }
    val isFlashOn by ToolManager.isFlashlightOn.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Büyüteç") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Simplified View - Camera setup would go here
            Text(text = "Kamera Görünümü (Yakınlaştırma: ${zoom}x)")
            
            Slider(
                value = zoom,
                onValueChange = { zoom = it },
                valueRange = 1f..5f,
                modifier = Modifier.padding(16.dp)
            )

            Button(
                onClick = { ToolManager.toggleFlashlight(context) },
                modifier = Modifier.size(120.dp)
            ) {
                Icon(
                    if (isFlashOn) Icons.Default.FlashlightOff else Icons.Default.FlashlightOn,
                    contentDescription = "Flaş",
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}
