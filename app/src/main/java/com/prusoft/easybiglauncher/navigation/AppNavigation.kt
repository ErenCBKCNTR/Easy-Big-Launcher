package com.prusoft.easybiglauncher.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.prusoft.easybiglauncher.ui.screens.HomeScreen
import com.prusoft.easybiglauncher.ui.screens.SettingsScreen
import com.prusoft.easybiglauncher.ui.screens.AllAppsScreen
import com.prusoft.easybiglauncher.ui.screens.NotificationSummaryScreen

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prusoft.easybiglauncher.viewmodel.LauncherViewModel
import com.prusoft.easybiglauncher.ui.screens.OnboardingScreen
import com.prusoft.easybiglauncher.ui.screens.SettingsWrapper
import com.prusoft.easybiglauncher.ui.screens.ToolsScreen

@Composable
fun AppNavigation(viewModel: LauncherViewModel = viewModel()) {
    val navController = rememberNavController()
    val isOnboardingCompleted by viewModel.securityRepository.isOnboardingCompleted.collectAsState(initial = false)
    val startDestination = if (isOnboardingCompleted) "home" else "onboarding"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("onboarding") { OnboardingScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("settings") { SettingsWrapper(navController) }
        composable("all_apps") { AllAppsScreen(navController) }
        composable("notification_summary") { NotificationSummaryScreen(navController) }
        composable("tools") { ToolsScreen(navController) }
    }
}
