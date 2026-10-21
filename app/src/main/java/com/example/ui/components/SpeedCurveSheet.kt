package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun SpeedCurveSheet(
    clip: ClipItem,
    onUpdateSpeed: (speed: Float, preset: SpeedPreset) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var speed by remember(clip) { mutableFloatStateOf(clip.speed) }
    var selectedPreset by remember(clip) { mutableStateOf(clip.speedPreset) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CAPCUT SPEED RAMPING",
                    color = NeonAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Speed Curves & Optical Flow (${"%.1f".format(speed)}x)",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Speed Curve Visual Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(StudioDarkBg)
                .border(1.dp, StudioBorder, RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Baseline
                drawLine(Color.White.copy(alpha = 0.2f), Offset(0f, h / 2f), Offset(w, h / 2f), 1f)

                val path = Path().apply {
                    when (selectedPreset) {
                        SpeedPreset.FLASH_IN -> {
                            moveTo(0f, h * 0.1f)
                            cubicTo(w * 0.2f, h * 0.1f, w * 0.4f, h * 0.8f, w, h * 0.8f)
                        }
                        SpeedPreset.FLASH_OUT -> {
                            moveTo(0f, h * 0.8f)
                            cubicTo(w * 0.6f, h * 0.8f, w * 0.8f, h * 0.1f, w, h * 0.1f)
                        }
                        SpeedPreset.MONTAGE -> {
                            moveTo(0f, h * 0.5f)
                            cubicTo(w * 0.25f, h * 0.1f, w * 0.5f, h * 0.9f, w * 0.75f, h * 0.1f)
                            lineTo(w, h * 0.5f)
                        }
                        SpeedPreset.HERO -> {
                            moveTo(0f, h * 0.2f)
                            cubicTo(w * 0.3f, h * 0.2f, w * 0.4f, h * 0.85f, w * 0.5f, h * 0.85f)
                            cubicTo(w * 0.6f, h * 0.85f, w * 0.7f, h * 0.2f, w, h * 0.2f)
                        }
                        else -> {
                            val normY = (1f - (speed / 5f).coerceIn(0.1f, 1f)) * h
                            moveTo(0f, normY)
                            lineTo(w, normY)
                        }
                    }
                }

                drawPath(path, color = NeonAmber, style = Stroke(width = 3f))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preset Curves Row
        Text("Curve Presets:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(SpeedPreset.values()) { preset ->
                val isSelected = preset == selectedPreset
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedPreset = preset
                        speed = preset.speed
                        onUpdateSpeed(speed, preset)
                    },
                    label = { Text(preset.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonAmber.copy(alpha = 0.25f),
                        selectedLabelColor = NeonAmber,
                        containerColor = StudioSurfaceVariant,
                        labelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Fine Speed Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Manual: ${"%.1f".format(speed)}x",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.width(90.dp)
            )
            Slider(
                value = speed,
                onValueChange = {
                    speed = it
                    selectedPreset = SpeedPreset.NORMAL_1X
                    onUpdateSpeed(speed, selectedPreset)
                },
                valueRange = 0.1f..5.0f,
                colors = SliderDefaults.colors(thumbColor = NeonAmber, activeTrackColor = NeonAmber),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
