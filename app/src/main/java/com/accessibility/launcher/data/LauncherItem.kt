package com.accessibility.launcher.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

enum class ItemType {
    APP, CONTACT, EMPTY
}

@Entity(
    tableName = "launcher_items",
    foreignKeys = [ForeignKey(
        entity = LauncherPage::class,
        parentColumns = ["id"],
        childColumns = ["pageId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class LauncherItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pageId: Int,
    val slotIndex: Int, // 0 to (rowCount * columnCount - 1)
    val itemType: ItemType,
    val packageName: String?,
    val label: String?,
    val iconUri: String?,
    val extraData: String?, // Store phone number for contacts
    val customLabel: String?,
    val customColor: String?,
    val customImageUri: String?
)
