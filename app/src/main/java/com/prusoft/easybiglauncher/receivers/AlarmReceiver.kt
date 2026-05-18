package com.prusoft.easybiglauncher.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.prusoft.easybiglauncher.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("reminder_id", -1)
        val reminderTitle = intent.getStringExtra("reminder_title") ?: "Hatırlatıcı"

        Log.d("AlarmReceiver", "Alarm received for: $reminderTitle ($reminderId)")

        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("reminder_title", reminderTitle)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(alarmIntent)
    }
}
