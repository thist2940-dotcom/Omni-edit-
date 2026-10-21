package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun EffectsRackSheet(
    clip: ClipItem,
    onAddEffect: (EffectType) -> Unit,
    onToggleEffect: (effectId: String) -> Unit,
    onRemoveEffect: (effectId: String) -> Unit,
    onUpdateParams: (effectId: String, intensity: Float, param1: Float, param2: Float) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

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
                    text = "AFTER EFFECTS PIPELINE",
                    color = NeonPurple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Effects Rack & Modifiers (${clip.effects.size})",
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

        // Add Effect Button
        Button(
            onClick = { showAddDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .testTag("btn_add_effect_rack")
        ) {
            Icon(Icons.Default.AddCircleOutline, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Effect to Rack", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Effects List
        if (clip.effects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioDarkBg)
                    .border(1.dp, StudioBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No effects on this layer.\nTap '+ Add Effect' to apply After Effects FX.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
            ) {
                items(clip.effects) { effect ->
                    EffectItemCard(
                        effect = effect,
                        onToggle = { onToggleEffect(effect.id) },
                        onRemove = { onRemoveEffect(effect.id) },
                        onUpdateParams = { intensity, p1, p2 ->
                            onUpdateParams(effect.id, intensity, p1, p2)
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Choose After Effects FX", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(EffectType.values()) { type ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StudioSurfaceVariant,
                            onClick = {
                                onAddEffect(type)
                                showAddDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(type.label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(type.category, color = NeonCyan, fontSize = 11.sp)
                                }
                                Icon(Icons.Default.Add, contentDescription = null, tint = NeonPurple)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            },
            containerColor = StudioSurface
        )
    }
}

@Composable
private fun EffectItemCard(
    effect: EffectInstance,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
    onUpdateParams: (intensity: Float, p1: Float, p2: Float) -> Unit
) {
    var intensity by remember(effect) { mutableFloatStateOf(effect.intensity) }
    var param1 by remember(effect) { mutableFloatStateOf(effect.param1) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (effect.isEnabled) StudioDarkBg else StudioDarkBg.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (effect.isEnabled) NeonPurple.copy(alpha = 0.5f) else StudioBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggle, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (effect.isEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Effect",
                            tint = if (effect.isEnabled) NeonCyan else Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = effect.type.label,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove Effect",
                        tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Intensity Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Amount: ${(intensity * 100).toInt()}%",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.width(90.dp)
                )
                Slider(
                    value = intensity,
                    onValueChange = {
                        intensity = it
                        onUpdateParams(intensity, param1, effect.param2)
                    },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = NeonPurple, activeTrackColor = NeonPurple),
                    modifier = Modifier.weight(1f)
                )
            }

            // Frequency / Parameter 1 Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Frequency: ${(param1 * 100).toInt()}%",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.width(90.dp)
                )
                Slider(
                    value = param1,
                    onValueChange = {
                        param1 = it
                        onUpdateParams(intensity, param1, effect.param2)
                    },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
