package com.doomfree.launcher.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromGroup(group: AppGroup): String = group.name

    @TypeConverter
    fun toGroup(value: String): AppGroup = AppGroup.valueOf(value)
}
