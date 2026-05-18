package com.prusoft.easybiglauncher.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "launcher_pages")
data class LauncherPage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pageOrder: Int,
    val rowCount: Int,
    val columnCount: Int
)
