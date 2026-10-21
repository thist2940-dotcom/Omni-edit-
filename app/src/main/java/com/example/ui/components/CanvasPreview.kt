package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MotionEngine
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun CanvasPreview(
    project: Project,
    currentTimeMs: Long,
    isPlaying: Boolean,
    isLooping: Boolean,
    selectedClipId: String?,
    safeGuideOverlay: Boolean,
    activeKeyframeProperty: KeyframeProperty,
    onTogglePlay: () -> Unit,
    onStepFrame: (Boolean) -> Unit,
    onToggleLoop: () -> Unit,
    onToggleSafeGuide: () -> Unit,
    onAddKeyframe: () -> Unit,
    onJumpKeyframe: (Boolean) -> Unit,
    onUpdateTransform: (clipId: String, posX: Float, posY: Float) -> Unit,
    onSelectClip: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Determine if selected clip has a keyframe near current playhead
    val selectedClip = remember(project, selectedClipId) {
        project.tracks.flatMap { it.clips }.find { it.id == selectedClipId }
    }
    val hasKeyframeAtPlayhead = remember(selectedClip, currentTimeMs, activeKeyframeProperty) {
        selectedClip?.keyframes?.any {
            it.property == activeKeyframeProperty && abs(it.timeMs - currentTimeMs) < 150L
        } == true
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioDarkBg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Viewport Container
        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .fillMaxWidth()
                .aspectRatio(project.aspectRatio.ratio.coerceIn(0.5625f, 2.33f))
                .shadow(16.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .border(1.dp, StudioBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("preview_canvas")
                    .pointerInput(selectedClipId) {
                        if (selectedClipId != null) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val currentX = selectedClip?.posX ?: 0f
                                val currentY = selectedClip?.posY ?: 0f
                                onUpdateTransform(
                                    selectedClipId,
                                    currentX + dragAmount.x * 0.8f,
                                    currentY + dragAmount.y * 0.8f
                                )
                            }
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height
                val center = Offset(canvasW / 2f, canvasH / 2f)

                // Render tracks bottom-to-top
                val sortedTracks = project.tracks.filter { it.isVisible }
                for (track in sortedTracks) {
                    for (clip in track.clips) {
                        val transform = MotionEngine.evaluateClipTransform(clip, currentTimeMs) ?: continue

                        val clipCenterX = center.x + transform.posX
                        val clipCenterY = center.y + transform.posY

                        withTransform({
                            translate(clipCenterX, clipCenterY)
                            rotate(transform.rotationDeg)
                            scale(transform.scaleX, transform.scaleY)
                        }) {
                            val blend = MotionEngine.mapBlendMode(clip.blendMode)

                            when (clip.clipType) {
                                ClipType.VIDEO -> {
                                    drawVideoSimulation(
                                        clip = clip,
                                        size = size,
                                        transform = transform,
                                        currentTimeMs = currentTimeMs,
                                        blendMode = blend
                                    )
                                }
                                ClipType.SHAPE -> {
                                    drawVectorShape(
                                        clip = clip,
                                        transform = transform,
                                        blendMode = blend
                                    )
                                }
                                ClipType.TEXT -> {
                                    drawKineticText(
                                        clip = clip,
                                        transform = transform,
                                        textMeasurer = textMeasurer,
                                        currentTimeMs = currentTimeMs,
                                        blendMode = blend
                                    )
                                }
                                ClipType.ADJUSTMENT -> {
                                    drawAdjustmentLayer(
                                        clip = clip,
                                        size = size,
                                        transform = transform
                                    )
                                }
                                ClipType.AUDIO -> {
                                    // Audio does not render visual canvas primitives
                                }
                            }
                        }

                        // Selected Clip Bounding Box and Transform Anchors
                        if (clip.id == selectedClipId) {
                            withTransform({
                                translate(clipCenterX, clipCenterY)
                                rotate(transform.rotationDeg)
                                scale(transform.scaleX, transform.scaleY)
                            }) {
                                val boxW = 200f
                                val boxH = 120f
                                val halfW = boxW / 2f
                                val halfH = boxH / 2f

                                drawRect(
                                    color = NeonCyan,
                                    topLeft = Offset(-halfW, -halfH),
                                    size = Size(boxW, boxH),
                                    style = Stroke(
                                        width = 2f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                                    )
                                )

                                // Corner Handles
                                val handleColor = NeonPurple
                                val handleRadius = 6f
                                drawCircle(handleColor, handleRadius, Offset(-halfW, -halfH))
                                drawCircle(handleColor, handleRadius, Offset(halfW, -halfH))
                                drawCircle(handleColor, handleRadius, Offset(-halfW, halfH))
                                drawCircle(handleColor, handleRadius, Offset(halfW, halfH))

                                // Rotation Pivot Stem & Handle
                                drawLine(NeonCyan, Offset(0f, -halfH), Offset(0f, -halfH - 24f), 2f)
                                drawCircle(NeonAmber, 7f, Offset(0f, -halfH - 24f))
                            }
                        }
                    }
                }

                // Safe Guide Overlays (TikTok safe zones & Rule of Thirds)
                if (safeGuideOverlay) {
                    drawSafeGuides(canvasW, canvasH)
                }
            }

            // Aspect Ratio badge
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = "${project.aspectRatio.label.substringBefore(" ")} • ${project.fps} FPS",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // Transport Controls Bar (Play/Pause, Step Frames, Timecode, Keyframe Diamond)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Timecode & Frame Step Back
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onStepFrame(false) },
                    modifier = Modifier.size(36.dp).testTag("btn_step_back")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Frame",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                val currentSeconds = currentTimeMs / 1000f
                val totalSeconds = project.durationMs / 1000f
                val currentMins = (currentSeconds / 60).toInt()
                val currentSecs = (currentSeconds % 60).toInt()
                val currentFrames = ((currentSeconds - currentSeconds.toInt()) * project.fps).toInt()

                Text(
                    text = String.format("%02d:%02d:%02d", currentMins, currentSecs, currentFrames),
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }

            // Center: Big Play / Pause Button
            FilledIconButton(
                onClick = onTogglePlay,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isPlaying) NeonAmber else NeonPurple,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .size(44.dp)
                    .testTag("btn_play_pause")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(26.dp)
                )
            }

            // Right: Step Frame Forward & Alight Motion Keyframe Diamond
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onStepFrame(true) },
                    modifier = Modifier.size(36.dp).testTag("btn_step_fwd")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Frame",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Prev Keyframe
                IconButton(
                    onClick = { onJumpKeyframe(false) },
                    modifier = Modifier.size(32.dp).testTag("btn_prev_kf")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous Keyframe",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }

                // Keyframe Diamond Button
                IconButton(
                    onClick = onAddKeyframe,
                    enabled = selectedClipId != null,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (hasKeyframeAtPlayhead) KeyframeDiamond.copy(alpha = 0.25f) else Color.Transparent,
                            CircleShape
                        )
                        .testTag("btn_keyframe_diamond")
                ) {
                    Icon(
                        imageVector = if (hasKeyframeAtPlayhead) Icons.Filled.Diamond else Icons.Outlined.Diamond,
                        contentDescription = "Keyframe Toggle",
                        tint = if (hasKeyframeAtPlayhead) KeyframeDiamond else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Next Keyframe
                IconButton(
                    onClick = { onJumpKeyframe(true) },
                    modifier = Modifier.size(32.dp).testTag("btn_next_kf")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next Keyframe",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }

                // Safe Guide toggle
                IconButton(
                    onClick = onToggleSafeGuide,
                    modifier = Modifier.size(32.dp).testTag("btn_safe_guides")
                ) {
                    Icon(
                        imageVector = if (safeGuideOverlay) Icons.Filled.GridOn else Icons.Outlined.GridOff,
                        contentDescription = "Toggle Grid Guides",
                        tint = if (safeGuideOverlay) NeonCyan else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Visual drawing helpers for Canvas
private fun DrawScope.drawVideoSimulation(
    clip: ClipItem,
    size: Size,
    transform: MotionEngine.EvaluatedTransform,
    currentTimeMs: Long,
    blendMode: BlendMode
) {
    val baseColor = runCatching { Color(android.graphics.Color.parseColor(clip.assetColorHex)) }.getOrDefault(Color(0xFF6366F1))
    val w = size.width
    val h = size.height
    val halfW = w / 2f
    val halfH = h / 2f

    // Dynamic gradient background simulating video motion
    val pulse = (sin(currentTimeMs * 0.003f) + 1f) / 2f
    val gradStart = Offset(-halfW + pulse * 100f, -halfH)
    val gradEnd = Offset(halfW, halfH - pulse * 100f)

    val brush = Brush.linearGradient(
        colors = listOf(
            baseColor.copy(alpha = transform.opacity),
            Color(0xFF0F172A).copy(alpha = transform.opacity),
            baseColor.copy(alpha = transform.opacity * 0.6f)
        ),
        start = gradStart,
        end = gradEnd
    )
    drawRect(brush = brush, topLeft = Offset(-halfW, -halfH), size = Size(w, h), blendMode = blendMode)

    // Simulating scenic motion lines
    for (i in 0..4) {
        val yOffset = -halfH + (i * h / 5f) + (sin(currentTimeMs * 0.002f + i) * 20f)
        drawLine(
            color = Color.White.copy(alpha = 0.15f * transform.opacity),
            start = Offset(-halfW, yOffset),
            end = Offset(halfW, yOffset),
            strokeWidth = 2f
        )
    }

    // RGB Split effect simulation
    if (transform.rgbSplitOffset > 1f) {
        val offset = transform.rgbSplitOffset
        drawRect(
            color = Color.Red.copy(alpha = 0.25f * transform.opacity),
            topLeft = Offset(-halfW - offset, -halfH),
            size = Size(w, h),
            blendMode = BlendMode.Screen
        )
        drawRect(
            color = Color.Cyan.copy(alpha = 0.25f * transform.opacity),
            topLeft = Offset(-halfW + offset, -halfH),
            size = Size(w, h),
            blendMode = BlendMode.Screen
        )
    }

    // Glitch scanlines effect
    if (abs(transform.glitchOffset) > 1f) {
        val glitchY = -halfH + ((currentTimeMs * 0.5f) % h)
        drawRect(
            color = Color.White.copy(alpha = 0.4f),
            topLeft = Offset(-halfW + transform.glitchOffset, glitchY),
            size = Size(w, 24f),
            blendMode = BlendMode.Overlay
        )
    }

    // Flash white transition overlay
    if (transform.transitionFlashAlpha > 0.01f) {
        drawRect(
            color = Color.White.copy(alpha = transform.transitionFlashAlpha),
            topLeft = Offset(-halfW, -halfH),
            size = Size(w, h),
            blendMode = BlendMode.Plus
        )
    }

    // Vignette
    if (clip.vignette > 0.05f) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = clip.vignette * 0.85f)),
                center = Offset(0f, 0f),
                radius = max(w, h) * 0.7f
            ),
            topLeft = Offset(-halfW, -halfH),
            size = Size(w, h)
        )
    }
}

