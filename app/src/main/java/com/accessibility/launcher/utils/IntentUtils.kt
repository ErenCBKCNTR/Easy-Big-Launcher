package com.accessibility.launcher.utils

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
}
