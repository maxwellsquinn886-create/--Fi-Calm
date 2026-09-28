package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CURATED_PRESETS
import com.example.model.PresetMix
import com.example.model.SoundChannelState
import com.example.model.SoundType
import com.example.ui.components.SoundChannelCard
import com.example.ui.theme.*

@Composable
fun MixerScreen(
    channels: Map<SoundType, SoundChannelState>,
    isMasterPlaying: Boolean,
    masterVolume: Float,
    activePresetId: String?,
    isArabic: Boolean,
    onTogglePlayPause: () -> Unit,
    onMasterVolumeChange: (Float) -> Unit,
    onToggleChannel: (SoundType) -> Unit,
    onChannelVolumeChange: (SoundType, Float) -> Unit,
    onApplyPreset: (PresetMix) -> Unit,
    onSaveCurrentMix: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var mixNameInput by remember { mutableStateOf("") }

    val activeCount = channels.values.count { it.isEnabled }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Master Control Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, LofiCardBorder, RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = LofiSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "مستوى الصوت العام" else "Master Volume",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = LofiTextPrimary
                            )
                            Text(
                                text = if (isArabic) "$activeCount أصوات نشطة حالياً" else "$activeCount active sound tracks",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (activeCount > 0) LofiEmerald else LofiTextMuted
                            )
                        }

                        // Save current mix button
                        FilledTonalButton(
                            onClick = {
                                mixNameInput = ""
                                showSaveDialog = true
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = LofiSurfaceVariant,
                                contentColor = LofiAmber
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("save_mix_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = "Save Mix",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "حفظ المزيج" else "Save Mix",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (masterVolume > 0.05f) onMasterVolumeChange(0f) else onMasterVolumeChange(0.85f)
                            }
                        ) {
                            Icon(
                                imageVector = if (masterVolume > 0.05f) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Master Volume Toggle",
                                tint = LofiPrimary
                            )
                        }

                        Slider(
                            value = masterVolume,
                            onValueChange = onMasterVolumeChange,
                            valueRange = 0f..1f,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("master_volume_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = LofiPrimary,
                                activeTrackColor = LofiPrimary,
                                inactiveTrackColor = LofiDarkBg
                            )
                        )

                        Text(
                            text = "${(masterVolume * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = LofiTextSecondary,
                            modifier = Modifier.width(38.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 2. Curated Presets Header & Horizontal Row
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isArabic) "مجموعات جاهزة للدراسة والعمل" else "Curated Focus Presets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LofiTextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CURATED_PRESETS.forEach { preset ->
                        val isSelected = activePresetId == preset.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { onApplyPreset(preset) },
                            label = {
                                Text(
                                    text = if (isArabic) preset.titleAr else preset.titleEn,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (preset.id) {
                                        "cozy_study" -> Icons.Default.MenuBook
                                        "midnight_cafe" -> Icons.Default.Coffee
                                        "deep_focus" -> Icons.Default.Psychology
                                        "campfire_night" -> Icons.Default.LocalFireDepartment
                                        "ocean_calm" -> Icons.Default.Waves
                                        else -> Icons.Default.AutoStories
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isSelected) LofiDarkBg else LofiPrimary
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LofiPrimary,
                                selectedLabelColor = LofiDarkBg,
                                containerColor = LofiSurface,
                                labelColor = LofiTextPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) LofiPrimary else LofiCardBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("preset_${preset.id}")
                        )
                    }
                }
            }
        }

        // 3. Sound Channels Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isArabic) "مكتبة الأصوات المحيطية" else "Ambient Sound Channels",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LofiTextPrimary
                )
                Text(
                    text = if (isArabic) "10 أصوات قابلة للدمج" else "10 blendable tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = LofiTextMuted
                )
            }
        }

        // 4. Sound Channels List
        items(SoundType.values().toList(), key = { it.id }) { soundType ->
            val channelState = channels[soundType] ?: SoundChannelState(type = soundType)
            SoundChannelCard(
                state = channelState,
                isArabic = isArabic,
                isMasterPlaying = isMasterPlaying,
                onToggle = { onToggleChannel(soundType) },
                onVolumeChange = { vol -> onChannelVolumeChange(soundType, vol) }
            )
        }
    }

    // Save Mix Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = if (isArabic) "حفظ المزيج الصوتي" else "Save Custom Mix",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isArabic)
                            "أدخل اسماً لهذا المزيج للرجوع إليه في أي وقت أثناء جلسات المذاكرة:"
                        else
                            "Enter a name for this ambient blend to quickly recall during study:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LofiTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = mixNameInput,
                        onValueChange = { mixNameInput = it },
                        placeholder = {
                            Text(if (isArabic) "مثال: مكس المذاكرة الليلية" else "e.g. Night Session")
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mix_name_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LofiPrimary,
                            unfocusedBorderColor = LofiCardBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (mixNameInput.isNotBlank()) {
                            onSaveCurrentMix(mixNameInput.trim())
                            showSaveDialog = false
                        }
                    },
                    enabled = mixNameInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = LofiPrimary, contentColor = LofiDarkBg),
                    modifier = Modifier.testTag("confirm_save_mix")
                ) {
                    Text(if (isArabic) "حفظ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel", color = LofiTextSecondary)
                }
            },
            containerColor = LofiSurface,
            titleContentColor = LofiTextPrimary,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
