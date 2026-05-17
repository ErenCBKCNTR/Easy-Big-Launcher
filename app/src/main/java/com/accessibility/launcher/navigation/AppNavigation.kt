package com.accessibility.launcher.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.accessibility.launcher.ui.screens.HomeScreen
import com.accessibility.launcher.ui.screens.SettingsScreen
import com.accessibility.launcher.ui.screens.AllAppsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable("all_apps") { AllAppsScreen(navController) }
    }
}
