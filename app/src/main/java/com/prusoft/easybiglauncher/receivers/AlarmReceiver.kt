package com.prusoft.easybiglauncher.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.prusoft.easybiglauncher.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("reminder_id", -1)
        val reminderTitle = intent.getStringExtra("reminder_title") ?: context.getString(com.prusoft.easybiglauncher.R.string.reminder_item_title)

        Log.d("AlarmReceiver", "Alarm received for: $reminderTitle ($reminderId)")

        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("reminder_title", reminderTitle)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        // Try to launch AlarmActivity directly so it appears immediately if screen is unlocked or app is in foreground
        try {
            context.startActivity(alarmIntent)
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to start AlarmActivity directly: ${e.message}")
        }
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("alarm_channel", "Alarms", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Easy Big Launcher Alarm Notifications"
                enableLights(true)
                enableVibration(true)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            reminderId,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(com.prusoft.easybiglauncher.R.string.reminder_item_title))
            .setContentText(reminderTitle)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
            
        notificationManager.notify(reminderId, notification)
    }
}
