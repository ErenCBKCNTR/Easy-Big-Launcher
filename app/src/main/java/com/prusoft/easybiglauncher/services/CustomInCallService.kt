package com.prusoft.easybiglauncher.services

import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.InCallService
import androidx.annotation.RequiresApi
import com.prusoft.easybiglauncher.ui.call.CustomCallActivity

@RequiresApi(Build.VERSION_CODES.M)
class CustomInCallService : InCallService() {

    companion object {
        var activeCall: Call? = null
        var instance: CustomInCallService? = null
    }

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call?, state: Int) {
            super.onStateChanged(call, state)
            if (state == Call.STATE_DISCONNECTED) {
                activeCall = null
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onCallAdded(call: Call?) {
        super.onCallAdded(call)
        if (call != null) {
            activeCall = call
            call.registerCallback(callback)
            
            // Extract attributes from call
            val number = call.details?.handle?.schemeSpecificPart ?: ""
            
            // Start custom fullscreen activity
            val intent = Intent(this, CustomCallActivity::class.java).apply {
                putExtra("caller_number", number)
                putExtra("caller_name", "") // Caller name can be empty or resolved from contacts
                putExtra("is_active", call.state == Call.STATE_ACTIVE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
        }
    }

    override fun onCallRemoved(call: Call?) {
        super.onCallRemoved(call)
        if (call != null) {
            call.unregisterCallback(callback)
            if (activeCall == call) {
                activeCall = null
            }
        }
    }
}
