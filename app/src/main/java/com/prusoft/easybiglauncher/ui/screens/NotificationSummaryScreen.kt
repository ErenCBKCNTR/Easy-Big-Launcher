package com.prusoft.easybiglauncher.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.prusoft.easybiglauncher.utils.NotificationTracker
import com.prusoft.easybiglauncher.utils.TTSManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.prusoft.easybiglauncher.R
import com.prusoft.easybiglauncher.viewmodel.LauncherViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSummaryScreen(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val context = LocalContext.current
    val missedCalls by NotificationTracker.missedCalls.collectAsState()
    val unreadSms by NotificationTracker.unreadSmsCount.collectAsState()
    val isTtsEnabled by viewModel.securityRepository.isTtsEnabled.collectAsState(initial = false)
    val ttsManager = remember { TTSManager.getInstance(context) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.notification_summary_title)) }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (missedCalls > 0) {
                item {
                    val summaryText = stringResource(R.string.missed_calls_announcement, missedCalls, "Bilinmeyen Numara")
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                if (isTtsEnabled) ttsManager.speak(summaryText)
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = summaryText,
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            if (unreadSms > 0) {
                item {
                    val smsSummaryText = "${unreadSms} yeni mesaj" // Use a localized string if possible
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                if (isTtsEnabled) ttsManager.speak(smsSummaryText)
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = smsSummaryText,
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (missedCalls == 0 && unreadSms == 0) {
                item {
                    Text(
                        text = "Yeni bildirim yok",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
