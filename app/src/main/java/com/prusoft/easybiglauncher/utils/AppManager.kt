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
            val name = appInfo.loadLabel(pm)
            val packageName = appInfo.packageName
            val icon = appInfo.loadIcon(pm)
            
            // Check if it has a launcher intent
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            
            // Ensure it's not a service or provider masquerading as an app (though getLaunchIntentForPackage usually covers this)
            if (launchIntent != null) {
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
