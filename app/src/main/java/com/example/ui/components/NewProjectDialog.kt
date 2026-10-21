package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioType
import com.example.ui.theme.*

@Composable
fun NewProjectDialog(
    onDismiss: () -> Unit,
    onCreateProject: (name: String, aspectRatio: AspectRatioType, fps: Int, durationMs: Long) -> Unit
) {
    var name by remember { mutableStateOf("New Composition") }
    var selectedAspect by remember { mutableStateOf(AspectRatioType.VERTICAL_9_16) }
    var selectedFps by remember { mutableIntStateOf(30) }
    var selectedDurationSeconds by remember { mutableIntStateOf(10) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create New Composition",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Project Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Aspect Ratio Selection
                Text("Aspect Ratio:", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AspectRatioType.values()) { aspect ->
                        val isSelected = aspect == selectedAspect
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAspect = aspect },
                            label = { Text(aspect.label.substringBefore(" "), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonPurple.copy(alpha = 0.3f),
                                selectedLabelColor = NeonPurple,
                                containerColor = StudioSurfaceVariant,
                                labelColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                    }
                }

                // Frame Rate
                Text("Frame Rate:", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(24, 30, 60).forEach { fps ->
                        val isSelected = fps == selectedFps
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFps = fps },
                            label = { Text("$fps FPS", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.3f),
                                selectedLabelColor = NeonCyan,
                                containerColor = StudioSurfaceVariant,
                                labelColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                    }
                }

                // Duration
                Text("Duration (Seconds):", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 15, 30).forEach { dur ->
                        val isSelected = dur == selectedDurationSeconds
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDurationSeconds = dur },
                            label = { Text("${dur}s", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonAmber.copy(alpha = 0.3f),
                                selectedLabelColor = NeonAmber,
                                containerColor = StudioSurfaceVariant,
                                labelColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreateProject(name, selectedAspect, selectedFps, selectedDurationSeconds * 1000L)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
            ) {
                Text("Create Project", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        },
        containerColor = StudioSurface,
        shape = RoundedCornerShape(16.dp)
    )
}
