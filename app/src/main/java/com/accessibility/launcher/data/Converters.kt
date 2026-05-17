package com.accessibility.launcher.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromItemType(value: ItemType) = value.name

    @TypeConverter
    fun toItemType(value: String) = enumValueOf<ItemType>(value)
}
