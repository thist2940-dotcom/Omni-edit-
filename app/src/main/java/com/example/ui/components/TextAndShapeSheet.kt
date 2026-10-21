package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun TextAndShapeSheet(
    clip: ClipItem,
    onUpdateText: (text: String, fontStyle: String, fontSizeSp: Float, fontColorHex: String, textBgColorHex: String, animation: String) -> Unit,
    onUpdateShape: (shapeType: ShapeType, fillHex: String, strokeHex: String, strokeWidth: Float, cornerRadius: Float) -> Unit,
    onUpdateBlendMode: (BlendModeType) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textValue by remember(clip) { mutableStateOf(clip.text) }
    var fontStyle by remember(clip) { mutableStateOf(clip.fontStyle) }
    var fontSizeSp by remember(clip) { mutableFloatStateOf(clip.fontSizeSp) }
    var fontColorHex by remember(clip) { mutableStateOf(clip.fontColorHex) }

    var selectedShape by remember(clip) { mutableStateOf(clip.shapeType) }
    var shapeFillHex by remember(clip) { mutableStateOf(clip.shapeFillHex) }
    var shapeStrokeHex by remember(clip) { mutableStateOf(clip.shapeStrokeHex) }
    var strokeWidth by remember(clip) { mutableFloatStateOf(clip.shapeStrokeWidthDp) }
    var cornerRadius by remember(clip) { mutableFloatStateOf(clip.shapeCornerRadiusDp) }
    var blendMode by remember(clip) { mutableStateOf(clip.blendMode) }

    val colorSwatches = listOf("#FFFFFF", "#00F0FF", "#FF007F", "#FBBF24", "#10B981", "#8B5CF6", "#000000")

    fun commitText() {
        onUpdateText(textValue, fontStyle, fontSizeSp, fontColorHex, clip.textBgColorHex, clip.textAnimation)
    }

    fun commitShape() {
        onUpdateShape(selectedShape, shapeFillHex, shapeStrokeHex, strokeWidth, cornerRadius)
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
                    text = if (clip.clipType == ClipType.TEXT) "ALIGHT & CAPCUT TYPOGRAPHY" else "ALIGHT VECTOR SHAPES",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (clip.clipType == ClipType.TEXT) "Text Styling & Formatting" else "Vector Geometry & Blending",
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

        if (clip.clipType == ClipType.TEXT) {
            // Text Input Field
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it; commitText() },
                label = { Text("Overlay Text") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = StudioBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Font Size Slider
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Font Size: ${fontSizeSp.toInt()}sp",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier.width(110.dp)
                )
                Slider(
                    value = fontSizeSp,
                    onValueChange = { fontSizeSp = it; commitText() },
                    valueRange = 16f..64f,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Font Color Swatches
            Text("Font Color:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(colorSwatches) { hex ->
                    val color = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.White)
                    val isSelected = hex.equals(fontColorHex, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) NeonCyan else Color.Gray,
                                shape = CircleShape
                            )
                            .clickable { fontColorHex = hex; commitText() }
                    )
                }
            }
        } else {
            // Shape Selection
            Text("Vector Shape:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(ShapeType.values()) { shape ->
                    val isSelected = shape == selectedShape
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedShape = shape; commitShape() },
                        label = { Text(shape.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                            selectedLabelColor = NeonCyan,
                            containerColor = StudioSurfaceVariant,
                            labelColor = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fill Color Swatches
            Text("Shape Fill Color:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(colorSwatches) { hex ->
                    val color = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(NeonPurple)
                    val isSelected = hex.equals(shapeFillHex, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Gray,
                                shape = CircleShape
                            )
                            .clickable { shapeFillHex = hex; commitShape() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stroke Width Slider
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Stroke: ${strokeWidth.toInt()}dp",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier.width(100.dp)
                )
                Slider(
                    value = strokeWidth,
                    onValueChange = { strokeWidth = it; commitShape() },
                    valueRange = 0f..12f,
                    colors = SliderDefaults.colors(thumbColor = NeonPurple, activeTrackColor = NeonPurple),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Alight Motion Layer Blend Modes
        Text("Alight Motion Blend Mode:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(BlendModeType.values()) { mode ->
                val isSelected = mode == blendMode
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        blendMode = mode
                        onUpdateBlendMode(mode)
                    },
                    label = { Text(mode.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonPurple.copy(alpha = 0.25f),
                        selectedLabelColor = NeonPurple,
                        containerColor = StudioSurfaceVariant,
                        labelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}
