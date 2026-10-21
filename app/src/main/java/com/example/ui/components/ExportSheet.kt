package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.model.ExportSettings
import com.example.model.Project
import com.example.ui.theme.*

@Composable
fun ExportSheet(
    project: Project,
    settings: ExportSettings,
    exportProgress: Float?,
    isExportComplete: Boolean,
    onUpdateSettings: (ExportSettings) -> Unit,
    onStartExport: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSeconds = project.durationMs / 1000f
    val totalFrames = (totalSeconds * settings.fps).toInt()
    val currentFrame = if (exportProgress != null) (exportProgress * totalFrames).toInt() else 0

    val estimatedSizeMb = run {
        val mbps = when {
            settings.bitrateName.contains("35") -> 35f
            settings.bitrateName.contains("16") -> 16f
            else -> 8f
        }
        (totalSeconds * mbps / 8f).coerceAtLeast(1.2f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .padding(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AFTER EFFECTS RENDER QUEUE",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Export & Encode Studio",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (exportProgress != null) {
            // Live Rendering Queue Box
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioDarkBg),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isExportComplete) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Export Successfully Completed!",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${settings.filename} (${"%.1f".format(estimatedSizeMb)} MB)",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save to Device")
                            }
                            OutlinedButton(onClick = onDismiss) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share", color = Color.White)
                            }
                        }
                    } else {
                        Text(
                            text = "Rendering Frame $currentFrame / $totalFrames",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { exportProgress },
                            color = NeonCyan,
                            trackColor = StudioSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${(exportProgress * 100).toInt()}% Encoded",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Est. Size: ${"%.1f".format(estimatedSizeMb)} MB",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Resolution Selection
            Text("Resolution:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(6.dp))
            val resolutions = listOf("720p HD", "1080p FHD", "2K QHD", "4K Ultra HD")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(resolutions) { res ->
                    val isSelected = res == settings.resolutionName
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(resolutionName = res)) },
                        label = { Text(res, fontSize = 11.sp) },
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

            // Frame Rate Selection
            Text("Frame Rate:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(6.dp))
            val framerates = listOf(24 to "24 FPS (Film)", 30 to "30 FPS (Standard)", 60 to "60 FPS (Fluid)")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(framerates) { (fpsVal, label) ->
                    val isSelected = fpsVal == settings.fps
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(fps = fpsVal)) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonPurple.copy(alpha = 0.25f),
                            selectedLabelColor = NeonPurple,
                            containerColor = StudioSurfaceVariant,
                            labelColor = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bitrate & Quality
            Text("Encoding Bitrate:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(6.dp))
            val bitrates = listOf("Standard (8 Mbps)", "High (16 Mbps)", "Master ProRes (35 Mbps)")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(bitrates) { br ->
                    val isSelected = br == settings.bitrateName
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdateSettings(settings.copy(bitrateName = br)) },
                        label = { Text(br, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonAmber.copy(alpha = 0.25f),
                            selectedLabelColor = NeonAmber,
                            containerColor = StudioSurfaceVariant,
                            labelColor = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Start Export Button
            Button(
                onClick = onStartExport,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_render_now")
            ) {
                Icon(Icons.Default.RocketLaunch, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Render & Export", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
