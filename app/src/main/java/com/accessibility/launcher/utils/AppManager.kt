package com.accessibility.launcher.utils

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable

data class AppInfo(
    val name: CharSequence,
    val packageName: String,
    val icon: Drawable
)

object AppManager {
    fun getInstalledApps(context: Context): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val packageManager = context.packageManager
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
        
        return resolveInfos.map {
            AppInfo(
                it.loadLabel(packageManager),
                it.activityInfo.packageName,
                it.loadIcon(packageManager)
            )
        }
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
