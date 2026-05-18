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
}
