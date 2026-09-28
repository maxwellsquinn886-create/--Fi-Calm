package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_mixes")
data class SavedMixEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val rainVolume: Float = 0f,
    val campfireVolume: Float = 0f,
    val cricketsVolume: Float = 0f,
    val oceanVolume: Float = 0f,
    val windVolume: Float = 0f,
    val cafeVolume: Float = 0f,
    val lofiChordsVolume: Float = 0f,
    val vinylVolume: Float = 0f,
    val brownNoiseVolume: Float = 0f,
    val alphaWavesVolume: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val durationMinutes: Int,
    val modeName: String,
    val completedAt: Long = System.currentTimeMillis()
)
