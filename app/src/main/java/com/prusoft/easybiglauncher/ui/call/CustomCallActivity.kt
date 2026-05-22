package com.prusoft.easybiglauncher.ui.call

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.prusoft.easybiglauncher.services.CustomInCallService
import com.prusoft.easybiglauncher.ui.theme.AccessibilityLauncherTheme

@RequiresApi(Build.VERSION_CODES.M)
class CustomCallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val callerName = intent.getStringExtra("caller_name") ?: ""
        val callerNumber = intent.getStringExtra("caller_number") ?: ""
        val isCallActiveInitially = intent.getBooleanExtra("is_active", false)

        setContent {
            AccessibilityLauncherTheme {
                CustomCallScreen(
                    callerName = callerName,
                    callerNumber = callerNumber,
                    isCallActiveInitially = isCallActiveInitially,
                    onDeclineCall = {
                        declineCall()
                        finish()
                    },
                    onAcceptCall = {
                        acceptCall()
                    },
                    onEndCall = {
                        endCall()
                        finish()
                    }
                )
            }
        }
    }

    private fun declineCall() {
        val activeCall = CustomInCallService.activeCall
        if (activeCall != null && activeCall.state == android.telecom.Call.STATE_RINGING) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                activeCall.reject(android.telecom.Call.REJECT_REASON_DECLINED, null)
            } else {
                activeCall.reject(false, null)
            }
        } else {
            activeCall?.disconnect()
        }
        
        // Fallback using TelecomManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                telecomManager.endCall()
            } catch (e: Exception) {
                // handle permission or system service exception gracefully
            }
        }
    }

    private fun acceptCall() {
        val activeCall = CustomInCallService.activeCall
        if (activeCall != null) {
            activeCall.answer(android.telecom.VideoProfile.STATE_AUDIO_ONLY)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Fallback using TelecomManager
            try {
                val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                telecomManager.acceptRingingCall()
            } catch (e: Exception) {
                // handle permission or system service exception gracefully
            }
        }
    }

    private fun endCall() {
        val activeCall = CustomInCallService.activeCall
        if (activeCall != null) {
            activeCall.disconnect()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Fallback using TelecomManager
            try {
                val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                telecomManager.endCall()
            } catch (e: Exception) {
                // handle permission or system service exception gracefully
            }
        }
    }
}
