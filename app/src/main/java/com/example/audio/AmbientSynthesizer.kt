package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.SoundType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.*
import kotlin.random.Random

class AmbientSynthesizer {

    private val sampleRate = 22050
    private val bufferSizeSamples = 2048 // stereo frames = 4096 shorts
    private var audioTrack: AudioTrack? = null

    private val isRunning = AtomicBoolean(false)
    private var synthesisJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Channel Target Volumes (0f .. 1f) and Current Smoothed Volumes
    private val targetVolumes = mutableMapOf<SoundType, Float>()
    private val currentVolumes = mutableMapOf<SoundType, Float>()

    // Master volume
    @Volatile
    var masterVolume: Float = 0.8f

    // Master Play/Pause
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Amplitude feedback for UI animation (0f .. 1f)
    private val _currentAmplitude = MutableStateFlow(0f)
    val currentAmplitude: StateFlow<Boolean> get() = throw UnsupportedOperationException()
    val amplitudeFlow: StateFlow<Float> = _currentAmplitude.asStateFlow()

    // Internal synthesis states
    private var rainState = RainState()
    private var campfireState = CampfireState()
    private var cricketsState = CricketsState()
    private var oceanState = OceanState()
    private var windState = WindState()
    private var cafeState = CafeState()
    private var lofiChordsState = LofiChordsState()
    private var vinylState = VinylState()
    private var brownNoiseState = BrownNoiseState()
    private var alphaWavesState = AlphaWavesState()

    init {
        SoundType.values().forEach {
            targetVolumes[it] = 0f
            currentVolumes[it] = 0f
        }
    }

    fun setChannelVolume(type: SoundType, volume: Float, isEnabled: Boolean) {
        val vol = if (isEnabled) volume.coerceIn(0f, 1f) else 0f
        synchronized(targetVolumes) {
            targetVolumes[type] = vol
        }
    }

    fun setMasterPlaying(play: Boolean) {
        if (_isPlaying.value == play) return
        _isPlaying.value = play
        if (play) {
            startAudio()
        } else {
            stopAudio()
        }
    }

    @Synchronized
    private fun startAudio() {
        if (isRunning.get()) return
        isRunning.set(true)

        try {
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSizeBytes = maxOf(minBuf * 2, bufferSizeSamples * 4)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSizeBytes)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()

            synthesisJob = coroutineScope.launch {
                runSynthesisLoop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isRunning.set(false)
            _isPlaying.value = false
        }
    }

