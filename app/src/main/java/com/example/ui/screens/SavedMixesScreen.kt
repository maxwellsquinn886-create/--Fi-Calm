package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.SavedMixEntity
import com.example.model.SoundType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SavedMixesScreen(
    savedMixes: List<SavedMixEntity>,
    isArabic: Boolean,
    onApplyMix: (SavedMixEntity) -> Unit,
    onDeleteMix: (SavedMixEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var mixToDelete by remember { mutableStateOf<SavedMixEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = if (isArabic) "مجموعاتك الصوتية المحفوظة" else "Your Custom Sound Mixes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LofiTextPrimary,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )
            Text(
                text = if (isArabic)
                    "المزائج التي قمت بتنسيقها وحفظها لبيئة تركيزك المثالية"
                else
                    "Your personalized soundscapes saved for focused work",
                style = MaterialTheme.typography.bodySmall,
                color = LofiTextMuted,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
        }

        if (savedMixes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .border(1.dp, LofiCardBorder, RoundedCornerShape(22.dp)),
                    colors = CardDefaults.cardColors(containerColor = LofiSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(LofiSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = LofiAmber,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isArabic) "لا توجد مزائج محفوظة بعد" else "No saved mixes yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LofiTextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isArabic)
                                "اذهب لتبويب الأصوات، اضبط مستوى صوت أي مجموعة من أصوات المطر أو البيانو أو النار، واضغط زر 'حفظ المزيج'."
                            else
                                "Go to the Sounds tab, blend any combination of rain, piano, or crackles, and tap 'Save Mix'.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LofiTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(savedMixes, key = { it.id }) { mix ->
                SavedMixCard(
                    mix = mix,
                    isArabic = isArabic,
                    onPlay = { onApplyMix(mix) },
                    onDelete = { mixToDelete = mix }
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (mixToDelete != null) {
        val target = mixToDelete!!
        AlertDialog(
            onDismissRequest = { mixToDelete = null },
            title = {
                Text(
                    text = if (isArabic) "حذف المزيج" else "Delete Mix",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isArabic)
                        "هل أنت متأكد من حذف المزيج '${target.name}'؟"
                    else
                        "Are you sure you want to delete '${target.name}'?",
                    color = LofiTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMix(target)
                        mixToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LofiRose, contentColor = Color.White)
                ) {
                    Text(if (isArabic) "حذف" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { mixToDelete = null }) {
                    Text(if (isArabic) "إلغاء" else "Cancel", color = LofiTextSecondary)
                }
            },
            containerColor = LofiSurface,
            titleContentColor = LofiTextPrimary,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SavedMixCard(
    mix: SavedMixEntity,
    isArabic: Boolean,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    val activeChannels = mutableListOf<String>()
    if (mix.rainVolume > 0.01f) activeChannels.add(if (isArabic) "مطر (${(mix.rainVolume * 100).toInt()}%)" else "Rain")
    if (mix.campfireVolume > 0.01f) activeChannels.add(if (isArabic) "موقد (${(mix.campfireVolume * 100).toInt()}%)" else "Fire")
    if (mix.cricketsVolume > 0.01f) activeChannels.add(if (isArabic) "طبيعة" else "Crickets")
    if (mix.oceanVolume > 0.01f) activeChannels.add(if (isArabic) "بحر" else "Ocean")
    if (mix.windVolume > 0.01f) activeChannels.add(if (isArabic) "رياح" else "Wind")
    if (mix.cafeVolume > 0.01f) activeChannels.add(if (isArabic) "مقهى" else "Cafe")
    if (mix.lofiChordsVolume > 0.01f) activeChannels.add(if (isArabic) "لو-فاي (${(mix.lofiChordsVolume * 100).toInt()}%)" else "Lo-Fi")
    if (mix.vinylVolume > 0.01f) activeChannels.add(if (isArabic) "فينيل" else "Vinyl")
    if (mix.brownNoiseVolume > 0.01f) activeChannels.add(if (isArabic) "ضوضاء بنية" else "Brown")
    if (mix.alphaWavesVolume > 0.01f) activeChannels.add(if (isArabic) "ألفا" else "Alpha")

    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(mix.createdAt))

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mix.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LofiTextPrimary
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = LofiTextMuted,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Mix",
                            tint = LofiTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalButton(
                        onClick = onPlay,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = LofiPrimary,
                            contentColor = LofiDarkBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("apply_mix_${mix.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabic) "تشغيل" else "Play",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (activeChannels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    activeChannels.take(4).forEach { tag ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(text = tag, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = LofiSurfaceVariant,
                                labelColor = LofiTextSecondary
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    if (activeChannels.size > 4) {
                        Text(
                            text = "+${activeChannels.size - 4}",
                            style = MaterialTheme.typography.labelSmall,
                            color = LofiTextMuted,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
    }
}
