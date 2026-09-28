package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TimerMode
import com.example.ui.theme.*
import com.example.viewmodel.LofiViewModel
import java.util.Locale

@Composable
fun TimerScreen(
    timerState: LofiViewModel.TimerUiState,
    todayMinutes: Int,
    totalSessions: Int,
    isArabic: Boolean,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSelectMode: (TimerMode) -> Unit,
    onToggleFade: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (timerState.totalSeconds > 0) {
        timerState.remainingSeconds.toFloat() / timerState.totalSeconds.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "timer_progress"
    )

    val minutes = timerState.remainingSeconds / 60
    val seconds = timerState.remainingSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Timer Mode Chips Row
        Text(
            text = if (isArabic) "نوع الجلسة والمؤقت" else "Focus Mode",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LofiTextPrimary,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimerMode.values().forEach { mode ->
                val isSelected = timerState.mode == mode
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectMode(mode) },
                    label = {
                        Text(
                            text = if (isArabic) "${mode.titleAr} (${mode.defaultMinutes} د)" else "${mode.name} (${mode.defaultMinutes}m)",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (mode.isBreak) LofiEmerald else LofiAmber,
                        selectedLabelColor = LofiDarkBg,
                        containerColor = LofiSurface,
                        labelColor = LofiTextPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Color.Transparent else LofiCardBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("mode_${mode.id}")
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Large Circular Dial
        Box(
            modifier = Modifier
                .size(260.dp)
                .testTag("timer_dial"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 16f
                val diameter = size.minDimension - strokeW * 2
                val topLeft = Offset(strokeW, strokeW)

                // Background track
                drawArc(
                    color = LofiSurfaceVariant,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )

                // Active progress arc
                val activeColor = if (timerState.mode.isBreak) LofiEmerald else LofiAmber
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            activeColor,
                            LofiRose,
                            LofiPrimary,
                            activeColor
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }

            // Inner Countdown Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = timeFormatted,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Bold,
                    color = LofiTextPrimary,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isArabic) {
                        if (timerState.mode.isBreak) "وقت الراحة ☕" else "جلسة تركيز ودراسة ✨"
                    } else {
                        if (timerState.mode.isBreak) "Break Time ☕" else "Focus & Study ✨"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (timerState.mode.isBreak) LofiEmerald else LofiAmber,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Controls (Play/Pause & Reset)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            FilledTonalIconButton(
                onClick = onResetTimer,
                modifier = Modifier
                    .size(54.dp)
                    .testTag("reset_timer_button"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = LofiSurfaceVariant,
                    contentColor = LofiTextSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer",
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Play / Pause FAB
            Button(
                onClick = onToggleTimer,
                modifier = Modifier
                    .height(56.dp)
                    .widthIn(min = 160.dp)
                    .testTag("toggle_timer_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (timerState.isRunning) LofiRose else LofiAmber,
                    contentColor = LofiDarkBg
                )
            ) {
                Icon(
                    imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (timerState.isRunning) "Pause" else "Start",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (timerState.isRunning) {
                        if (isArabic) "إيقاف مؤقت" else "Pause"
                    } else {
                        if (isArabic) "بدء الجلسة" else "Start Focus"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Fade on Complete Option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, LofiCardBorder, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = LofiSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.NightlightRound,
                        contentDescription = null,
                        tint = LofiPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isArabic) "تلاشي الصوت تدريجياً" else "Fade Out Audio On Finish",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = LofiTextPrimary
                        )
                        Text(
                            text = if (isArabic) "يخفف الصوت بهدوء في آخر 20 ثانية" else "Gently lowers volume during final 20 seconds",
                            style = MaterialTheme.typography.bodySmall,
                            color = LofiTextMuted
                        )
                    }
                }

                Switch(
                    checked = timerState.fadeOnFinish,
                    onCheckedChange = { onToggleFade() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = LofiPurple,
                        checkedTrackColor = LofiPurple.copy(alpha = 0.35f),
                        uncheckedThumbColor = LofiTextMuted,
                        uncheckedTrackColor = LofiDarkBg
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Focus Statistics Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, LofiCardBorder, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = LofiSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = LofiEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "إحصائيات إنجاز اليوم" else "Today's Study Progress",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = LofiTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$todayMinutes",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = LofiPrimary
                        )
                        Text(
                            text = if (isArabic) "دقيقة تركيز اليوم" else "Minutes Today",
                            style = MaterialTheme.typography.bodySmall,
                            color = LofiTextMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(LofiCardBorder)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalSessions",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = LofiEmerald
                        )
                        Text(
                            text = if (isArabic) "جلسات مكتملة" else "Completed Sessions",
                            style = MaterialTheme.typography.bodySmall,
                            color = LofiTextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}
