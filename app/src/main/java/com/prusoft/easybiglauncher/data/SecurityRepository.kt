package com.prusoft.easybiglauncher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "security_prefs")

class SecurityRepository(private val context: Context) {
    private val PIN_KEY = stringPreferencesKey("pin")
    private val PROTECTION_ENABLED_KEY = booleanPreferencesKey("protection_enabled")
    private val LANGUAGE_KEY = stringPreferencesKey("language")
    private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
    private val TTS_ENABLED_KEY = booleanPreferencesKey("tts_enabled")

    private val MED_NAME_KEY = stringPreferencesKey("med_name")
    private val MED_SURNAME_KEY = stringPreferencesKey("med_surname")
    private val MED_AGE_KEY = stringPreferencesKey("med_age")
    private val MED_ADDRESS_KEY = stringPreferencesKey("med_address")
    private val MED_BLOOD_KEY = stringPreferencesKey("med_blood")
    private val MED_CHRONIC_KEY = stringPreferencesKey("med_chronic")

    val isProtectionEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PROTECTION_ENABLED_KEY] ?: false
    }

    val savedPin: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PIN_KEY]
    }

    val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "tr"
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED_KEY] ?: false
    }

    val isTtsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TTS_ENABLED_KEY] ?: false
    }

    val medName: Flow<String> = context.dataStore.data.map { it[MED_NAME_KEY] ?: "" }
    val medSurname: Flow<String> = context.dataStore.data.map { it[MED_SURNAME_KEY] ?: "" }
    val medAge: Flow<String> = context.dataStore.data.map { it[MED_AGE_KEY] ?: "" }
    val medAddress: Flow<String> = context.dataStore.data.map { it[MED_ADDRESS_KEY] ?: "" }
    val medBlood: Flow<String> = context.dataStore.data.map { it[MED_BLOOD_KEY] ?: "" }
    val medChronic: Flow<String> = context.dataStore.data.map { it[MED_CHRONIC_KEY] ?: "" }

    suspend fun setProtectionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PROTECTION_ENABLED_KEY] = enabled
        }
    }

    suspend fun setPin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[PIN_KEY] = pin
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = lang
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = completed
        }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TTS_ENABLED_KEY] = enabled
        }
    }

    suspend fun setMedicalInfo(name: String, surname: String, age: String, address: String, blood: String, chronic: String) {
        context.dataStore.edit { preferences ->
            preferences[MED_NAME_KEY] = name
            preferences[MED_SURNAME_KEY] = surname
            preferences[MED_AGE_KEY] = age
            preferences[MED_ADDRESS_KEY] = address
            preferences[MED_BLOOD_KEY] = blood
            preferences[MED_CHRONIC_KEY] = chronic
        }
    }
}
