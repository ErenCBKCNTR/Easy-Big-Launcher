package com.accessibility.launcher.data

import kotlinx.coroutines.flow.Flow

class LauncherRepository(private val dao: LauncherDao) {
    val allPages: Flow<List<LauncherPage>> = dao.getAllPages()
    
    fun getItemsForPage(pageId: Int) = dao.getItemsForPage(pageId)
    
    suspend fun insertPage(page: LauncherPage) = dao.insertPage(page)
    suspend fun insertItem(item: LauncherItem) = dao.insertItem(item)
    suspend fun updateItem(item: LauncherItem) = dao.updateItem(item)
}