    @Synchronized
    private fun stopAudio() {
        isRunning.set(false)
        synthesisJob?.cancel()
        synthesisJob = null

        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            audioTrack = null
            _currentAmplitude.value = 0f
        }
    }

    private suspend fun runSynthesisLoop() {
        val audioData = ShortArray(bufferSizeSamples * 2) // L & R interleaved
        val dt = 1.0f / sampleRate

        while (isRunning.get()) {
            if (!coroutineScope.isActive) break

            var sumSquares = 0.0f

            for (i in 0 until bufferSizeSamples) {
                // Smoothly update volumes towards target volumes
                synchronized(targetVolumes) {
                    SoundType.values().forEach { type ->
                        val target = targetVolumes[type] ?: 0f
                        val cur = currentVolumes[type] ?: 0f
                        currentVolumes[type] = cur + (target - cur) * 0.005f
                    }
                }

                var leftSample = 0.0f
                var rightSample = 0.0f

                val vRain = currentVolumes[SoundType.RAIN] ?: 0f
                if (vRain > 0.001f) {
                    val s = rainState.nextSample(dt) * vRain
                    leftSample += s * 0.9f
                    rightSample += s * 1.1f
                }

                val vCampfire = currentVolumes[SoundType.CAMPFIRE] ?: 0f
                if (vCampfire > 0.001f) {
                    val s = campfireState.nextSample() * vCampfire
                    leftSample += s
                    rightSample += s
                }

                val vCrickets = currentVolumes[SoundType.CRICKETS] ?: 0f
                if (vCrickets > 0.001f) {
                    val (l, r) = cricketsState.nextSample(dt)
                    leftSample += l * vCrickets
                    rightSample += r * vCrickets
                }

                val vOcean = currentVolumes[SoundType.OCEAN] ?: 0f
                if (vOcean > 0.001f) {
                    val (l, r) = oceanState.nextSample(dt)
                    leftSample += l * vOcean
                    rightSample += r * vOcean
                }

                val vWind = currentVolumes[SoundType.WIND] ?: 0f
                if (vWind > 0.001f) {
                    val s = windState.nextSample() * vWind
                    leftSample += s
                    rightSample += s
                }

                val vCafe = currentVolumes[SoundType.CAFE] ?: 0f
                if (vCafe > 0.001f) {
                    val (l, r) = cafeState.nextSample(dt)
                    leftSample += l * vCafe
                    rightSample += r * vCafe
                }

                val vLofi = currentVolumes[SoundType.LOFI_CHORDS] ?: 0f
                if (vLofi > 0.001f) {
                    val (l, r) = lofiChordsState.nextSample(dt)
                    leftSample += l * vLofi
                    rightSample += r * vLofi
                }

                val vVinyl = currentVolumes[SoundType.VINYL] ?: 0f
                if (vVinyl > 0.001f) {
                    val s = vinylState.nextSample(dt) * vVinyl
                    leftSample += s
                    rightSample += s
                }

                val vBrown = currentVolumes[SoundType.BROWN_NOISE] ?: 0f
                if (vBrown > 0.001f) {
                    val s = brownNoiseState.nextSample() * vBrown
                    leftSample += s
                    rightSample += s
                }

                val vAlpha = currentVolumes[SoundType.ALPHA_WAVES] ?: 0f
                if (vAlpha > 0.001f) {
                    val (l, r) = alphaWavesState.nextSample(dt)
                    leftSample += l * vAlpha
                    rightSample += r * vAlpha
                }

                // Apply master volume and soft limiting (tanh-like curve)
                val m = masterVolume
                val outLeft = softClip(leftSample * m * 0.45f)
                val outRight = softClip(rightSample * m * 0.45f)

                audioData[i * 2] = (outLeft * 32767f).toInt().coerceIn(-32768, 32767).toShort()
                audioData[i * 2 + 1] = (outRight * 32767f).toInt().coerceIn(-32768, 32767).toShort()

                sumSquares += (outLeft * outLeft + outRight * outRight) * 0.5f
            }

            // Write to AudioTrack
            val track = audioTrack
            if (track != null && isRunning.get()) {
                track.write(audioData, 0, audioData.size)
            }

            // Calculate RMS amplitude for UI feedback
            val rms = sqrt(sumSquares / bufferSizeSamples).coerceIn(0f, 1f)
            _currentAmplitude.value = rms
        }
    }

    private fun softClip(x: Float): Float {
        return if (x > 1.0f) {
            1.0f - (1.0f / (x + 1.0f))
        } else if (x < -1.0f) {
            -1.0f + (1.0f / (-x + 1.0f))
        } else {
            x - (x * x * x) / 6.0f
        }
    }

    fun release() {
        stopAudio()
        coroutineScope.cancel()
    }

    // --- SYNTHESIZER INNER GENERATORS ---

    private class RainState {
        private var pinkB0 = 0f
        private var pinkB1 = 0f
        private var pinkB2 = 0f
        private var dropTimer = 0f
        private var dropDecay = 0f
        private var dropFreq = 600f
        private var dropPhase = 0f

        fun nextSample(dt: Float): Float {
            // Pink noise filter approximation
            val white = (Random.nextFloat() * 2f - 1f)
            pinkB0 = 0.99765f * pinkB0 + white * 0.0990460f
            pinkB1 = 0.96300f * pinkB1 + white * 0.2965164f
            pinkB2 = 0.57000f * pinkB2 + white * 1.0526913f
            val pink = (pinkB0 + pinkB1 + pinkB2 + white * 0.1848f) * 0.15f

            // Drops
            dropTimer -= dt
            if (dropTimer <= 0f) {
                dropTimer = Random.nextFloat() * 0.12f + 0.03f
                dropDecay = 1.0f
                dropFreq = Random.nextFloat() * 800f + 400f
            }

            var drop = 0f
            if (dropDecay > 0.01f) {
                dropPhase += 2f * Math.PI.toFloat() * dropFreq * dt
                drop = sin(dropPhase) * dropDecay * 0.35f
                dropDecay *= 0.996f
            }

            return pink * 0.7f + drop
        }
    }

    private class CampfireState {
        private var rumble = 0f
        fun nextSample(): Float {
            val white = Random.nextFloat() * 2f - 1f
            rumble = rumble * 0.985f + white * 0.015f

            // Crackles/pops
            val pop = if (Random.nextFloat() < 0.0007f) {
                (Random.nextFloat() * 2f - 1f) * 0.85f
            } else 0f

            return rumble * 2.5f + pop
        }
    }

    private class CricketsState {
        private var chirpTimer = 0f
        private var chirpPhase1 = 0f
        private var chirpPhase2 = 0f
        private var envelope = 0f

        fun nextSample(dt: Float): Pair<Float, Float> {
            chirpTimer += dt
            if (chirpTimer > 1.2f) {
                chirpTimer = 0f
            }

            // Rapid chirp burst during first 0.4 seconds
            if (chirpTimer < 0.35f) {
                val cycle = (chirpTimer * 16f) % 1.0f
                envelope = sin(cycle * Math.PI.toFloat()).coerceAtLeast(0f)
            } else {
                envelope = 0f
            }

            chirpPhase1 += 2f * Math.PI.toFloat() * 4400f * dt
            chirpPhase2 += 2f * Math.PI.toFloat() * 4750f * dt

            val tone1 = sin(chirpPhase1) * envelope * 0.2f
            val tone2 = sin(chirpPhase2) * envelope * 0.2f
            return Pair(tone1, tone2)
        }
    }

    private class OceanState {
        private var pink = 0f
        private var lfoPhase = 0f

        fun nextSample(dt: Float): Pair<Float, Float> {
            lfoPhase += dt * 0.16f // ~6.2s period
            if (lfoPhase > 2f * Math.PI.toFloat()) lfoPhase -= 2f * Math.PI.toFloat()

            // Swell envelope: sine squared for smooth rolling swell
            val swell = (sin(lfoPhase) * 0.5f + 0.5f).pow(2.2f)
            val white = Random.nextFloat() * 2f - 1f
            pink = pink * 0.94f + white * 0.06f

            val sound = pink * swell * 2.8f
            return Pair(sound * 0.95f, sound * 1.05f)
        }
    }

    private class WindState {
        private var filter = 0f
        private var centerFreqMod = 0f
        private var lfo = 0f

        fun nextSample(): Float {
            lfo += 0.0001f
            centerFreqMod = (sin(lfo) * 0.04f + 0.08f)
            val white = Random.nextFloat() * 2f - 1f
            filter += centerFreqMod * (white - filter)
            return filter * 1.8f
        }
    }

    private class CafeState {
        private var murmur = 0f
        private var cupDecay = 0f
        private var cupPhase = 0f
        private var cupTimer = 2.0f

        fun nextSample(dt: Float): Pair<Float, Float> {
            val white = Random.nextFloat() * 2f - 1f
            murmur = murmur * 0.93f + white * 0.07f

            cupTimer -= dt
            if (cupTimer <= 0f) {
                cupTimer = Random.nextFloat() * 4.0f + 1.5f
                cupDecay = 0.5f
                cupPhase = 0f
            }

            var clink = 0f
            if (cupDecay > 0.001f) {
                cupPhase += 2f * Math.PI.toFloat() * 2400f * dt
                clink = sin(cupPhase) * cupDecay * 0.2f
                cupDecay *= 0.997f
            }

            val l = murmur * 1.2f + clink
            val r = murmur * 1.2f + clink * 0.6f
            return Pair(l, r)
        }
    }

    private class LofiChordsState {
        // Chord cycle: 4 chords, 4 seconds per chord = 16 seconds cycle
        // Chords (frequencies):
        // 0: Dmaj7: D3 (146.83), F#3 (185.00), A3 (220.00), C#4 (277.18)
        // 1: Bm7:   B2 (123.47), D3 (146.83), F#3 (185.00), A3 (220.00)
        // 2: Em7:   E3 (164.81), G3 (196.00), B3 (246.94), D4 (293.66)
        // 3: A7:    A2 (110.00), E3 (164.81), G3 (196.00), C#4 (277.18)
        private val chords = listOf(
            floatArrayOf(146.83f, 185.00f, 220.00f, 277.18f),
            floatArrayOf(123.47f, 146.83f, 185.00f, 220.00f),
            floatArrayOf(164.81f, 196.00f, 246.94f, 293.66f),
            floatArrayOf(110.00f, 164.81f, 196.00f, 277.18f)
        )

        private var timer = 0f
        private val phases = FloatArray(4)
        private var chordIndex = 0
        private var wowFlutter = 0f

        fun nextSample(dt: Float): Pair<Float, Float> {
            timer += dt
            val chordDur = 4.2f
            val curPos = timer % chordDur
            chordIndex = ((timer / chordDur).toInt()) % chords.size

            // Vintage flutter (subtle vibrato)
            wowFlutter += dt * 3.5f
            val vibrato = 1.0f + sin(wowFlutter) * 0.003f

            // Attack & gentle decay envelope
            val env = if (curPos < 0.15f) {
                curPos / 0.15f
            } else {
                exp(-(curPos - 0.15f) * 0.55f).coerceAtLeast(0.08f)
            }

            val currentFreqs = chords[chordIndex]
            var mixLeft = 0f
            var mixRight = 0f

            for (n in currentFreqs.indices) {
                val f = currentFreqs[n] * vibrato
                phases[n] += 2f * Math.PI.toFloat() * f * dt
                if (phases[n] > 2f * Math.PI.toFloat()) phases[n] -= 2f * Math.PI.toFloat()

                // Electric piano tone: Fundamental + soft 2nd harmonic
                val s1 = sin(phases[n])
                val s2 = sin(phases[n] * 2.01f) * 0.25f
                val note = (s1 + s2) * env * 0.22f

                if (n % 2 == 0) {
                    mixLeft += note * 1.1f
                    mixRight += note * 0.9f
                } else {
                    mixLeft += note * 0.9f
                    mixRight += note * 1.1f
                }
            }

            return Pair(mixLeft, mixRight)
        }
    }

    private class VinylState {
        private var humPhase = 0f
        fun nextSample(dt: Float): Float {
            humPhase += 2f * Math.PI.toFloat() * 50f * dt
            val hum = sin(humPhase) * 0.03f
            val crackle = if (Random.nextFloat() < 0.0012f) {
                (Random.nextFloat() * 2f - 1f) * 0.4f
            } else {
                (Random.nextFloat() * 2f - 1f) * 0.035f
            }
            return crackle + hum
        }
    }

    private class BrownNoiseState {
        private var brown = 0f
        fun nextSample(): Float {
            val white = Random.nextFloat() * 2f - 1f
            brown = (brown * 0.97f + white * 0.03f)
            return brown * 3.8f
        }
    }

    private class AlphaWavesState {
        private var phaseL = 0f
        private var phaseR = 0f
        fun nextSample(dt: Float): Pair<Float, Float> {
            phaseL += 2f * Math.PI.toFloat() * 200f * dt // 200 Hz
            phaseR += 2f * Math.PI.toFloat() * 210f * dt // 210 Hz (10 Hz binaural beat)
            val l = sin(phaseL) * 0.25f
            val r = sin(phaseR) * 0.25f
            return Pair(l, r)
        }
    }
}
