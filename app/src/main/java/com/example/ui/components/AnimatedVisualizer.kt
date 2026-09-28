package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.SceneType
import com.example.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

data class TouchRipple(
    val id: Long,
    val x: Float,
    val y: Float,
    val startTime: Long
)

@Composable
fun AnimatedVisualizer(
    sceneType: SceneType,
    isPlaying: Boolean,
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    var ripples by remember { mutableStateOf(listOf<TouchRipple>()) }
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer_infinite")

    // Continuous time animation for smooth particles & waves
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "anim_progress"
    )

    val lampPulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lamp_pulse"
    )

    // Vinyl rotation
    val vinylAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_rotation"
    )

    // Filter old ripples
    val currentTime = System.currentTimeMillis()
    val activeRipples = ripples.filter { currentTime - it.startTime < 1200 }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(LofiSurface)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newRipple = TouchRipple(
                        id = System.nanoTime(),
                        x = offset.x,
                        y = offset.y,
                        startTime = System.currentTimeMillis()
                    )
                    ripples = (activeRipples + newRipple).takeLast(6)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        when (sceneType) {
            SceneType.COZY_ROOM -> {
                CozyRoomScene(
                    isPlaying = isPlaying,
                    amplitude = amplitude,
                    animProgress = animProgress,
                    lampPulse = lampPulse,
                    activeRipples = activeRipples
                )
            }
            SceneType.VINYL_RECORD -> {
                VinylTurntableScene(
                    isPlaying = isPlaying,
                    amplitude = amplitude,
                    rotationAngle = if (isPlaying) vinylAngle else 0f,
                    animProgress = animProgress,
                    activeRipples = activeRipples
                )
            }
            SceneType.CAMPFIRE_NIGHT -> {
                CampfireNightScene(
                    isPlaying = isPlaying,
                    amplitude = amplitude,
                    animProgress = animProgress,
                    activeRipples = activeRipples
                )
            }
            SceneType.ZEN_MINIMAL -> {
                ZenMinimalScene(
                    isPlaying = isPlaying,
                    amplitude = amplitude,
                    animProgress = animProgress,
                    activeRipples = activeRipples
                )
            }
        }
    }
}

@Composable
private fun CozyRoomScene(
    isPlaying: Boolean,
    amplitude: Float,
    animProgress: Float,
    lampPulse: Float,
    activeRipples: List<TouchRipple>
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Base illustration
        Image(
            painter = painterResource(id = R.drawable.bg_lofi_study),
            contentDescription = "Lo-Fi Study Room",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark gradient vignette for cozy cinematic feel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x330F111A),
                            Color(0x440F111A),
                            Color(0xBB0F111A)
                        )
                    )
                )
        )

        // Canvas for animated rain on window, coffee steam, warm lamp glow, and audio waves
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Lamp Warm Glow (top-left or mid-right desk)
            val lampCenter = Offset(w * 0.78f, h * 0.42f)
            val glowRadius = (w * 0.35f) * (0.85f + amplitude * 0.5f + lampPulse * 0.15f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x66FBBF24),
                        Color(0x22F59E0B),
                        Color.Transparent
                    ),
                    center = lampCenter,
                    radius = glowRadius
                ),
                radius = glowRadius,
                center = lampCenter
            )

            // 2. Animated Rain on Window
            val rainSpeed = if (isPlaying) 1.2f else 0.4f
            val numDrops = 40
            for (i in 0 until numDrops) {
                val seed = i * 179 + 31
                val xNorm = ((seed % 100) / 100f)
                val initialY = ((seed * 73 % 100) / 100f)
                val dropY = (initialY + animProgress * rainSpeed * ((i % 3 + 1) * 0.7f)) % 1.0f

                val startX = xNorm * w
                val startY = dropY * h
                val dropLength = 12f + (i % 5) * 4f

                drawLine(
                    color = Color(0x6693C5FD),
                    start = Offset(startX, startY),
                    end = Offset(startX - 2f, startY + dropLength),
                    strokeWidth = 1.6f,
                    cap = StrokeCap.Round
                )
            }

            // 3. Animated Coffee Steam spirals
            val coffeeCup = Offset(w * 0.35f, h * 0.65f)
            val steamPoints = 16
            val steamPath = Path()
            steamPath.moveTo(coffeeCup.x, coffeeCup.y)
            for (s in 1..steamPoints) {
                val progress = s / steamPoints.toFloat()
                val py = coffeeCup.y - progress * 80f
                val waveOffset = sin((animProgress * 8f) + s * 0.6f) * (progress * 14f)
                val px = coffeeCup.x + waveOffset
                steamPath.lineTo(px, py)
            }
            drawPath(
                path = steamPath,
                color = Color(0x33E2E8F0),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // 4. Audio Reactive Waveform along the bottom
            val numBars = 32
            val barWidth = (w / numBars) * 0.65f
            val baseBarY = h - 20f
            for (b in 0 until numBars) {
                val bx = b * (w / numBars) + barWidth * 0.5f
                val freqFactor = sin(b * 0.3f + animProgress * 12f) * 0.5f + 0.5f
                val barHeight = 8f + (amplitude * 55f * freqFactor) + (sin(b.toFloat()) * 4f).coerceAtLeast(0f)

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(LofiPrimary, LofiAmber),
                        startY = baseBarY - barHeight,
                        endY = baseBarY
                    ),
                    topLeft = Offset(bx, baseBarY - barHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
            }

            // 5. Draw touch ripples
            drawTouchRipples(activeRipples, this)
        }
    }
}

