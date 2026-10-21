package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AudioSimulator
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun AudioSheet(
    clip: ClipItem,
    onUpdateAudio: (volume: Float, fadeIn: Long, fadeOut: Long, pitchPreset: VoicePitchPreset) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var volume by remember(clip) { mutableFloatStateOf(clip.volume) }
    var fadeInMs by remember(clip) { mutableLongStateOf(clip.fadeInMs) }
    var fadeOutMs by remember(clip) { mutableLongStateOf(clip.fadeOutMs) }
    var pitchPreset by remember(clip) { mutableStateOf(clip.pitchPreset) }

    fun commit() {
        onUpdateAudio(volume, fadeInMs, fadeOutMs, pitchPreset)
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
                    text = "CAPCUT AUDIO STUDIO",
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Audio Mastering & Voice FX",
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

        // Volume Slider
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Volume: ${(volume * 100).toInt()}%",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.width(100.dp)
            )
            Slider(
                value = volume,
                onValueChange = { volume = it; commit() },
                valueRange = 0f..2f,
                colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen),
                modifier = Modifier.weight(1f)
            )
        }

        // Fade In Slider
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Fade In: ${fadeInMs}ms",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.width(100.dp)
            )
            Slider(
                value = fadeInMs.toFloat(),
                onValueChange = { fadeInMs = it.toLong(); commit() },
                valueRange = 0f..3000f,
                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan),
                modifier = Modifier.weight(1f)
            )
        }

        // Fade Out Slider
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Fade Out: ${fadeOutMs}ms",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.width(100.dp)
            )
            Slider(
                value = fadeOutMs.toFloat(),
                onValueChange = { fadeOutMs = it.toLong(); commit() },
                valueRange = 0f..3000f,
                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Voice Changer Presets
        Text("Voice Changer Presets:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(VoicePitchPreset.values()) { preset ->
                val isSelected = preset == pitchPreset
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        pitchPreset = preset
                        AudioSimulator.playSfx("beat")
                        commit()
                    },
                    label = { Text(preset.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonGreen.copy(alpha = 0.25f),
                        selectedLabelColor = NeonGreen,
                        containerColor = StudioSurfaceVariant,
                        labelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Instant Sound Effects Trigger Pad
        Text("Instant Sound FX Pad:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(6.dp))

        val sfxList = listOf(
            "whoosh" to "Cinematic Swoosh",
            "camera" to "Camera Click",
            "glitch" to "Digital Glitch",
            "beat" to "Sub Bass Drop",
            "split" to "Snappy Pop"
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sfxList) { (key, label) ->
                FilledTonalButton(
                    onClick = { AudioSimulator.playSfx(key) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = StudioSurfaceVariant,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp), tint = NeonGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(label, fontSize = 11.sp)
                }
            }
        }
    }
}
