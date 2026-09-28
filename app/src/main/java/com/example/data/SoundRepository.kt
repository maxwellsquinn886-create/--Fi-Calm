package com.example.data

import kotlinx.coroutines.flow.Flow

class SoundRepository(private val soundDao: SoundDao) {

    val allSavedMixes: Flow<List<SavedMixEntity>> = soundDao.getAllSavedMixes()
    val allFocusSessions: Flow<List<FocusSessionEntity>> = soundDao.getAllFocusSessions()
    val totalSessionsCount: Flow<Int> = soundDao.getTotalSessionsCount()

    fun getTodayFocusMinutes(todayStartMillis: Long): Flow<Int?> {
        return soundDao.getMinutesSince(todayStartMillis)
    }

    suspend fun saveMix(mix: SavedMixEntity): Long {
        return soundDao.insertMix(mix)
    }

    suspend fun deleteMix(mix: SavedMixEntity) {
        soundDao.deleteMix(mix)
    }

    suspend fun logFocusSession(durationMinutes: Int, modeName: String): Long {
        val session = FocusSessionEntity(
            durationMinutes = durationMinutes,
            modeName = modeName,
            completedAt = System.currentTimeMillis()
        )
        return soundDao.insertSession(session)
    }
}
