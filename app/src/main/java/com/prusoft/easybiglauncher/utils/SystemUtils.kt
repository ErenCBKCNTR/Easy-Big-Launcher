package com.prusoft.easybiglauncher.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.telephony.TelephonyManager
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.os.Build

data class BatteryStatus(val percentage: Int, val isCharging: Boolean)

@Composable
fun rememberSignalStrength(context: Context): State<Int> {
    val signalLevel = remember { mutableStateOf(0) }
    val telephonyManager = remember { context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager }

    DisposableEffect(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val callback = object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                    signalLevel.value = signalStrength.level
                }
            }
            telephonyManager.registerTelephonyCallback(context.mainExecutor, callback)
            onDispose { telephonyManager.unregisterTelephonyCallback(callback) }
        } else {
            val listener = object : android.telephony.PhoneStateListener() {
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                    signalLevel.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        signalStrength.level
                    } else {
                        // Approximation for older versions if needed, but level is usually good
                        0 // Default fallback
                    }
                }
            }
            telephonyManager.listen(listener, android.telephony.PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
            onDispose { telephonyManager.listen(listener, android.telephony.PhoneStateListener.LISTEN_NONE) }
        }
    }
    return signalLevel
}

@Composable
fun rememberBatteryStatus(context: Context): State<BatteryStatus> {
    val status = remember {
        mutableStateOf(BatteryStatus(0, false))
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val percentage = (level * 100 / scale.toFloat()).toInt()
                val charging = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING
                status.value = BatteryStatus(percentage, charging)
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { context.unregisterReceiver(receiver) }
    }
    return status
}

@Composable
fun rememberCurrentTime(): State<String> {
    val time = remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    
    LaunchedEffect(Unit) {
        while (true) {
            time.value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(1000) // update every second for simplicity, or 60000 for minutes
        }
    }
    return time
}
