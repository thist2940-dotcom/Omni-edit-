package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun TransitionSheet(
    clip: ClipItem,
    onUpdateTransitions: (transIn: TransitionType, transOut: TransitionType, durationMs: Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var transIn by remember(clip) { mutableStateOf(clip.transitionIn) }
    var transOut by remember(clip) { mutableStateOf(clip.transitionOut) }
    var durationMs by remember(clip) { mutableLongStateOf(clip.transitionDurationMs) }

    fun commit() {
        onUpdateTransitions(transIn, transOut, durationMs)
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
                    text = "CAPCUT TRANSITIONS ENGINE",
                    color = NeonPurple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Seamless Clip Transitions",
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

        // Transition Duration
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Duration: ${durationMs}ms",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.width(110.dp)
            )
            Slider(
                value = durationMs.toFloat(),
                onValueChange = { durationMs = it.toLong(); commit() },
                valueRange = 200f..2000f,
                colors = SliderDefaults.colors(thumbColor = NeonPurple, activeTrackColor = NeonPurple),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // In-Transition Presets
        Text("Transition IN (Clip Entrance):", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(TransitionType.values()) { trans ->
                val isSelected = trans == transIn
                FilterChip(
                    selected = isSelected,
                    onClick = { transIn = trans; commit() },
                    label = { Text(trans.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonPurple.copy(alpha = 0.25f),
                        selectedLabelColor = NeonPurple,
                        containerColor = StudioSurfaceVariant,
                        labelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Out-Transition Presets
        Text("Transition OUT (Clip Exit):", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(TransitionType.values()) { trans ->
                val isSelected = trans == transOut
                FilterChip(
                    selected = isSelected,
                    onClick = { transOut = trans; commit() },
                    label = { Text(trans.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                        selectedLabelColor = NeonCyan,
                        containerColor = StudioSurfaceVariant,
                        labelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}
