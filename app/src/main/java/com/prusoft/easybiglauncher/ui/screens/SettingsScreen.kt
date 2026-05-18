package com.prusoft.easybiglauncher.ui.screens

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.prusoft.easybiglauncher.R
import com.prusoft.easybiglauncher.ui.components.PinPadDialog
import com.prusoft.easybiglauncher.utils.LauncherUtils
import com.prusoft.easybiglauncher.viewmodel.LauncherViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsWrapper(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val isProtectionEnabled by viewModel.securityRepository.isProtectionEnabled.collectAsState(initial = false)
    val savedPin by viewModel.securityRepository.savedPin.collectAsState(initial = null)
    
    var isAuthenticated by remember { mutableStateOf(!isProtectionEnabled) }
    var showPinDialog by remember { mutableStateOf(false) }

    if (isAuthenticated) {
        SettingsScreen(navController = navController, viewModel = viewModel)
    } else if (showPinDialog) {
        PinPadDialog(
            onPinDismiss = { navController.popBackStack() },
            onPinEntered = { pin ->
                if (pin == savedPin) {
                    isAuthenticated = true
                    showPinDialog = false
                }
            }
        )
    } else {
        LaunchedEffect(Unit) { showPinDialog = true }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sharedPref = remember { context.getSharedPreferences("sos_prefs", Context.MODE_PRIVATE) }
    var sosMessage by remember { mutableStateOf(sharedPref.getString("sos_message", "Yardıma ihtiyacım var!") ?: "") }
    val isProtectionEnabled by viewModel.securityRepository.isProtectionEnabled.collectAsState(initial = false)
    val isTtsEnabled by viewModel.securityRepository.isTtsEnabled.collectAsState(initial = false)
    val isDefault = remember { LauncherUtils.isDefaultLauncher(context) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.go_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = stringResource(id = R.string.language_option), style = MaterialTheme.typography.titleLarge)
            
            Button(onClick = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en")) }) { Text("English") }
            Button(onClick = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("tr")) }) { Text("Türkçe") }
            
            Divider()

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(stringResource(R.string.protection_mode), modifier = Modifier.weight(1f))
                Switch(checked = isProtectionEnabled, onCheckedChange = { 
                    scope.launch { viewModel.securityRepository.setProtectionEnabled(it) }
                })
            }

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(stringResource(R.string.voice_feedback), modifier = Modifier.weight(1f))
                Switch(checked = isTtsEnabled, onCheckedChange = { 
                    scope.launch { viewModel.securityRepository.setTtsEnabled(it) }
                })
            }

            Button(
                onClick = { navController.navigate("manage_pages") },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Text(stringResource(R.string.manage_pages), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            Button(onClick = { LauncherUtils.requestSetDefaultLauncher(context) }) {
                Text(if (isDefault) "${stringResource(R.string.set_default)} (${stringResource(R.string.active_status)})" else stringResource(R.string.set_default))
            }
            
            Divider()
            
            Text(text = stringResource(R.string.add_page), style = MaterialTheme.typography.titleLarge)
            Button(onClick = { viewModel.addPage(3, 2) }) { Text(stringResource(R.string.add_page_3x2)) }

            Divider()
            Text(text = stringResource(R.string.sos_settings), style = MaterialTheme.typography.titleLarge)
            TextField(
                value = sosMessage,
                onValueChange = { sosMessage = it; sharedPref.edit().putString("sos_message", it).apply() },
                label = { Text(stringResource(R.string.sos_message_label)) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate("privacy_policy") },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.privacy_policy_title), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
