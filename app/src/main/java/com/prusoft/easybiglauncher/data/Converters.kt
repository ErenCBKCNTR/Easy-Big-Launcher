package com.prusoft.easybiglauncher.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromItemType(value: ItemType) = value.name

    @TypeConverter
    fun toItemType(value: String) = enumValueOf<ItemType>(value)
}