@Composable
private fun VinylTurntableScene(
    isPlaying: Boolean,
    amplitude: Float,
    rotationAngle: Float,
    animProgress: Float,
    activeRipples: List<TouchRipple>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1E2132), Color(0xFF0F111A)),
                    radius = 800f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Turntable Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.45f, size.height * 0.5f)
            val recordRadius = minOf(size.width, size.height) * 0.40f

            // Concentric pulsating audio reactive glow rings around record
            if (isPlaying && amplitude > 0.02f) {
                val pulseRadius = recordRadius + (amplitude * 60f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x44C084FC),
                            Color(0x11A5B4FC),
                            Color.Transparent
                        ),
                        center = center,
                        radius = pulseRadius
                    ),
                    radius = pulseRadius,
                    center = center
                )
            }

            // Vinyl base shadow
            drawCircle(
                color = Color(0x88000000),
                radius = recordRadius + 8f,
                center = center.copy(y = center.y + 6f)
            )

            // Vinyl Body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF262938), Color(0xFF13151F), Color(0xFF0A0B10)),
                    center = center,
                    radius = recordRadius
                ),
                radius = recordRadius,
                center = center
            )

            // Vinyl Grooves (concentric thin circles)
            val grooveCount = 14
            for (g in 1..grooveCount) {
                val r = recordRadius * (0.38f + (g / grooveCount.toFloat()) * 0.58f)
                drawCircle(
                    color = Color(0x22FFFFFF),
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.0f)
                )
            }

            // Light sheen/reflection arc across vinyl
            val sheenAngle = (rotationAngle * 0.5f) % 360f
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x1AFFFFFF),
                        Color.Transparent,
                        Color(0x1AFFFFFF),
                        Color.Transparent
                    ),
                    center = center
                ),
                startAngle = sheenAngle,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - recordRadius, center.y - recordRadius),
                size = androidx.compose.ui.geometry.Size(recordRadius * 2, recordRadius * 2)
            )

            // Record Label (Center circle)
            val labelRadius = recordRadius * 0.35f
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(LofiAmber, LofiRose, LofiPurple),
                    start = Offset(center.x - labelRadius, center.y - labelRadius),
                    end = Offset(center.x + labelRadius, center.y + labelRadius)
                ),
                radius = labelRadius,
                center = center
            )

            // Spindle hole
            drawCircle(
                color = Color(0xFF0F111A),
                radius = labelRadius * 0.16f,
                center = center
            )
            drawCircle(
                color = Color(0xFFCBD5E1),
                radius = labelRadius * 0.10f,
                center = center
            )

            // Tonearm
            val armPivot = Offset(size.width * 0.88f, size.height * 0.22f)
            val armTarget = if (isPlaying) {
                Offset(center.x + recordRadius * 0.70f, center.y - recordRadius * 0.15f)
            } else {
                Offset(size.width * 0.85f, size.height * 0.75f)
            }

            // Pivot base
            drawCircle(
                color = Color(0xFF475569),
                radius = 16f,
                center = armPivot
            )
            drawCircle(
                color = Color(0xFF94A3B8),
                radius = 9f,
                center = armPivot
            )

            // Metallic arm body
            drawLine(
                color = Color(0xFFCBD5E1),
                start = armPivot,
                end = armTarget,
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )

            // Cartridge / stylus head
            val headEnd = armTarget + Offset(-12f, 8f)
            drawLine(
                color = LofiAmber,
                start = armTarget,
                end = headEnd,
                strokeWidth = 9f,
                cap = StrokeCap.Square
            )

            // Draw touch ripples
            drawTouchRipples(activeRipples, this)
        }
    }
}

