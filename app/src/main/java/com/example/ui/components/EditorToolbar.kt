package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ActiveSheet

@Composable
fun EditorToolbar(
    hasSelectedClip: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSplit: () -> Unit,
    onOpenSheet: (ActiveSheet) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioDarkBg)
    ) {
        // Upper Quick Bar: Undo, Redo, Add Layer, and Quick Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier.size(36.dp).testTag("btn_undo")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier.size(36.dp).testTag("btn_redo")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Add Layer Button
            Button(
                onClick = { onOpenSheet(ActiveSheet.ADD_LAYER) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonPurple,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp).testTag("btn_add_layer")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Layer", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Primary Editing Tools Bar (Horizontal Scrollable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarToolItem(
                label = "Split",
                icon = Icons.Default.ContentCut,
                color = NeonCyan,
                enabled = true,
                onClick = onSplit,
                testTag = "tool_split"
            )

            ToolbarToolItem(
                label = "Curves",
                icon = Icons.Default.Timeline,
                color = KeyframeDiamond,
                enabled = hasSelectedClip,
                onClick = { onOpenSheet(ActiveSheet.KEYFRAME_GRAPH) },
                testTag = "tool_curves"
            )

            ToolbarToolItem(
                label = "Effects",
                icon = Icons.Default.AutoFixHigh,
                color = NeonPurple,
                enabled = hasSelectedClip,
                onClick = { onOpenSheet(ActiveSheet.EFFECTS_RACK) },
                testTag = "tool_effects"
            )

            ToolbarToolItem(
                label = "Speed",
                icon = Icons.Default.Speed,
                color = NeonAmber,
                enabled = hasSelectedClip,
                onClick = { onOpenSheet(ActiveSheet.SPEED_RAMP) },
                testTag = "tool_speed"
            )

            ToolbarToolItem(
                label = "Audio",
                icon = Icons.Default.VolumeUp,
                color = NeonGreen,
                enabled = hasSelectedClip,
                onClick = { onOpenSheet(ActiveSheet.AUDIO_FX) },
                testTag = "tool_audio"
            )

            ToolbarToolItem(
                label = "Colors",
                icon = Icons.Default.Palette,
                color = NeonPink,
                enabled = hasSelectedClip,
                onClick = { onOpenSheet(ActiveSheet.COLOR_GRADE) },
                testTag = "tool_colors"
            )

            ToolbarToolItem(
                label = "Text",
                icon = Icons.Default.TextFields,
                color = NeonCyan,
                enabled = hasSelectedClip,
                onClick = { onOpenSheet(ActiveSheet.TEXT_STYLE) },
                testTag = "tool_text"
            )

            ToolbarToolItem(
                label = "Shapes",
                icon = Icons.Default.Category,
                color = NeonAmber,
                enabled = hasSelectedClip,
                onClick = { onOpenSheet(ActiveSheet.SHAPE_STYLE) },
                testTag = "tool_shapes"
            )

            ToolbarToolItem(
                label = "Transitions",
                icon = Icons.Default.FlashOn,
                color = NeonPurple,
                enabled = hasSelectedClip,
                onClick = { onOpenSheet(ActiveSheet.TRANSITIONS) },
                testTag = "tool_transitions"
            )

            ToolbarToolItem(
                label = "Duplicate",
                icon = Icons.Default.ContentCopy,
                color = Color.White.copy(alpha = 0.8f),
                enabled = hasSelectedClip,
                onClick = onDuplicate,
                testTag = "tool_duplicate"
            )

            ToolbarToolItem(
                label = "Delete",
                icon = Icons.Default.Delete,
                color = Color(0xFFEF4444),
                enabled = hasSelectedClip,
                onClick = onDelete,
                testTag = "tool_delete"
            )
        }
    }
}

@Composable
private fun ToolbarToolItem(
    label: String,
    icon: ImageVector,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(
                    if (enabled) color.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.05f),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) color else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (enabled) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.3f)
        )
    }
}
