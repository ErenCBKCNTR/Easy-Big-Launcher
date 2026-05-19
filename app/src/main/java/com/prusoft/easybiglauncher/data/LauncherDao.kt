package com.prusoft.easybiglauncher.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LauncherDao {
    @Query("SELECT * FROM launcher_pages ORDER BY pageOrder ASC")
    fun getAllPages(): Flow<List<LauncherPage>>

    @Insert
    suspend fun insertPage(page: LauncherPage): Long

    @Query("SELECT * FROM launcher_items WHERE pageId = :pageId ORDER BY slotIndex ASC")
    fun getItemsForPage(pageId: Int): Flow<List<LauncherItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LauncherItem)

    @Update
    suspend fun updateItem(item: LauncherItem)

    @Delete
    suspend fun deletePage(page: LauncherPage)

    @Query("DELETE FROM launcher_items WHERE pageId = :pageId")
    suspend fun deleteItemsByPageId(pageId: Int)

    // Reminders
    @Query("SELECT * FROM reminders ORDER BY timeInMillis ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Int): Reminder?

    @Query("DELETE FROM launcher_pages")
    suspend fun deleteAllPages()

    @Query("DELETE FROM launcher_items")
    suspend fun deleteAllItems()

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()
}
