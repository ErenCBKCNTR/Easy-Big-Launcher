package com.prusoft.easybiglauncher.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TTSManager private constructor(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    var isInitialized = false
        private set

    companion object {
        @Volatile
        private var instance: TTSManager? = null

        fun getInstance(context: Context): TTSManager {
            return instance ?: synchronized(this) {
                instance ?: TTSManager(context).also { instance = it }
            }
        }
    }

    private val sharedPrefs = context.getSharedPreferences("sos_prefs", Context.MODE_PRIVATE)

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setGender(sharedPrefs.getBoolean("is_female", true))
        } else {
            Log.e("TTSManager", "Initialization failed")
        }
    }

    fun setLanguage(locale: Locale) {
        if (isInitialized) {
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTSManager", "Language not supported or missing data")
            }
        }
    }

    fun setGender(isFemale: Boolean) {
        if (!isInitialized) return
        
        val voices = tts?.voices
        val selectedVoice = voices?.find { 
            if (isFemale) it.name.contains("female", ignoreCase = true) 
            else it.name.contains("male", ignoreCase = true) 
        }
        
        if (selectedVoice != null) {
            tts?.setVoice(selectedVoice)
        } else {
            tts?.setPitch(if (isFemale) 1.2f else 0.8f)
            tts?.setSpeechRate(1.0f)
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        instance = null
    }
}
