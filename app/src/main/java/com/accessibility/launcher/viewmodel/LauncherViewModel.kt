package com.accessibility.launcher.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.accessibility.launcher.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).launcherDao()
    val repository = LauncherRepository(dao)
    val securityRepository = SecurityRepository(application)

    val pages = repository.allPages
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addPage(rowCount: Int, columnCount: Int) {
        viewModelScope.launch {
            val pageId = repository.insertPage(LauncherPage(pageOrder = 0, rowCount = rowCount, columnCount = columnCount)).toInt()
            // Create empty items
            for (i in 0 until (rowCount * columnCount)) {
                repository.insertItem(LauncherItem(
                    pageId = pageId,
                    slotIndex = i,
                    itemType = ItemType.EMPTY,
                    packageName = null,
                    label = null,
                    iconUri = null
                ))
            }
        }
    }
    
    fun assignAppToItem(item: LauncherItem, pkgName: String, label: String) {
        viewModelScope.launch {
            repository.updateItem(item.copy(itemType = ItemType.APP, packageName = pkgName, label = label))
        }
    }
}
