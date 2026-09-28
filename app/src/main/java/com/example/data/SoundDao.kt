package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SoundDao {

    @Query("SELECT * FROM saved_mixes ORDER BY createdAt DESC")
    fun getAllSavedMixes(): Flow<List<SavedMixEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMix(mix: SavedMixEntity): Long

    @Delete
    suspend fun deleteMix(mix: SavedMixEntity)

    @Query("SELECT * FROM focus_sessions ORDER BY completedAt DESC")
    fun getAllFocusSessions(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE completedAt >= :sinceTimestamp")
    fun getMinutesSince(sinceTimestamp: Long): Flow<Int?>

    @Query("SELECT COUNT(*) FROM focus_sessions")
    fun getTotalSessionsCount(): Flow<Int>
}
