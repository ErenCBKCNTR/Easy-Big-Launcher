package com.accessibility.launcher.utils

import android.content.Context
import com.accessibility.launcher.data.SecurityRepository
import kotlinx.coroutines.flow.Flow

class LocaleManager(private val context: Context) {
    private val securityRepository = SecurityRepository(context)

    val currentLanguage: Flow<String> = securityRepository.language

    suspend fun setLanguage(lang: String) {
        securityRepository.setLanguage(lang)
    }
}
