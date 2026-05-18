package com.prusoft.easybiglauncher.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.widget.Toast

object EmergencyManager {
    fun triggerSos(context: Context) {
        val sharedPref = context.getSharedPreferences("sos_prefs", Context.MODE_PRIVATE)
        val sosNumber = sharedPref.getString("sos_number", "")
        val sosMessage = sharedPref.getString("sos_message", "Yardıma ihtiyacım var!") ?: "Yardıma ihtiyacım var!"

        if (!sosNumber.isNullOrEmpty()) {
            try {
                // Send SMS
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(sosNumber, null, sosMessage, null, null)
                Toast.makeText(context, "SOS Mesajı Gönderildi", Toast.LENGTH_SHORT).show()

                // Call
                val callIntent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:$sosNumber")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(callIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "SOS Hatası: ${e.message}", Toast.LENGTH_LONG).show()
                
                // Fallback to dialer if CALL_PHONE permission is missing or fails
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$sosNumber")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            }
        } else {
            Toast.makeText(context, "Lütfen ayarlardan SOS numarası belirleyin.", Toast.LENGTH_LONG).show()
        }
    }
}
