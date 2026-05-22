package com.prusoft.easybiglauncher.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class AppInfo(
    val name: CharSequence,
    val packageName: String,
    val icon: Drawable
)

object AppManager {
    fun getInstalledApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        return apps.mapNotNull { appInfo ->
            val name = appInfo.loadLabel(pm).toString()
            val packageName = appInfo.packageName
            val icon = appInfo.loadIcon(pm)
            
            // Block system settings and app stores
            val lowerPkg = packageName.lowercase()
            val lowerName = name.lowercase()
            val isRestricted = lowerPkg.contains("settings") ||
                               lowerPkg.contains("vending") || // google play
                               lowerPkg.contains("store") || // other stores
                               lowerPkg.contains("packageinstaller") || // package installer
                               lowerName.contains("ayarlar") ||
                               lowerName.contains("settings") ||
                               lowerName.contains("store") ||
                               lowerName.contains("mağaza")
                               
            if (isRestricted) return@mapNotNull null
            
            // Check if it has a launcher intent
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            
            // Ensure it's not a service or provider masquerading as an app
            if (launchIntent != null && packageName != context.packageName) {
                AppInfo(name, packageName, icon)
            } else {
                null
            }
        }.distinctBy { it.packageName }.sortedBy { it.name.toString().lowercase() }
    }

    fun launchApp(context: Context, packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Hata durumu
        }
    }
}
