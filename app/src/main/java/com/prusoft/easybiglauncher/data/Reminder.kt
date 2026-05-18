package com.prusoft.easybiglauncher.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val timeInMillis: Long,
    val isActive: Boolean = true,
    val type: String = "MEDICATION", // "MEDICATION" or "APPOINTMENT"
    val imageUri: String? = null
)
