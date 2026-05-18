package com.prusoft.easybiglauncher.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSummaryScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Bildirimler") }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            // TODO: List notifications
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Text("Örnek Bildirim: Ahmet aradı.", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    }
}
