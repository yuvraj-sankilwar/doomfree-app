package com.doomfree.launcher.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_item")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
