package com.doomfree.launcher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_session WHERE isActive = 1 ORDER BY id DESC LIMIT 1")
    fun observeActive(): Flow<FocusSession?>

    @Query("SELECT * FROM focus_session WHERE isActive = 1 ORDER BY id DESC LIMIT 1")
    suspend fun getActive(): FocusSession?

    @Insert
    suspend fun insert(session: FocusSession): Long

    @Query("UPDATE focus_session SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Query("UPDATE focus_session SET isActive = 0 WHERE isActive = 1")
    suspend fun deactivateAll()
}
