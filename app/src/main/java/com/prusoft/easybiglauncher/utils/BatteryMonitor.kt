package com.prusoft.easybiglauncher.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.telephony.SmsManager
import androidx.compose.runtime.*
import com.prusoft.easybiglauncher.utils.EmergencyManager

object BatteryMonitor {
    fun checkAndAlert(context: Context) {
        val sharedPref = context.getSharedPreferences("sos_prefs", Context.MODE_PRIVATE)
        val isEnabled = sharedPref.getBoolean("low_battery_sos_enabled", false)
        
        if (!isEnabled) return

        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = level * 100 / scale
        
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        
        if (batteryPct <= 15 && !isCharging) {
            val sosNumber = sharedPref.getString("sos_number", "")
            if (!sosNumber.isNullOrEmpty()) {
                try {
                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(sosNumber, null, "Uyarı: Telefonumun şarjı düşük seviyeye indi ve kapanmak üzere. Bana bir süre ulaşamayabilirsiniz.", null, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
