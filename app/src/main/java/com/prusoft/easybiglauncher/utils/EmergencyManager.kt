package com.prusoft.easybiglauncher.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

object EmergencyManager {
    @SuppressLint("MissingPermission")
    fun triggerSos(context: Context) {
        val sharedPref = context.getSharedPreferences("sos_prefs", Context.MODE_PRIVATE)
        val sosNumber = sharedPref.getString("sos_number", "")
        val sosMessageBase = sharedPref.getString("sos_message", "Yardıma ihtiyacım var!") ?: "Yardıma ihtiyacım var!"
        val sendLocation = sharedPref.getBoolean("sos_send_location", false)

        if (!sosNumber.isNullOrEmpty()) {
            if (sendLocation && (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    val finalMsg = if (location != null) {
                        "$sosMessageBase\nKonumum: https://maps.google.com/?q=${location.latitude},${location.longitude}"
                    } else {
                        sosMessageBase
                    }
                    executeSos(context, sosNumber, finalMsg)
                }.addOnFailureListener {
                    executeSos(context, sosNumber, sosMessageBase)
                }
            } else {
                executeSos(context, sosNumber, sosMessageBase)
            }
        } else {
            Toast.makeText(context, "Lütfen ayarlardan SOS numarası belirleyin.", Toast.LENGTH_LONG).show()
        }
    }

    private fun executeSos(context: Context, sosNumber: String, message: String) {
        try {
            // Send SMS
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(sosNumber, null, message, null, null)
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
    }
}
