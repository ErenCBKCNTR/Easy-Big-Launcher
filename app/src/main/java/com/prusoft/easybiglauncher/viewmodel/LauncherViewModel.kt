package com.prusoft.easybiglauncher.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.prusoft.easybiglauncher.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).launcherDao()
    val repository = LauncherRepository(dao)
    val securityRepository = SecurityRepository(application)

    val pages = repository.allPages
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _pendingAssignmentItem = MutableStateFlow<LauncherItem?>(null)
    val pendingAssignmentItem = _pendingAssignmentItem.asStateFlow()

    fun setPendingAssignmentItem(item: LauncherItem?) {
        _pendingAssignmentItem.value = item
    }

    private var isAddingPage = false

    fun addPage(rowCount: Int, columnCount: Int) {
        if (isAddingPage) return
        isAddingPage = true
        viewModelScope.launch {
            try {
                val pageId = repository.insertPage(LauncherPage(pageOrder = pages.value.size, rowCount = rowCount, columnCount = columnCount)).toInt()
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
            } finally {
                isAddingPage = false
            }
        }
    }

    fun assignContactToItem(item: LauncherItem, name: String, number: String) {
        viewModelScope.launch {
            repository.updateItem(item.copy(
                itemType = ItemType.CONTACT,
                label = name,
                packageName = number // We store number in packageName field for now, or we should use a proper field. 
                // Given the current schema, let's use packageName for the "action data" which is the number.
            ))
        }
    }

    fun deletePage(page: LauncherPage) {
        viewModelScope.launch {
            repository.deletePageWithItems(page)
        }
    }
    
    fun assignAppToItem(item: LauncherItem, pkgName: String, label: String) {
        viewModelScope.launch {
            repository.updateItem(item.copy(itemType = ItemType.APP, packageName = pkgName, label = label))
        }
    }

    fun clearSlot(item: LauncherItem) {
        viewModelScope.launch {
            repository.updateItem(item.copy(
                itemType = ItemType.EMPTY, 
                packageName = null, 
                label = null, 
                iconUri = null, 
                customLabel = null, 
                customColor = null, 
                customImageUri = null
            ))
        }
    }

    fun swapItems(item1: LauncherItem, item2: LauncherItem) {
        viewModelScope.launch {
            // Swap slotIndex and pageId to move between slots/pages
            val tempItem1 = item1.copy(slotIndex = item2.slotIndex, pageId = item2.pageId)
            val tempItem2 = item2.copy(slotIndex = item1.slotIndex, pageId = item1.pageId)
            
            repository.updateItem(tempItem1)
            repository.updateItem(tempItem2)
        }
    }
}
