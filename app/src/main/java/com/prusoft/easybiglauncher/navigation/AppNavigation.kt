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
import com.prusoft.easybiglauncher.ui.screens.PrivacyPolicyScreen
import com.prusoft.easybiglauncher.ui.screens.ManagePagesScreen
import com.prusoft.easybiglauncher.ui.screens.BigDialerScreen
import com.prusoft.easybiglauncher.ui.screens.BigSmsScreen
import com.prusoft.easybiglauncher.ui.screens.RemindersScreen
import com.prusoft.easybiglauncher.ui.screens.MagnifierScreen
import com.prusoft.easybiglauncher.ui.screens.PanicSirenScreen

@Composable
fun AppNavigation(viewModel: LauncherViewModel = viewModel()) {
    val navController = rememberNavController()
    val isOnboardingCompleted by viewModel.securityRepository.isOnboardingCompleted.collectAsState(initial = false)
    val startDestination = if (isOnboardingCompleted) "home" else "onboarding"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("onboarding") { OnboardingScreen(navController) }
        composable("home") { HomeScreen(navController, viewModel) }
        composable("settings") { SettingsWrapper(navController, viewModel) }
        composable("all_apps") { AllAppsScreen(navController, viewModel) }
        composable("notification_summary") { NotificationSummaryScreen(navController, viewModel) }
        composable("tools") { ToolsScreen(navController, viewModel) }
        composable("privacy_policy") { PrivacyPolicyScreen(navController) }
        composable("manage_pages") { ManagePagesScreen(navController, viewModel) }
        composable("dialer") { BigDialerScreen(navController) }
        composable("sms") { BigSmsScreen(navController) }
        composable("reminders") { RemindersScreen(navController, viewModel) }
        composable("magnifier") { MagnifierScreen(navController) }
        composable("siren") { PanicSirenScreen(navController) }
    }
}
