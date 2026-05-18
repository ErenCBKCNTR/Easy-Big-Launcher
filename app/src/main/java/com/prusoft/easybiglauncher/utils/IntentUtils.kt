package com.prusoft.easybiglauncher.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast

object IntentUtils {
    
    fun openDialer(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_DIAL)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Telefon uygulaması açılamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    fun openMessages(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_APP_MESSAGING)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Mesajlar uygulaması açılamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    fun openCamera(context: Context) {
        try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Kamera uygulaması açılamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    fun openGallery(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Galeri uygulaması açılamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    fun makeCall(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Arama yapılamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendSms(context: Context, phoneNumber: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber"))
            intent.putExtra("sms_body", message)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Mesaj gönderilemedi.", Toast.LENGTH_SHORT).show()
        }
    }

    fun callWhatsApp(context: Context, phoneNumber: String, video: Boolean) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val url = "https://wa.me/$phoneNumber"
            intent.data = Uri.parse(url)
            intent.setPackage("com.whatsapp")
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp açılamadı.", Toast.LENGTH_SHORT).show()
        }
    }
}
