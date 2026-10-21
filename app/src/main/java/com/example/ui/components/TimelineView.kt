package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.max

@Composable
fun TimelineView(
    project: Project,
    currentTimeMs: Long,
    selectedClipId: String?,
    onSeekTo: (Long) -> Unit,
    onSelectClip: (String?) -> Unit,
    onTrimStart: (clipId: String, newStartTime: Long) -> Unit,
    onTrimEnd: (clipId: String, newDuration: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var pixelsPerSecond by remember { mutableFloatStateOf(80f) }
    val msPerPixel = 1000f / pixelsPerSecond

    val totalDurationMs = project.durationMs
    val timelineWidthDp = ((totalDurationMs / 1000f) * pixelsPerSecond).dp.coerceAtLeast(360.dp)

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .border(1.dp, StudioBorder)
    ) {
        // Timeline Header: Zoom controls & Add Track button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "TIMELINE • ${project.tracks.size} TRACKS",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { pixelsPerSecond = max(40f, pixelsPerSecond - 20f) },
                    modifier = Modifier.size(28.dp).testTag("btn_zoom_out")
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom Out",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "${pixelsPerSecond.toInt()}px/s",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )

                IconButton(
                    onClick = { pixelsPerSecond = (pixelsPerSecond + 20f).coerceAtMost(200f) },
                    modifier = Modifier.size(28.dp).testTag("btn_zoom_in")
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom In",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Horizontal Scrollable Timeline Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .pointerInput(totalDurationMs, msPerPixel) {
                    detectTapGestures { offset ->
                        val clickedMs = (offset.x * msPerPixel).toLong().coerceIn(0L, totalDurationMs)
                        onSeekTo(clickedMs)
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .width(timelineWidthDp + 80.dp)
                    .padding(bottom = 8.dp)
            ) {
                // Time Ruler
                TimeRuler(
                    totalDurationMs = totalDurationMs,
                    pixelsPerSecond = pixelsPerSecond,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .background(StudioSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Track Rows
                project.tracks.forEach { track ->
                    TrackRow(
                        track = track,
                        pixelsPerSecond = pixelsPerSecond,
                        selectedClipId = selectedClipId,
                        onSelectClip = onSelectClip,
                        onTrimStart = onTrimStart,
                        onTrimEnd = onTrimEnd,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )
                }
            }

            // Playhead Scrub Line
            val playheadOffsetPx = (currentTimeMs / 1000f) * pixelsPerSecond
            Box(
                modifier = Modifier
                    .offset(x = playheadOffsetPx.dp - 7.dp)
                    .width(14.dp)
                    .fillMaxHeight()
                    .pointerInput(totalDurationMs, msPerPixel) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaMs = (dragAmount.x * msPerPixel).toLong()
                            onSeekTo((currentTimeMs + deltaMs).coerceIn(0L, totalDurationMs))
                        }
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Playhead Diamond Head
                    val headW = 14.dp.toPx()
                    val headH = 14.dp.toPx()
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(headW / 2f, headH)
                        lineTo(0f, headH * 0.4f)
                        lineTo(0f, 0f)
                        lineTo(headW, 0f)
                        lineTo(headW, headH * 0.4f)
                        close()
                    }
                    drawPath(path, TimelinePlayhead)

                    // Vertical Needle Line
                    drawLine(
                        color = TimelinePlayhead,
                        start = Offset(headW / 2f, headH),
                        end = Offset(headW / 2f, size.height),
                        strokeWidth = 2.5f
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeRuler(
    totalDurationMs: Long,
    pixelsPerSecond: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val totalSeconds = (totalDurationMs / 1000f).toInt() + 1
        for (sec in 0..totalSeconds) {
            val x = sec * pixelsPerSecond

            // Major second tick
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(x, 0f),
                end = Offset(x, 16f),
                strokeWidth = 1.5f
            )

            // Sub-second ticks
            val subStep = pixelsPerSecond / 4f
            for (sub in 1..3) {
                drawLine(
                    color = Color.White.copy(alpha = 0.2f),
                    start = Offset(x + sub * subStep, 8f),
                    end = Offset(x + sub * subStep, 16f),
                    strokeWidth = 1f
                )
            }
        }
    }
}

@Composable
private fun TrackRow(
    track: Track,
    pixelsPerSecond: Float,
    selectedClipId: String?,
    onSelectClip: (String?) -> Unit,
    onTrimStart: (clipId: String, newStartTime: Long) -> Unit,
    onTrimEnd: (clipId: String, newDuration: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor = when (track.type) {
        TrackType.VIDEO -> NeonPurple
        TrackType.OVERLAY -> NeonPink
        TrackType.TEXT -> NeonCyan
        TrackType.SHAPE -> NeonAmber
        TrackType.AUDIO -> NeonGreen
        TrackType.EFFECT -> Color(0xFF6366F1)
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .background(StudioSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
    ) {
        // Clips in this track
        track.clips.forEach { clip ->
            val leftOffsetPx = (clip.startTimeMs / 1000f) * pixelsPerSecond
            val widthPx = (clip.durationMs / 1000f) * pixelsPerSecond
            val isSelected = clip.id == selectedClipId

            Box(
                modifier = Modifier
                    .offset(x = leftOffsetPx.dp)
                    .width(widthPx.dp.coerceAtLeast(32.dp))
                    .height(40.dp)
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(6.dp))
                    .background(trackColor.copy(alpha = if (isSelected) 0.9f else 0.55f))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Color.White else trackColor,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelectClip(clip.id) }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = clip.title,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    // Transitions indicator
                    if (clip.transitionIn != TransitionType.NONE || clip.transitionOut != TransitionType.NONE) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Text(
                                text = "⚡",
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 3.dp)
                            )
                        }
                    }
                }

                // Keyframe Markers on the clip
                if (clip.keyframes.isNotEmpty()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 22.dp)
                    ) {
                        val clipDurationMs = clip.durationMs.toFloat()
                        clip.keyframes.forEach { kf ->
                            val kfRelMs = (kf.timeMs - clip.startTimeMs).toFloat()
                            if (kfRelMs in 0f..clipDurationMs) {
                                val kfX = (kfRelMs / clipDurationMs) * size.width
                                drawCircle(
                                    color = KeyframeDiamond,
                                    radius = 3.5f,
                                    center = Offset(kfX, 6f)
                                )
                                drawCircle(
                                    color = Color.Black,
                                    radius = 3.5f,
                                    center = Offset(kfX, 6f),
                                    style = Stroke(width = 1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
