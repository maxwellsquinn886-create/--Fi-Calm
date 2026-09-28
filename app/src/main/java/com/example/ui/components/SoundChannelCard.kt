package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SoundChannelState
import com.example.model.SoundType
import com.example.ui.theme.*
import kotlin.math.sin

@Composable
fun SoundChannelCard(
    state: SoundChannelState,
    isArabic: Boolean,
    isMasterPlaying: Boolean,
    onToggle: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val sound = state.type
    val isActive = state.isEnabled && isMasterPlaying

    val borderColor by animateColorAsState(
        targetValue = if (state.isEnabled) sound.accentColor.copy(alpha = 0.6f) else LofiCardBorder,
        label = "card_border"
    )

    val cardBg by animateColorAsState(
        targetValue = if (state.isEnabled) LofiSurfaceVariant else LofiSurface,
        label = "card_bg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .testTag("sound_card_${sound.id}"),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon Badge + Title & Description
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onToggle() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (state.isEnabled) sound.accentColor.copy(alpha = 0.25f)
                                else LofiDarkBg
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getSoundIcon(sound),
                            contentDescription = if (isArabic) sound.nameAr else sound.nameEn,
                            tint = if (state.isEnabled) sound.accentColor else LofiTextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isArabic) sound.nameAr else sound.nameEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (state.isEnabled) LofiTextPrimary else LofiTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (isActive) {
                                Spacer(modifier = Modifier.width(8.dp))
                                MiniAnimatedEqualizer(accentColor = sound.accentColor)
                            }
                        }

                        Text(
                            text = if (isArabic) sound.descriptionAr else sound.nameEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = LofiTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Switch
                Switch(
                    checked = state.isEnabled,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.testTag("switch_${sound.id}"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = sound.accentColor,
                        checkedTrackColor = sound.accentColor.copy(alpha = 0.35f),
                        uncheckedThumbColor = LofiTextMuted,
                        uncheckedTrackColor = LofiDarkBg
                    )
                )
            }

            // Volume Slider (only enabled or prominent when active)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (state.volume > 0.05f) Icons.Default.VolumeDown else Icons.Default.VolumeMute,
                    contentDescription = "Volume Icon",
                    tint = if (state.isEnabled) sound.accentColor.copy(alpha = 0.8f) else LofiTextMuted,
                    modifier = Modifier.size(18.dp)
                )

                Slider(
                    value = state.volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1f,
                    enabled = state.isEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("slider_${sound.id}"),
                    colors = SliderDefaults.colors(
                        thumbColor = if (state.isEnabled) sound.accentColor else LofiTextMuted,
                        activeTrackColor = if (state.isEnabled) sound.accentColor else LofiTextMuted.copy(alpha = 0.3f),
                        inactiveTrackColor = LofiDarkBg
                    )
                )

                Text(
                    text = "${(state.volume * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (state.isEnabled) LofiTextSecondary else LofiTextMuted,
                    modifier = Modifier.width(36.dp),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun MiniAnimatedEqualizer(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_transition")

    val h1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(460, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )

    Row(
        modifier = modifier.height(16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h1.dp)
                .background(accentColor, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h2.dp)
                .background(accentColor, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h3.dp)
                .background(accentColor, RoundedCornerShape(1.dp))
        )
    }
}

fun getSoundIcon(type: SoundType): ImageVector {
    return when (type) {
        SoundType.RAIN -> Icons.Default.WaterDrop
        SoundType.CAMPFIRE -> Icons.Default.LocalFireDepartment
        SoundType.CRICKETS -> Icons.Default.Nightlight
        SoundType.OCEAN -> Icons.Default.Waves
        SoundType.WIND -> Icons.Default.Air
        SoundType.CAFE -> Icons.Default.Coffee
        SoundType.LOFI_CHORDS -> Icons.Default.MusicNote
        SoundType.VINYL -> Icons.Default.Album
        SoundType.BROWN_NOISE -> Icons.Default.GraphicEq
        SoundType.ALPHA_WAVES -> Icons.Default.Psychology
    }
}
