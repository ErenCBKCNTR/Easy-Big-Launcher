package com.prusoft.easybiglauncher.data

import kotlinx.coroutines.flow.Flow

class LauncherRepository(private val dao: LauncherDao) {
    val allPages: Flow<List<LauncherPage>> = dao.getAllPages()
    
    fun getItemsForPage(pageId: Int) = dao.getItemsForPage(pageId)
    
    suspend fun insertPage(page: LauncherPage) = dao.insertPage(page)
    suspend fun insertItem(item: LauncherItem) = dao.insertItem(item)
    suspend fun updateItem(item: LauncherItem) = dao.updateItem(item)
    
    suspend fun deletePageWithItems(page: LauncherPage) {
        dao.deleteItemsByPageId(page.id)
        dao.deletePage(page)
    }

    // Reminders
    val allReminders: Flow<List<Reminder>> = dao.getAllReminders()
    suspend fun insertReminder(reminder: Reminder) = dao.insertReminder(reminder)
    suspend fun updateReminder(reminder: Reminder) = dao.updateReminder(reminder)
    suspend fun deleteReminder(reminder: Reminder) = dao.deleteReminder(reminder)
    suspend fun getReminderById(id: Int) = dao.getReminderById(id)
}
