package com.prusoft.easybiglauncher.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.prusoft.easybiglauncher.data.Reminder
import com.prusoft.easybiglauncher.receivers.AlarmReceiver

object AlarmScheduler {

    fun scheduleAlarm(context: Context, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("reminder_title", reminder.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        var canScheduleExact = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            canScheduleExact = alarmManager.canScheduleExactAlarms()
        }

        if (canScheduleExact) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.timeInMillis,
                    pendingIntent
                )
                android.util.Log.d("AlarmScheduler", "Exact alarm scheduled successfully for ID: ${reminder.id}")
                return
            } catch (e: SecurityException) {
                android.util.Log.e("AlarmScheduler", "SecurityException scheduling exact: ${e.message}")
            }
        }

        // Graceful fallback to non-exact but lockscreen/doze resilient alarm
        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.timeInMillis,
                pendingIntent
            )
            android.util.Log.d("AlarmScheduler", "Fallback alarm scheduled successfully for ID: ${reminder.id}")
        } catch (e: Exception) {
            android.util.Log.e("AlarmScheduler", "Error scheduling fallback alarm: ${e.message}")
        }
    }

    fun cancelAlarm(context: Context, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
