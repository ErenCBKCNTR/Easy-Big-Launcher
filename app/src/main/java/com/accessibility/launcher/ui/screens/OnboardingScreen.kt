package com.accessibility.launcher.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.accessibility.launcher.viewmodel.LauncherViewModel
import kotlinx.coroutines.launch
import androidx.core.os.LocaleListCompat
import androidx.appcompat.app.AppCompatDelegate

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    
    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            when(page) {
                0 -> {
                    Text("Hoşgeldiniz", style = MaterialTheme.typography.displayLarge)
                    Button(onClick = { 
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("tr"))
                        scope.launch { viewModel.securityRepository.setLanguage("tr") }
                    }) { Text("Türkçe") }
                    Button(onClick = { 
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                        scope.launch { viewModel.securityRepository.setLanguage("en") }
                    }) { Text("English") }
                }
                1 -> {
                    Text("İzinler", style = MaterialTheme.typography.displaySmall)
                    Button(onClick = { /* Request Perms */ }) { Text("İzinleri Onayla") }
                }
                2 -> {
                    Text("Güvenlik", style = MaterialTheme.typography.displaySmall)
                    // TODO: PinPadDialog or Pin input
                }
                3 -> {
                    Text("Acil Durum", style = MaterialTheme.typography.displaySmall)
                    Button(onClick = {
                        scope.launch {
                            viewModel.securityRepository.setOnboardingCompleted(true)
                            navController.navigate("home") { popUpTo("onboarding") { inclusive = true } }
                        }
                    }) { Text("Başla") }
                }
            }
        }
    }
}
