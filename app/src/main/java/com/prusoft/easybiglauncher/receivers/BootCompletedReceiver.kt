package com.prusoft.easybiglauncher.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            Log.d("BootCompletedReceiver", "Device reboot detected, initializing services...")
            // The system will automatically restart the launcher if it's set as default
            // Here we can trigger any background sync or service initialization if needed in the future.
            // For now, the launcher activity will handle the main initialization when it boots.
        }
    }
}
