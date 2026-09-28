package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SceneType
import com.example.model.SoundChannelState
import com.example.model.SoundType
import com.example.ui.theme.*
import com.example.viewmodel.LofiViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ZenOverlay(
    sceneType: SceneType,
    isPlaying: Boolean,
    amplitude: Float,
    masterVolume: Float,
    timerState: LofiViewModel.TimerUiState,
    channels: Map<SoundType, SoundChannelState>,
    isArabic: Boolean,
    onTogglePlayPause: () -> Unit,
    onMasterVolumeChange: (Float) -> Unit,
    onExitZen: () -> Unit
) {
    BackHandler {
        onExitZen()
    }

    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        while (true) {
            currentTimeString = format.format(Date())
            delay(1000L)
        }
    }

    val activeSoundNames = channels.values
        .filter { it.isEnabled }
        .map { if (isArabic) it.type.nameAr else it.type.nameEn }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LofiDarkBg)
    ) {
        // 1. Fullscreen Visualizer
        AnimatedVisualizer(
            sceneType = sceneType,
            isPlaying = isPlaying,
            amplitude = amplitude,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Translucent Ambient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x990F111A),
                            Color(0x330F111A),
                            Color(0xCC0F111A)
                        )
                    )
                )
        )

        // 3. Top Header: Exit Button + Tag
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) LofiEmerald else LofiRose)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isArabic) "وضع زن للدراسة" else "Zen Desk Mode",
                    style = MaterialTheme.typography.titleSmall,
                    color = LofiTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onExitZen,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(LofiSurface.copy(alpha = 0.8f))
                    .testTag("exit_zen_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FullscreenExit,
                    contentDescription = "Exit Zen Mode",
                    tint = LofiTextPrimary
                )
            }
        }

        // 4. Center Digital Clock & Focus Timer
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTimeString,
                fontSize = 58.sp,
                fontWeight = FontWeight.Light,
                color = LofiTextPrimary,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 2.sp
            )

            if (timerState.isRunning || timerState.remainingSeconds < timerState.totalSeconds) {
                Spacer(modifier = Modifier.height(12.dp))
                val min = timerState.remainingSeconds / 60
                val sec = timerState.remainingSeconds % 60
                val timeFormatted = String.format(Locale.US, "%02d:%02d", min, sec)

                Surface(
                    color = LofiSurface.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LofiAmber.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassBottom,
                            contentDescription = null,
                            tint = LofiAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = timeFormatted,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LofiAmber,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) timerState.mode.titleAr else timerState.mode.name,
                            fontSize = 13.sp,
                            color = LofiTextSecondary
                        )
                    }
                }
            }

            if (activeSoundNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = activeSoundNames.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = LofiTextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 2
                )
            }
        }

        // 5. Bottom Minimal Control Bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 28.dp, start = 24.dp, end = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(LofiSurface.copy(alpha = 0.92f))
                .border(1.dp, LofiCardBorder, RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilledIconButton(
                onClick = onTogglePlayPause,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isPlaying) LofiRose else LofiPrimary,
                    contentColor = LofiDarkBg
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(24.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.VolumeDown,
                contentDescription = null,
                tint = LofiTextSecondary,
                modifier = Modifier.size(20.dp)
            )

            Slider(
                value = masterVolume,
                onValueChange = onMasterVolumeChange,
                valueRange = 0f..1f,
                modifier = Modifier.width(140.dp),
                colors = SliderDefaults.colors(
                    thumbColor = LofiPrimary,
                    activeTrackColor = LofiPrimary,
                    inactiveTrackColor = LofiDarkBg
                )
            )
        }
    }
}