private fun DrawScope.drawVectorShape(
    clip: ClipItem,
    transform: MotionEngine.EvaluatedTransform,
    blendMode: BlendMode
) {
    val fillColor = runCatching { Color(android.graphics.Color.parseColor(clip.shapeFillHex)) }.getOrDefault(NeonPurple)
    val strokeColor = runCatching { Color(android.graphics.Color.parseColor(clip.shapeStrokeHex)) }.getOrDefault(Color.White)
    val strokeW = clip.shapeStrokeWidthDp * 2.5f

    val radius = 80f

    when (clip.shapeType) {
        ShapeType.RECTANGLE -> {
            val rectSize = Size(160f, 100f)
            val topleft = Offset(-80f, -50f)
            drawRoundRect(
                color = fillColor.copy(alpha = transform.opacity),
                topLeft = topleft,
                size = rectSize,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(clip.shapeCornerRadiusDp * 2f),
                blendMode = blendMode
            )
            if (strokeW > 0f) {
                drawRoundRect(
                    color = strokeColor.copy(alpha = transform.opacity),
                    topLeft = topleft,
                    size = rectSize,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(clip.shapeCornerRadiusDp * 2f),
                    style = Stroke(width = strokeW),
                    blendMode = blendMode
                )
            }
        }
        ShapeType.CIRCLE -> {
            drawCircle(
                color = fillColor.copy(alpha = transform.opacity),
                radius = radius,
                center = Offset(0f, 0f),
                blendMode = blendMode
            )
            if (strokeW > 0f) {
                drawCircle(
                    color = strokeColor.copy(alpha = transform.opacity),
                    radius = radius,
                    center = Offset(0f, 0f),
                    style = Stroke(width = strokeW),
                    blendMode = blendMode
                )
            }
        }
        ShapeType.STAR -> {
            val path = Path()
            val points = 5
            val innerRadius = radius * 0.45f
            val outerRadius = radius
            for (i in 0 until points * 2) {
                val r = if (i % 2 == 0) outerRadius else innerRadius
                val angle = (i * PI / points - PI / 2).toFloat()
                val x = r * cos(angle)
                val y = r * sin(angle)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, fillColor.copy(alpha = transform.opacity), blendMode = blendMode)
            if (strokeW > 0f) {
                drawPath(path, strokeColor.copy(alpha = transform.opacity), style = Stroke(width = strokeW), blendMode = blendMode)
            }
        }
        ShapeType.TRIANGLE -> {
            val path = Path().apply {
                moveTo(0f, -radius)
                lineTo(radius * 0.866f, radius * 0.5f)
                lineTo(-radius * 0.866f, radius * 0.5f)
                close()
            }
            drawPath(path, fillColor.copy(alpha = transform.opacity), blendMode = blendMode)
            if (strokeW > 0f) {
                drawPath(path, strokeColor.copy(alpha = transform.opacity), style = Stroke(width = strokeW), blendMode = blendMode)
            }
        }
        ShapeType.HEART -> {
            val path = Path().apply {
                moveTo(0f, radius * 0.3f)
                cubicTo(-radius, -radius * 0.6f, -radius * 0.5f, -radius, 0f, -radius * 0.3f)
                cubicTo(radius * 0.5f, -radius, radius, -radius * 0.6f, 0f, radius * 0.3f)
            }
            drawPath(path, fillColor.copy(alpha = transform.opacity), blendMode = blendMode)
            if (strokeW > 0f) {
                drawPath(path, strokeColor.copy(alpha = transform.opacity), style = Stroke(width = strokeW), blendMode = blendMode)
            }
        }
        ShapeType.WAVE -> {
            val path = Path().apply {
                moveTo(-radius, 0f)
                cubicTo(-radius * 0.5f, -radius * 0.5f, 0f, radius * 0.5f, radius * 0.5f, -radius * 0.5f)
                lineTo(radius, 0f)
            }
            drawPath(path, strokeColor.copy(alpha = transform.opacity), style = Stroke(width = strokeW.coerceAtLeast(4f)), blendMode = blendMode)
        }
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawKineticText(
    clip: ClipItem,
    transform: MotionEngine.EvaluatedTransform,
    textMeasurer: TextMeasurer,
    currentTimeMs: Long,
    blendMode: BlendMode
) {
    val fontColor = runCatching { Color(android.graphics.Color.parseColor(clip.fontColorHex)) }.getOrDefault(Color.White)
    val textBgColor = runCatching { Color(android.graphics.Color.parseColor(clip.textBgColorHex)) }.getOrDefault(Color(0x99000000))

    val layoutResult = textMeasurer.measure(
        text = AnnotatedString(clip.text),
        style = TextStyle(
            fontSize = clip.fontSizeSp.sp,
            fontWeight = FontWeight.Bold,
            color = fontColor.copy(alpha = transform.opacity)
        )
    )

    val halfW = layoutResult.size.width / 2f
    val halfH = layoutResult.size.height / 2f

    // Background pill/box
    val padding = 16f
    drawRoundRect(
        color = textBgColor.copy(alpha = transform.opacity * 0.8f),
        topLeft = Offset(-halfW - padding, -halfH - padding / 2f),
        size = Size(layoutResult.size.width + padding * 2, layoutResult.size.height + padding),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f),
        blendMode = blendMode
    )

    // Deep Glow aura
    if (transform.glowIntensity > 0.05f) {
        drawRoundRect(
            color = fontColor.copy(alpha = transform.glowIntensity * 0.4f * transform.opacity),
            topLeft = Offset(-halfW - padding - 8f, -halfH - padding / 2f - 8f),
            size = Size(layoutResult.size.width + padding * 2 + 16f, layoutResult.size.height + padding + 16f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f),
            blendMode = BlendMode.Screen
        )
    }

    drawText(
        textLayoutResult = layoutResult,
        topLeft = Offset(-halfW, -halfH)
    )
}

private fun DrawScope.drawAdjustmentLayer(
    clip: ClipItem,
    size: Size,
    transform: MotionEngine.EvaluatedTransform
) {
    val w = size.width
    val h = size.height
    val halfW = w / 2f
    val halfH = h / 2f

    if (transform.filmGrainNoise > 0.05f) {
        // Film grain noise particles
        for (i in 0..50) {
            val px = -halfW + ((i * 37) % w.toInt())
            val py = -halfH + ((i * 73) % h.toInt())
            drawCircle(
                color = Color.White.copy(alpha = 0.2f * transform.filmGrainNoise),
                radius = 1.5f,
                center = Offset(px.toFloat(), py.toFloat()),
                blendMode = BlendMode.Overlay
            )
        }
    }
}

private fun DrawScope.drawSafeGuides(w: Float, h: Float) {
    val guideColor = Color.White.copy(alpha = 0.3f)
    val stroke = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))

    // Rule of Thirds Lines
    drawLine(guideColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = 1f, pathEffect = stroke.pathEffect)
    drawLine(guideColor, Offset(2 * w / 3f, 0f), Offset(2 * w / 3f, h), strokeWidth = 1f, pathEffect = stroke.pathEffect)
    drawLine(guideColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = 1f, pathEffect = stroke.pathEffect)
    drawLine(guideColor, Offset(0f, 2 * h / 3f), Offset(w, 2 * h / 3f), strokeWidth = 1f, pathEffect = stroke.pathEffect)

    // Center Crosshair
    val cx = w / 2f
    val cy = h / 2f
    drawLine(NeonCyan.copy(alpha = 0.6f), Offset(cx - 16f, cy), Offset(cx + 16f, cy), 1.5f)
    drawLine(NeonCyan.copy(alpha = 0.6f), Offset(cx, cy - 16f), Offset(cx, cy + 16f), 1.5f)

    // TikTok / Reels Safe Margins (Top 12% and Bottom 20% overlay guides)
    drawRect(
        color = NeonPink.copy(alpha = 0.1f),
        topLeft = Offset(0f, 0f),
        size = Size(w, h * 0.12f)
    )
    drawRect(
        color = NeonPink.copy(alpha = 0.12f),
        topLeft = Offset(0f, h * 0.78f),
        size = Size(w, h * 0.22f)
    )
}
