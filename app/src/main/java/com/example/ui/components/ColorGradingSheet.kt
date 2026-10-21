package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun ColorGradingSheet(
    clip: ClipItem,
    onUpdateColor: (FilterPresetType, Float, Float, Float, Float, Float) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember(clip) { mutableStateOf(clip.filterPreset) }
    var exposure by remember(clip) { mutableFloatStateOf(clip.exposure) }
    var contrast by remember(clip) { mutableFloatStateOf(clip.contrast) }
    var saturation by remember(clip) { mutableFloatStateOf(clip.saturation) }
    var temperature by remember(clip) { mutableFloatStateOf(clip.temperature) }
    var vignette by remember(clip) { mutableFloatStateOf(clip.vignette) }

    fun commit() {
        onUpdateColor(selectedFilter, exposure, contrast, saturation, temperature, vignette)
    }

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
                    text = "LUMETRI COLOR & FILTERS",
                    color = NeonPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Color Grading & LUT Presets",
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

        // LUT Filters Carousel
        Text("Cinematic LUT Presets:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(FilterPresetType.values()) { filter ->
                val isSelected = filter == selectedFilter
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonPink.copy(alpha = 0.3f) else StudioDarkBg)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) NeonPink else StudioBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            selectedFilter = filter
                            commit()
                        }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter.label,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sliders for Fine Adjustments
        ColorSliderRow("Exposure", "${(exposure * 100).toInt()}%", exposure, -1f..1f, NeonAmber) {
            exposure = it; commit()
        }

        ColorSliderRow("Contrast", "${(contrast * 100).toInt()}%", contrast, 0.5f..2.0f, NeonCyan) {
            contrast = it; commit()
        }

        ColorSliderRow("Saturation", "${(saturation * 100).toInt()}%", saturation, 0f..2.0f, NeonPink) {
            saturation = it; commit()
        }

        ColorSliderRow("Temperature", if (temperature < 0) "Cool" else "Warm", temperature, -1f..1f, NeonPurple) {
            temperature = it; commit()
        }

        ColorSliderRow("Vignette", "${(vignette * 100).toInt()}%", vignette, 0f..1f, Color.White) {
            vignette = it; commit()
        }
    }
}

@Composable
private fun ColorSliderRow(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    accentColor: Color,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: $valueText",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            modifier = Modifier.width(110.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor),
            modifier = Modifier.weight(1f)
        )
    }
}
