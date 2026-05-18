package com.prusoft.easybiglauncher

import androidx.appcompat.app.AppCompatActivity
import androidx.activity.ComponentActivity
import android.os.Bundle
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
import com.prusoft.easybiglauncher.utils.BatteryMonitor
import android.os.BatteryManager
import com.prusoft.easybiglauncher.ui.theme.AccessibilityLauncherTheme
import com.prusoft.easybiglauncher.navigation.AppNavigation
import com.prusoft.easybiglauncher.utils.TTSManager
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
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
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
