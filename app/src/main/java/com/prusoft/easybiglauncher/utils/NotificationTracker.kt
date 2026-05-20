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
        try {
            val projection = arrayOf(CallLog.Calls.NUMBER)
            val selection = "${CallLog.Calls.TYPE} = ? AND ${CallLog.Calls.NEW} = 1"
            val selectionArgs = arrayOf(CallLog.Calls.MISSED_TYPE.toString())
            val cursor = context.contentResolver.query(CallLog.Calls.CONTENT_URI, projection, selection, selectionArgs, null)
            _missedCalls.value = cursor?.count ?: 0
            cursor?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun updateUnreadSms(context: Context) {
        try {
            val selection = "${Telephony.Sms.READ} = 0"
            val cursor = context.contentResolver.query(Telephony.Sms.CONTENT_URI, null, selection, null, null)
            _unreadSmsCount.value = cursor?.count ?: 0
            cursor?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetMissedCalls(context: Context) {
        try {
            val values = android.content.ContentValues()
            values.put(CallLog.Calls.NEW, 0)
            context.contentResolver.update(CallLog.Calls.CONTENT_URI, values, "${CallLog.Calls.NEW} = 1 AND ${CallLog.Calls.TYPE} = ?", arrayOf(CallLog.Calls.MISSED_TYPE.toString()))
            _missedCalls.value = 0
        } catch (e: Exception) {
            e.printStackTrace()
            _missedCalls.value = 0
        }
    }
    
    fun resetUnreadSms(context: Context) {
        try {
            val values = android.content.ContentValues()
            values.put(Telephony.Sms.READ, 1)
            context.contentResolver.update(Telephony.Sms.CONTENT_URI, values, "${Telephony.Sms.READ} = 0", null)
            _unreadSmsCount.value = 0
        } catch (e: Exception) {
            e.printStackTrace()
            _unreadSmsCount.value = 0
        }
    }
}
