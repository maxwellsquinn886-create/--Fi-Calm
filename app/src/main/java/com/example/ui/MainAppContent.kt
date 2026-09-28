package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.SceneType
import com.example.ui.components.MiniAnimatedEqualizer
import com.example.ui.components.ZenOverlay
import com.example.ui.screens.MixerScreen
import com.example.ui.screens.SavedMixesScreen
import com.example.ui.screens.ScenesScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.theme.*
import com.example.viewmodel.LofiViewModel

enum class AppTab(
    val titleAr: String,
    val titleEn: String
) {
    SOUNDS("الأصوات", "Sounds"),
    TIMER("المؤقت", "Timer"),
    SCENES("المشاهد", "Scenes"),
    SAVED("مجموعاتي", "Saved")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: LofiViewModel) {
    val channels by viewModel.channels.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val masterVolume by viewModel.masterVolume.collectAsStateWithLifecycle()
    val amplitude by viewModel.amplitude.collectAsStateWithLifecycle()
    val activePresetId by viewModel.activePresetId.collectAsStateWithLifecycle()
    val activeScene by viewModel.activeScene.collectAsStateWithLifecycle()
    val isZenMode by viewModel.isZenMode.collectAsStateWithLifecycle()
    val isArabic by viewModel.isArabic.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val savedMixes by viewModel.savedMixes.collectAsStateWithLifecycle()
    val todayMinutes by viewModel.todayFocusMinutes.collectAsStateWithLifecycle()
    val totalSessions by viewModel.totalSessionsCount.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(AppTab.SOUNDS) }

    if (isZenMode) {
        ZenOverlay(
            sceneType = activeScene,
            isPlaying = isPlaying,
            amplitude = amplitude,
            masterVolume = masterVolume,
            timerState = timerState,
            channels = channels,
            isArabic = isArabic,
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onMasterVolumeChange = { viewModel.setMasterVolume(it) },
            onExitZen = { viewModel.setZenMode(false) }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = LofiDarkBg,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(LofiPrimary, LofiPurple))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Headphones,
                                    contentDescription = null,
                                    tint = LofiDarkBg,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "Lo-Fi Calm" else "Lo-Fi Calm",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = LofiTextPrimary
                                )
                                Text(
                                    text = if (isArabic) "أصوات الاسترخاء والدراسة" else "Relax & Study Ambient",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LofiTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    },
                    actions = {
                        // Language switcher button
                        TextButton(
                            onClick = { viewModel.toggleLanguage() },
                            colors = ButtonDefaults.textButtonColors(contentColor = LofiPrimary),
                            modifier = Modifier.testTag("lang_toggle")
                        ) {
                            Text(
                                text = if (isArabic) "EN" else "عربي",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Fullscreen Zen mode quick button
                        IconButton(
                            onClick = { viewModel.setZenMode(true) },
                            modifier = Modifier.testTag("top_zen_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Zen Mode",
                                tint = LofiAmber
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = LofiDarkBg,
                        titleContentColor = LofiTextPrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = LofiSurface,
                    contentColor = LofiTextSecondary,
                    tonalElevation = 8.dp
                ) {
                    AppTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        AppTab.SOUNDS -> if (isSelected) Icons.Filled.Tune else Icons.Outlined.Tune
                                        AppTab.TIMER -> if (isSelected) Icons.Filled.HourglassTop else Icons.Outlined.HourglassTop
                                        AppTab.SCENES -> if (isSelected) Icons.Filled.Palette else Icons.Outlined.Palette
                                        AppTab.SAVED -> if (isSelected) Icons.Filled.Bookmarks else Icons.Outlined.Bookmarks
                                    },
                                    contentDescription = if (isArabic) tab.titleAr else tab.titleEn
                                )
                            },
                            label = {
                                Text(
                                    text = if (isArabic) tab.titleAr else tab.titleEn,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = LofiDarkBg,
                                selectedTextColor = LofiPrimary,
                                indicatorColor = LofiPrimary,
                                unselectedIconColor = LofiTextMuted,
                                unselectedTextColor = LofiTextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            },
            floatingActionButton = {
                // Persistent Play/Pause FAB
                ExtendedFloatingActionButton(
                    onClick = { viewModel.togglePlayPause() },
                    icon = {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isPlaying) {
                                    if (isArabic) "إيقاف الأصوات" else "Pause"
                                } else {
                                    if (isArabic) "تشغيل الأصوات" else "Play All"
                                },
                                fontWeight = FontWeight.Bold
                            )
                            if (isPlaying) {
                                Spacer(modifier = Modifier.width(6.dp))
                                MiniAnimatedEqualizer(accentColor = LofiDarkBg)
                            }
                        }
                    },
                    containerColor = if (isPlaying) LofiRose else LofiPrimary,
                    contentColor = LofiDarkBg,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .testTag("master_fab")
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(
                    targetState = selectedTab,
                    animationSpec = tween(280),
                    label = "tab_crossfade"
                ) { tab ->
                    when (tab) {
                        AppTab.SOUNDS -> {
                            MixerScreen(
                                channels = channels,
                                isMasterPlaying = isPlaying,
                                masterVolume = masterVolume,
                                activePresetId = activePresetId,
                                isArabic = isArabic,
                                onTogglePlayPause = { viewModel.togglePlayPause() },
                                onMasterVolumeChange = { viewModel.setMasterVolume(it) },
                                onToggleChannel = { viewModel.toggleChannel(it) },
                                onChannelVolumeChange = { type, vol -> viewModel.setChannelVolume(type, vol) },
                                onApplyPreset = { viewModel.applyPreset(it) },
                                onSaveCurrentMix = { viewModel.saveCurrentMix(it) }
                            )
                        }
                        AppTab.TIMER -> {
                            TimerScreen(
                                timerState = timerState,
                                todayMinutes = todayMinutes,
                                totalSessions = totalSessions,
                                isArabic = isArabic,
                                onToggleTimer = { viewModel.toggleTimer() },
                                onResetTimer = { viewModel.resetTimer() },
                                onSelectMode = { viewModel.setTimerMode(it) },
                                onToggleFade = { viewModel.toggleFadeOnFinish() }
                            )
                        }
                        AppTab.SCENES -> {
                            ScenesScreen(
                                activeScene = activeScene,
                                isPlaying = isPlaying,
                                amplitude = amplitude,
                                isArabic = isArabic,
                                onSelectScene = { viewModel.selectScene(it) },
                                onEnterZenMode = { viewModel.setZenMode(true) }
                            )
                        }
                        AppTab.SAVED -> {
                            SavedMixesScreen(
                                savedMixes = savedMixes,
                                isArabic = isArabic,
                                onApplyMix = { viewModel.applySavedMix(it) },
                                onDeleteMix = { viewModel.deleteSavedMix(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
