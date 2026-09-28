package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AmbientSynthesizer
import com.example.data.AppDatabase
import com.example.data.SavedMixEntity
import com.example.data.SoundRepository
import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Calendar

class LofiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SoundRepository
    private val synthesizer = AmbientSynthesizer()

    // Sound Channels State
    private val _channels = MutableStateFlow<Map<SoundType, SoundChannelState>>(
        SoundType.values().associateWith { type ->
            SoundChannelState(
                type = type,
                isEnabled = false,
                volume = type.defaultVolume
            )
        }
    )
    val channels: StateFlow<Map<SoundType, SoundChannelState>> = _channels.asStateFlow()

    // Master Controls
    val isPlaying: StateFlow<Boolean> = synthesizer.isPlaying
    private val _masterVolume = MutableStateFlow(0.85f)
    val masterVolume: StateFlow<Float> = _masterVolume.asStateFlow()

    val amplitude: StateFlow<Float> = synthesizer.amplitudeFlow

    // Active Preset
    private val _activePresetId = MutableStateFlow<String?>("cozy_study")
    val activePresetId: StateFlow<String?> = _activePresetId.asStateFlow()

    // Active Scene
    private val _activeScene = MutableStateFlow(SceneType.COZY_ROOM)
    val activeScene: StateFlow<SceneType> = _activeScene.asStateFlow()

    // Zen Mode
    private val _isZenMode = MutableStateFlow(false)
    val isZenMode: StateFlow<Boolean> = _isZenMode.asStateFlow()

    // Language (Default Arabic true)
    private val _isArabic = MutableStateFlow(true)
    val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

    // Timer / Pomodoro
    data class TimerUiState(
        val isRunning: Boolean = false,
        val remainingSeconds: Int = 25 * 60,
        val totalSeconds: Int = 25 * 60,
        val mode: TimerMode = TimerMode.POMODORO,
        val fadeOnFinish: Boolean = true,
        val sessionsCompletedToday: Int = 0
    )

    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()
    private var timerJob: Job? = null

    // Room Data
    val savedMixes: StateFlow<List<SavedMixEntity>>
    val todayFocusMinutes: StateFlow<Int>
    val totalSessionsCount: StateFlow<Int>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SoundRepository(database.soundDao())

        savedMixes = repository.allSavedMixes.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        totalSessionsCount = repository.totalSessionsCount.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        todayFocusMinutes = repository.getTodayFocusMinutes(startOfDay)
            .map { it ?: 0 }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                0
            )

        // Apply default preset "Cozy Study"
        applyPreset(CURATED_PRESETS.first())
    }

    fun togglePlayPause() {
        val current = isPlaying.value
        synthesizer.setMasterPlaying(!current)
    }

    fun setMasterVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _masterVolume.value = clamped
        synthesizer.masterVolume = clamped
    }

    fun toggleChannel(type: SoundType) {
        val current = _channels.value[type] ?: return
        val updated = current.copy(isEnabled = !current.isEnabled)
        val newMap = _channels.value.toMutableMap()
        newMap[type] = updated
        _channels.value = newMap

        synthesizer.setChannelVolume(type, updated.volume, updated.isEnabled)

        // If toggling on and master wasn't playing, start audio
        if (updated.isEnabled && !isPlaying.value) {
            synthesizer.setMasterPlaying(true)
        }
        _activePresetId.value = null
    }

    fun setChannelVolume(type: SoundType, volume: Float) {
        val current = _channels.value[type] ?: return
        val updated = current.copy(volume = volume.coerceIn(0f, 1f))
        val newMap = _channels.value.toMutableMap()
        newMap[type] = updated
        _channels.value = newMap

        synthesizer.setChannelVolume(type, updated.volume, updated.isEnabled)
        _activePresetId.value = null
    }

    fun applyPreset(preset: PresetMix) {
        _activePresetId.value = preset.id
        val newMap = mutableMapOf<SoundType, SoundChannelState>()

        SoundType.values().forEach { type ->
            val presetVol = preset.volumes[type]
            if (presetVol != null && presetVol > 0f) {
                newMap[type] = SoundChannelState(type = type, isEnabled = true, volume = presetVol)
                synthesizer.setChannelVolume(type, presetVol, true)
            } else {
                newMap[type] = SoundChannelState(type = type, isEnabled = false, volume = type.defaultVolume)
                synthesizer.setChannelVolume(type, 0f, false)
            }
        }
        _channels.value = newMap

        if (!isPlaying.value) {
            synthesizer.setMasterPlaying(true)
        }
    }

    fun applySavedMix(mix: SavedMixEntity) {
        _activePresetId.value = "saved_${mix.id}"
        val volumes = mapOf(
            SoundType.RAIN to mix.rainVolume,
            SoundType.CAMPFIRE to mix.campfireVolume,
            SoundType.CRICKETS to mix.cricketsVolume,
            SoundType.OCEAN to mix.oceanVolume,
            SoundType.WIND to mix.windVolume,
            SoundType.CAFE to mix.cafeVolume,
            SoundType.LOFI_CHORDS to mix.lofiChordsVolume,
            SoundType.VINYL to mix.vinylVolume,
            SoundType.BROWN_NOISE to mix.brownNoiseVolume,
            SoundType.ALPHA_WAVES to mix.alphaWavesVolume
        )

        val newMap = mutableMapOf<SoundType, SoundChannelState>()
        SoundType.values().forEach { type ->
            val vol = volumes[type] ?: 0f
            if (vol > 0.01f) {
                newMap[type] = SoundChannelState(type = type, isEnabled = true, volume = vol)
                synthesizer.setChannelVolume(type, vol, true)
            } else {
                newMap[type] = SoundChannelState(type = type, isEnabled = false, volume = type.defaultVolume)
                synthesizer.setChannelVolume(type, 0f, false)
            }
        }
        _channels.value = newMap

        if (!isPlaying.value) {
            synthesizer.setMasterPlaying(true)
        }
    }

    fun saveCurrentMix(name: String) {
        if (name.isBlank()) return
        val currentChannels = _channels.value
        fun vol(t: SoundType): Float {
            val st = currentChannels[t]
            return if (st?.isEnabled == true) st.volume else 0f
        }

        viewModelScope.launch {
            val entity = SavedMixEntity(
                name = name.trim(),
                rainVolume = vol(SoundType.RAIN),
                campfireVolume = vol(SoundType.CAMPFIRE),
                cricketsVolume = vol(SoundType.CRICKETS),
                oceanVolume = vol(SoundType.OCEAN),
                windVolume = vol(SoundType.WIND),
                cafeVolume = vol(SoundType.CAFE),
                lofiChordsVolume = vol(SoundType.LOFI_CHORDS),
                vinylVolume = vol(SoundType.VINYL),
                brownNoiseVolume = vol(SoundType.BROWN_NOISE),
                alphaWavesVolume = vol(SoundType.ALPHA_WAVES)
            )
            repository.saveMix(entity)
        }
    }

    fun deleteSavedMix(mix: SavedMixEntity) {
        viewModelScope.launch {
            repository.deleteMix(mix)
        }
    }

    fun selectScene(scene: SceneType) {
        _activeScene.value = scene
    }

    fun setZenMode(enabled: Boolean) {
        _isZenMode.value = enabled
    }

    fun toggleLanguage() {
        _isArabic.value = !_isArabic.value
    }

    // --- TIMER / POMODORO LOGIC ---

    fun setTimerMode(mode: TimerMode) {
        timerJob?.cancel()
        val totalSec = mode.defaultMinutes * 60
        _timerState.value = _timerState.value.copy(
            isRunning = false,
            remainingSeconds = totalSec,
            totalSeconds = totalSec,
            mode = mode
        )
    }

    fun toggleTimer() {
        if (_timerState.value.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = true)

        timerJob = viewModelScope.launch {
            while (_timerState.value.remainingSeconds > 0 && _timerState.value.isRunning) {
                delay(1000L)
                val remaining = _timerState.value.remainingSeconds - 1
                _timerState.value = _timerState.value.copy(remainingSeconds = remaining)

                // Smooth fade-out in final 20 seconds if sleep mode
                if (_timerState.value.fadeOnFinish && remaining in 1..20) {
                    val fadeRatio = remaining / 20f
                    synthesizer.masterVolume = _masterVolume.value * fadeRatio
                }
            }

            if (_timerState.value.remainingSeconds <= 0) {
                onTimerFinished()
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = false)
        // Restore volume if paused during fade
        synthesizer.masterVolume = _masterVolume.value
    }

    fun resetTimer() {
        timerJob?.cancel()
        val total = _timerState.value.totalSeconds
        _timerState.value = _timerState.value.copy(
            isRunning = false,
            remainingSeconds = total
        )
        synthesizer.masterVolume = _masterVolume.value
    }

    fun toggleFadeOnFinish() {
        _timerState.value = _timerState.value.copy(fadeOnFinish = !_timerState.value.fadeOnFinish)
    }

    private fun onTimerFinished() {
        _timerState.value = _timerState.value.copy(isRunning = false)
        synthesizer.setMasterPlaying(false)
        synthesizer.masterVolume = _masterVolume.value

        // Gentle haptic feedback
        triggerVibration()

        // Log focus session if it wasn't a break
        val mode = _timerState.value.mode
        if (!mode.isBreak) {
            viewModelScope.launch {
                repository.logFocusSession(
                    durationMinutes = mode.defaultMinutes,
                    modeName = mode.titleAr
                )
            }
        }
    }

    private fun triggerVibration() {
        try {
            val context = getApplication<Application>()
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 400), -1)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(500)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        synthesizer.release()
    }
}
