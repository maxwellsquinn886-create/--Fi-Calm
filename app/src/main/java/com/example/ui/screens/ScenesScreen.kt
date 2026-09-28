package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SceneType
import com.example.ui.components.AnimatedVisualizer
import com.example.ui.theme.*

@Composable
fun ScenesScreen(
    activeScene: SceneType,
    isPlaying: Boolean,
    amplitude: Float,
    isArabic: Boolean,
    onSelectScene: (SceneType) -> Unit,
    onEnterZenMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Live Visualizer Preview Box
        AnimatedVisualizer(
            sceneType = activeScene,
            isPlaying = isPlaying,
            amplitude = amplitude,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .testTag("interactive_visualizer_preview")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Interaction hint
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                tint = LofiAmber,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isArabic)
                    "المس المشهد بالأعلى لإحداث تموجات ضوئية تفاعلية ✨"
                else
                    "Tap anywhere on the visualizer for glowing ripple effects ✨",
                style = MaterialTheme.typography.bodySmall,
                color = LofiTextMuted,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Enter Zen Mode Fullscreen Action
        Button(
            onClick = onEnterZenMode,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("enter_zen_mode_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = LofiPrimary,
                contentColor = LofiDarkBg
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = "Zen Mode",
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isArabic) "وضع زن التام (شاشة كاملة للمكتب)" else "Enter Fullscreen Zen Desk Mode",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Scene Selector Title
        Text(
            text = if (isArabic) "اختر المشهد البصري المتحرك" else "Choose Visual Theme",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LofiTextPrimary,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 12.dp)
        )

        // List of 4 Scenes
        SceneType.values().forEach { scene ->
            val isSelected = activeScene == scene
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) LofiPrimary else LofiCardBorder,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { onSelectScene(scene) }
                    .testTag("scene_option_${scene.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) LofiSurfaceVariant else LofiSurface
                )
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
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, if (isSelected) LofiPrimary else LofiCardBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (scene) {
                                    SceneType.COZY_ROOM -> Icons.Default.Weekend
                                    SceneType.VINYL_RECORD -> Icons.Default.Album
                                    SceneType.CAMPFIRE_NIGHT -> Icons.Default.LocalFireDepartment
                                    SceneType.ZEN_MINIMAL -> Icons.Default.Spa
                                },
                                contentDescription = null,
                                tint = if (isSelected) LofiPrimary else LofiTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = if (isArabic) scene.titleAr else scene.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) LofiTextPrimary else LofiTextSecondary
                            )
                            Text(
                                text = when (scene) {
                                    SceneType.COZY_ROOM -> if (isArabic) "نافذة المطر والمصباح الدافئ وبخار القهوة" else "Warm lamp, window rain, steaming coffee"
                                    SceneType.VINYL_RECORD -> if (isArabic) "أسطوانة فينيل كلاسيكية دوارة مع إبرة التشغيل" else "Rotating vintage vinyl disc & tonearm"
                                    SceneType.CAMPFIRE_NIGHT -> if (isArabic) "شرارات النار وضوء القمر والنجوم المتلألئة" else "Glowing fire sparks, moon & night stars"
                                    SceneType.ZEN_MINIMAL -> if (isArabic) "تموجات ترددية نقية وهادئة للتأمل" else "Pure pulsating aura waves for meditation"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = LofiTextMuted
                            )
                        }
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectScene(scene) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = LofiPrimary,
                            unselectedColor = LofiCardBorder
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}
