package com.accessibility.launcher.ui.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.accessibility.launcher.R
import com.accessibility.launcher.viewmodel.LauncherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val context = LocalContext.current
    
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
            
            Button(onClick = { 
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
            }) {
                Text("English")
            }
            
            Button(onClick = { 
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("tr"))
            }) {
                Text("Türkçe")
            }
            
            Divider()
            
            Text(text = "Sayfa Ekle", style = MaterialTheme.typography.titleLarge)
            Button(onClick = { 
                viewModel.addPage(3, 2)
            }) {
                Text("Yeni Sayfa Ekle (3x2)")
            }
        }
    }
}
