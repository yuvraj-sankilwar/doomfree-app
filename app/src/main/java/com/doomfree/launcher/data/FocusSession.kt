package com.doomfree.launcher.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_session")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val durationMs: Long,
    val endsAt: Long,
    val isActive: Boolean = true,
)
