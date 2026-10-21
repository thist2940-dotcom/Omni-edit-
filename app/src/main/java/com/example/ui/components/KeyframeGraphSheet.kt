package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MotionEngine
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.abs

@Composable
fun KeyframeGraphSheet(
    clip: ClipItem,
    activeProperty: KeyframeProperty,
    currentTimeMs: Long,
    onSelectProperty: (KeyframeProperty) -> Unit,
    onUpdateBezier: (keyframeId: String, easing: EasingCurveType, cp1x: Float, cp1y: Float, cp2x: Float, cp2y: Float) -> Unit,
    onDeleteKeyframe: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val propertyKeyframes = remember(clip, activeProperty) {
        clip.keyframes.filter { it.property == activeProperty }.sortedBy { it.timeMs }
    }

    val activeKeyframe = remember(propertyKeyframes, currentTimeMs) {
        propertyKeyframes.find { abs(it.timeMs - currentTimeMs) < 300L } ?: propertyKeyframes.firstOrNull()
    }

    var cp1x by remember(activeKeyframe) { mutableFloatStateOf(activeKeyframe?.cp1x ?: 0.42f) }
    var cp1y by remember(activeKeyframe) { mutableFloatStateOf(activeKeyframe?.cp1y ?: 0.0f) }
    var cp2x by remember(activeKeyframe) { mutableFloatStateOf(activeKeyframe?.cp2x ?: 0.58f) }
    var cp2y by remember(activeKeyframe) { mutableFloatStateOf(activeKeyframe?.cp2y ?: 1.0f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .padding(16.dp)
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ALIGHT MOTION GRAPH EDITOR",
                    color = KeyframeDiamond,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Bézier Curve & Easing Interpolator",
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

        // Property Tabs (Position X, Position Y, Scale, Rotation, Opacity)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(KeyframeProperty.values()) { prop ->
                val isSelected = prop == activeProperty
                val kfCount = clip.keyframes.count { it.property == prop }

                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectProperty(prop) },
                    label = {
                        Text(
                            text = "${prop.label} ($kfCount)",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = KeyframeDiamond.copy(alpha = 0.25f),
                        selectedLabelColor = KeyframeDiamond,
                        containerColor = StudioSurfaceVariant,
                        labelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Graph Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(StudioDarkBg)
                .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("bezier_canvas")
                    .pointerInput(activeKeyframe) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val pos = change.position
                            val normX = (pos.x / size.width).coerceIn(0f, 1f)
                            val normY = 1f - (pos.y / size.height).coerceIn(-0.3f, 1.3f)

                            val distToP1 = abs(normX - cp1x) + abs(normY - cp1y)
                            val distToP2 = abs(normX - cp2x) + abs(normY - cp2y)

                            if (distToP1 < distToP2) {
                                cp1x = normX
                                cp1y = normY
                            } else {
                                cp2x = normX
                                cp2y = normY
                            }

                            if (activeKeyframe != null) {
                                onUpdateBezier(
                                    activeKeyframe.id,
                                    EasingCurveType.BEZIER_CUSTOM,
                                    cp1x, cp1y, cp2x, cp2y
                                )
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Background Grid Lines
                drawLine(Color.White.copy(alpha = 0.1f), Offset(0f, h), Offset(w, h), 1f)
                drawLine(Color.White.copy(alpha = 0.1f), Offset(0f, 0f), Offset(w, 0f), 1f)
                drawLine(Color.White.copy(alpha = 0.1f), Offset(0f, h / 2f), Offset(w, h / 2f), 1f)
                drawLine(Color.White.copy(alpha = 0.1f), Offset(w / 2f, 0f), Offset(w / 2f, h), 1f)

                val p0 = Offset(0f, h)
                val p3 = Offset(w, 0f)
                val p1 = Offset(cp1x * w, h - cp1y * h)
                val p2 = Offset(cp2x * w, h - cp2y * h)

                // Tangent handle arms
                drawLine(NeonCyan.copy(alpha = 0.8f), p0, p1, strokeWidth = 2f)
                drawLine(NeonPink.copy(alpha = 0.8f), p3, p2, strokeWidth = 2f)

                // The Cubic Bézier Curve
                val curvePath = Path().apply {
                    moveTo(p0.x, p0.y)
                    cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
                }
                drawPath(curvePath, color = KeyframeDiamond, style = Stroke(width = 3.5f))

                // Handle Nodes
                drawCircle(NeonCyan, radius = 9f, center = p1)
                drawCircle(Color.White, radius = 5f, center = p1)

                drawCircle(NeonPink, radius = 9f, center = p2)
                drawCircle(Color.White, radius = 5f, center = p2)

                // End Nodes
                drawCircle(Color.White, radius = 6f, center = p0)
                drawCircle(Color.White, radius = 6f, center = p3)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Preset Curves Row
        Text("Curve Presets:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val presets = listOf(
                "Linear" to QuadCurve(0.0f, 0.0f, 1.0f, 1.0f, EasingCurveType.LINEAR),
                "Ease In" to QuadCurve(0.42f, 0.0f, 1.0f, 1.0f, EasingCurveType.EASE_IN),
                "Ease Out" to QuadCurve(0.0f, 0.0f, 0.58f, 1.0f, EasingCurveType.EASE_OUT),
                "Ease In-Out" to QuadCurve(0.42f, 0.0f, 0.58f, 1.0f, EasingCurveType.EASE_IN_OUT),
                "Fast In / Out" to QuadCurve(0.1f, 0.9f, 0.2f, 1.0f, EasingCurveType.BEZIER_CUSTOM),
                "Bounce" to QuadCurve(0.2f, 1.4f, 0.4f, 0.8f, EasingCurveType.BOUNCE),
                "Elastic" to QuadCurve(0.6f, -0.28f, 0.735f, 0.045f, EasingCurveType.ELASTIC)
            )

            items(presets) { (name, curve) ->
                OutlinedButton(
                    onClick = {
                        cp1x = curve.x1
                        cp1y = curve.y1
                        cp2x = curve.x2
                        cp2y = curve.y2
                        if (activeKeyframe != null) {
                            onUpdateBezier(activeKeyframe.id, curve.easing, cp1x, cp1y, cp2x, cp2y)
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(name, fontSize = 11.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Keyframe Details & Delete Button
        if (activeKeyframe != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Keyframe @ ${activeKeyframe.timeMs}ms • Val: ${"%.2f".format(activeKeyframe.value)}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )

                TextButton(
                    onClick = { onDeleteKeyframe(activeKeyframe.id) },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete Keyframe", fontSize = 12.sp)
                }
            }
        }
    }
}

private data class QuadCurve(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val easing: EasingCurveType)
