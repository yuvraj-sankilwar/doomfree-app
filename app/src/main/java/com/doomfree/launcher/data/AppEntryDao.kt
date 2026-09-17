package com.doomfree.launcher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppEntryDao {
    @Query("SELECT * FROM app_entry ORDER BY label ASC")
    fun observeAll(): Flow<List<AppEntry>>

    @Query("SELECT * FROM app_entry WHERE packageName = :packageName")
    suspend fun getByPackage(packageName: String): AppEntry?

    @Query("SELECT * FROM app_entry WHERE isFrequent = 1 ORDER BY frequentSortOrder ASC")
    fun observeFrequent(): Flow<List<AppEntry>>

    @Query("SELECT * FROM app_entry WHERE group_ = 'ESSENTIAL' ORDER BY label ASC")
    fun observeEssentials(): Flow<List<AppEntry>>

    @Query("SELECT * FROM app_entry WHERE group_ = 'DISTRACTION' ORDER BY label ASC")
    fun observeDistractions(): Flow<List<AppEntry>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: AppEntry)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entries: List<AppEntry>)

    @Update
    suspend fun update(entry: AppEntry)

    @Query("UPDATE app_entry SET group_ = :group WHERE packageName = :packageName")
    suspend fun setGroup(packageName: String, group: AppGroup)

    @Query("UPDATE app_entry SET group_ = 'GENERAL' WHERE group_ = :group")
    suspend fun resetGroup(group: AppGroup)

    @Query("UPDATE app_entry SET isFrequent = 0, frequentSortOrder = NULL WHERE isFrequent = 1")
    suspend fun resetFrequent()

    @Query("UPDATE app_entry SET isFrequent = :isFrequent, frequentSortOrder = :sortOrder WHERE packageName = :packageName")
    suspend fun setFrequent(packageName: String, isFrequent: Boolean, sortOrder: Int?)

    @Query("DELETE FROM app_entry WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}
