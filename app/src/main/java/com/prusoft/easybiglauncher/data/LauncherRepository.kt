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

    suspend fun nukeTable() {
        dao.deleteAllItems()
        dao.deleteAllPages()
        dao.deleteAllReminders()
    }

    suspend fun insertInitialData() {
        val pageId = dao.insertPage(LauncherPage(pageOrder = 0, rowCount = 3, columnCount = 2)).toInt()
        dao.insertItem(LauncherItem(pageId = pageId, slotIndex = 0, itemType = ItemType.APP, packageName = "com.android.dialer", label = "Telefon", iconUri = null))
        dao.insertItem(LauncherItem(pageId = pageId, slotIndex = 1, itemType = ItemType.APP, packageName = "com.android.messaging", label = "Mesajlar", iconUri = null))
        for (i in 2 until 6) {
            dao.insertItem(LauncherItem(pageId = pageId, slotIndex = i, itemType = ItemType.EMPTY, packageName = null, label = null, iconUri = null))
        }
    }
}
