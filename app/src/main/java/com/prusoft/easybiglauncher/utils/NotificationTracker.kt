package com.prusoft.easybiglauncher.utils

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.provider.CallLog
import android.provider.Telephony
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object NotificationTracker {
    private val _missedCalls = MutableStateFlow(0)
    val missedCalls: StateFlow<Int> = _missedCalls
    
    private val _unreadSmsCount = MutableStateFlow(0)
    val unreadSmsCount: StateFlow<Int> = _unreadSmsCount

    fun startTracking(context: Context) {
        // Observe Call Log
        context.contentResolver.registerContentObserver(
            CallLog.Calls.CONTENT_URI, true,
            object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    updateMissedCalls(context)
                }
            }
        )
        updateMissedCalls(context)
        
        // Observe SMS
        context.contentResolver.registerContentObserver(
            Telephony.Sms.CONTENT_URI, true,
            object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    updateUnreadSms(context)
                }
            }
        )
        updateUnreadSms(context)
    }

    private fun updateMissedCalls(context: Context) {
        val projection = arrayOf(CallLog.Calls.NUMBER)
        val selection = "${CallLog.Calls.TYPE} = ?"
        val selectionArgs = arrayOf(CallLog.Calls.MISSED_TYPE.toString())
        val cursor = context.contentResolver.query(CallLog.Calls.CONTENT_URI, projection, selection, selectionArgs, null)
        _missedCalls.value = cursor?.count ?: 0
        cursor?.close()
    }
    
    private fun updateUnreadSms(context: Context) {
        val selection = "${Telephony.Sms.READ} = 0"
        val cursor = context.contentResolver.query(Telephony.Sms.CONTENT_URI, null, selection, null, null)
        _unreadSmsCount.value = cursor?.count ?: 0
        cursor?.close()
    }
}
