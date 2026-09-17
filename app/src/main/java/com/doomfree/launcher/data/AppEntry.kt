package com.doomfree.launcher.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AppGroup { ESSENTIAL, DISTRACTION, GENERAL }

@Entity(tableName = "app_entry")
data class AppEntry(
    @PrimaryKey val packageName: String,
    val label: String,
    @ColumnInfo(name = "group_") val group: AppGroup = AppGroup.GENERAL,
    val isFrequent: Boolean = false,
    val frequentSortOrder: Int? = null,
)
