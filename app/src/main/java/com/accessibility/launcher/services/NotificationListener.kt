package com.accessibility.launcher.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Handle notification posted
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Handle notification removed
    }
}