@Composable
private fun CampfireNightScene(
    isPlaying: Boolean,
    amplitude: Float,
    animProgress: Float,
    activeRipples: List<TouchRipple>
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF08090F), Color(0xFF0F1424), Color(0xFF1C1322))
                )
            )
    ) {
        val w = size.width
        val h = size.height

        // 1. Twinkling Stars in twilight sky
        for (i in 0 until 50) {
            val sx = ((i * 127) % 1000) / 1000f * w
            val sy = ((i * 311) % 550) / 1000f * h
            val starTwinkle = sin(animProgress * 15f + i) * 0.4f + 0.6f
            drawCircle(
                color = Color.White.copy(alpha = (starTwinkle * 0.7f).coerceIn(0.1f, 0.95f)),
                radius = 1.2f + (i % 3) * 0.8f,
                center = Offset(sx, sy)
            )
        }

        // 2. Crescent Moon
        val moonCenter = Offset(w * 0.82f, h * 0.22f)
        drawCircle(
            color = Color(0xFFFEF3C7),
            radius = 28f,
            center = moonCenter
        )
        drawCircle(
            color = Color(0xFF0B0E1B),
            radius = 24f,
            center = Offset(moonCenter.x - 10f, moonCenter.y - 6f)
        )

        // 3. Campfire Glow and Base
        val fireBase = Offset(w * 0.5f, h * 0.78f)
        val fireRadius = (w * 0.32f) * (0.85f + amplitude * 0.8f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x66F97316),
                    Color(0x33FBBF24),
                    Color(0x11EA580C),
                    Color.Transparent
                ),
                center = fireBase,
                radius = fireRadius
            ),
            radius = fireRadius,
            center = fireBase
        )

        // 4. Wooden logs
        drawLine(
            color = Color(0xFF451A03),
            start = Offset(fireBase.x - 45f, fireBase.y + 12f),
            end = Offset(fireBase.x + 45f, fireBase.y - 6f),
            strokeWidth = 14f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF78350F),
            start = Offset(fireBase.x + 45f, fireBase.y + 12f),
            end = Offset(fireBase.x - 45f, fireBase.y - 6f),
            strokeWidth = 14f,
            cap = StrokeCap.Round
        )

        // 5. Rising Spark Particles (Embers)
        val numSparks = 24
        for (i in 0 until numSparks) {
            val progress = ((animProgress * 1.5f + (i / numSparks.toFloat())) % 1.0f)
            val sparkY = fireBase.y - progress * (h * 0.45f)
            val wobble = sin(animProgress * 8f + i) * (progress * 28f)
            val sparkX = fireBase.x + wobble + ((i % 5 - 2) * 8f)
            val alpha = (1.0f - progress).coerceIn(0f, 1f)

            drawCircle(
                color = if (i % 2 == 0) LofiAmber.copy(alpha = alpha) else LofiRose.copy(alpha = alpha),
                radius = 2.2f + (i % 3) * 1.2f,
                center = Offset(sparkX, sparkY)
            )
        }

        // Draw touch ripples
        drawTouchRipples(activeRipples, this)
    }
}

@Composable
private fun ZenMinimalScene(
    isPlaying: Boolean,
    amplitude: Float,
    animProgress: Float,
    activeRipples: List<TouchRipple>
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1E2337), Color(0xFF0F111A)),
                    radius = 700f
                )
            )
    ) {
        val center = Offset(size.width * 0.5f, size.height * 0.5f)
        val baseRadius = minOf(size.width, size.height) * 0.28f

        // Pulsating Aura Rings
        val numRings = 5
        for (r in 1..numRings) {
            val ringProgress = (animProgress * 0.6f + r / numRings.toFloat()) % 1.0f
            val rad = baseRadius + ringProgress * (baseRadius * 0.9f) + amplitude * 40f
            val alpha = ((1.0f - ringProgress) * 0.4f * (if (isPlaying) 1.0f else 0.4f)).coerceIn(0f, 1f)

            drawCircle(
                color = LofiPrimary.copy(alpha = alpha),
                radius = rad,
                center = center,
                style = Stroke(width = 2.5f)
            )
        }

        // Minimal Zen Stone Balance or Glowing Core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(LofiPrimary, LofiPurple, Color(0xFF1E1B4B)),
                center = center,
                radius = baseRadius
            ),
            radius = baseRadius * (0.85f + amplitude * 0.25f),
            center = center
        )

        // Inner glowing core
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = baseRadius * 0.22f * (1f + amplitude * 0.35f),
            center = center
        )

        // Draw touch ripples
        drawTouchRipples(activeRipples, this)
    }
}

private fun drawTouchRipples(
    ripples: List<TouchRipple>,
    scope: androidx.compose.ui.graphics.drawscope.DrawScope
) {
    val cur = System.currentTimeMillis()
    ripples.forEach { r ->
        val elapsed = (cur - r.startTime).coerceAtLeast(0)
        val progress = (elapsed / 1200f).coerceIn(0f, 1f)
        val alpha = (1.0f - progress).coerceIn(0f, 1f)
        val radius = 10f + progress * 140f

        scope.drawCircle(
            color = LofiAmber.copy(alpha = alpha * 0.7f),
            radius = radius,
            center = Offset(r.x, r.y),
            style = Stroke(width = 3.0f * (1.0f - progress * 0.5f))
        )
    }
}
