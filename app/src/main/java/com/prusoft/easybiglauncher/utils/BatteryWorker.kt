package com.prusoft.easybiglauncher.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.prusoft.easybiglauncher.R
import kotlinx.coroutines.delay

class BatteryWorker(val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = level * 100 / scale.toFloat()
        
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        if (batteryPct <= 15 && !isCharging) {
            val ttsManager = TTSManager.getInstance(context)
            // Wait for engine to initialize if not already
            var tries = 0
            while (!ttsManager.isInitialized && tries < 10) {
                delay(200)
                tries++
            }
            if (ttsManager.isInitialized) {
                ttsManager.speak(context.getString(R.string.battery_low_warning))
                // Allow TTS to speak before worker finishes
                delay(5000)
            }
        }

        return Result.success()
    }
}
