package com.prusoft.easybiglauncher

import androidx.appcompat.app.AppCompatActivity
import androidx.activity.ComponentActivity
import android.os.Bundle
import android.content.Context
import android.content.Intent
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.prusoft.easybiglauncher.utils.BatteryMonitor
import android.os.BatteryManager
import com.prusoft.easybiglauncher.ui.theme.AccessibilityLauncherTheme
import com.prusoft.easybiglauncher.navigation.AppNavigation
import com.prusoft.easybiglauncher.utils.TTSManager
import com.prusoft.easybiglauncher.utils.BatteryWorker
import android.content.ComponentCallbacks2
import coil.imageLoader

class MainActivity : AppCompatActivity() {
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            BatteryMonitor.checkAndAlert(context)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("tr"))
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        
        val workRequest = PeriodicWorkRequestBuilder<BatteryWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("BatteryWorker", ExistingPeriodicWorkPolicy.KEEP, workRequest)
        
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContent {
            AccessibilityLauncherTheme {
                AppNavigation()
            }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
            // Aggressively clear Coil memory cache on critical low memory
            imageLoader.memoryCache?.clear()
        }
    }

    override fun onBackPressed() {
        // Launcher should not close
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        TTSManager.getInstance(this).shutdown()
    }
}
