package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.ActiveSheet
import com.example.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBackToProjects: () -> Unit,
    modifier: Modifier = Modifier
) {
    val project by viewModel.project.collectAsState()
    val currentTimeMs by viewModel.currentTimeMs.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isLooping by viewModel.isLooping.collectAsState()
    val selectedClipId by viewModel.selectedClipId.collectAsState()
    val activeSheet by viewModel.activeSheet.collectAsState()
    val activeKeyframeProp by viewModel.activeKeyframeProperty.collectAsState()
    val safeGuideOverlay by viewModel.safeGuideOverlay.collectAsState()
    val exportProgress by viewModel.exportProgress.collectAsState()
    val isExportComplete by viewModel.isExportComplete.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    val selectedClip = remember(project, selectedClipId) {
        project.tracks.flatMap { it.clips }.find { it.id == selectedClipId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = project.name,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${project.aspectRatio.label.substringBefore(" ")} • ${project.fps} FPS • ${project.durationMs / 1000}s",
                            color = NeonCyan,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToProjects, modifier = Modifier.testTag("btn_back_to_projects")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Projects",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.openSheet(ActiveSheet.EXPORT_DIALOG) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(32.dp)
                            .testTag("btn_export_top")
                    ) {
                        Icon(Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioDarkBg)
            )
        },
        containerColor = StudioDarkBg,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Viewport & Direct Touch Canvas
            CanvasPreview(
                project = project,
                currentTimeMs = currentTimeMs,
                isPlaying = isPlaying,
                isLooping = isLooping,
                selectedClipId = selectedClipId,
                safeGuideOverlay = safeGuideOverlay,
                activeKeyframeProperty = activeKeyframeProp,
                onTogglePlay = { viewModel.togglePlayback() },
                onStepFrame = { forward -> viewModel.stepFrame(forward) },
                onToggleLoop = { viewModel.toggleLoop() },
                onToggleSafeGuide = { viewModel.toggleSafeGuide() },
                onAddKeyframe = { viewModel.toggleKeyframeAtCurrentTime() },
                onJumpKeyframe = { forward -> viewModel.jumpToKeyframe(forward) },
                onUpdateTransform = { clipId, px, py -> viewModel.updateClipTransform(clipId, px, py) },
                onSelectClip = { viewModel.selectClip(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
            )

            // Multi-Track Timeline
            TimelineView(
                project = project,
                currentTimeMs = currentTimeMs,
                selectedClipId = selectedClipId,
                onSeekTo = { viewModel.seekTo(it) },
                onSelectClip = { viewModel.selectClip(it) },
                onTrimStart = { clipId, newStart -> viewModel.trimClipStart(clipId, newStart) },
                onTrimEnd = { clipId, newDur -> viewModel.trimClipEnd(clipId, newDur) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            )

            // Primary Editor Toolbar (Split, Curves, Effects, Speed, Audio, Transitions, etc.)
            EditorToolbar(
                hasSelectedClip = selectedClipId != null,
                canUndo = canUndo,
                canRedo = canRedo,
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onSplit = { viewModel.splitClipAtPlayhead() },
                onOpenSheet = { viewModel.openSheet(it) },
                onDuplicate = { viewModel.duplicateSelectedClip() },
                onDelete = { viewModel.deleteSelectedClip() },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Active Bottom Sheets for Pro Tools
        activeSheet?.let { sheet ->
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeSheet() },
                containerColor = StudioSurface,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .width(36.dp)
                            .height(4.dp)
                            .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                    )
                }
            ) {
                when (sheet) {
                    ActiveSheet.KEYFRAME_GRAPH -> {
                        selectedClip?.let { clip ->
                            KeyframeGraphSheet(
                                clip = clip,
                                activeProperty = activeKeyframeProp,
                                currentTimeMs = currentTimeMs,
                                onSelectProperty = { viewModel.setActiveKeyframeProperty(it) },
                                onUpdateBezier = { kfId, easing, p1x, p1y, p2x, p2y ->
                                    viewModel.updateKeyframeBezier(clip.id, kfId, easing, p1x, p1y, p2x, p2y)
                                },
                                onDeleteKeyframe = { kfId ->
                                    viewModel.deleteKeyframe(clip.id, kfId)
                                },
                                onClose = { viewModel.closeSheet() }
                            )
                        } ?: viewModel.closeSheet()
                    }

                    ActiveSheet.EFFECTS_RACK -> {
                        selectedClip?.let { clip ->
                            EffectsRackSheet(
                                clip = clip,
                                onAddEffect = { effectType -> viewModel.addEffectToClip(clip.id, effectType) },
                                onToggleEffect = { effectId -> viewModel.toggleEffectEnabled(clip.id, effectId) },
                                onRemoveEffect = { effectId -> viewModel.removeEffectFromClip(clip.id, effectId) },
                                onUpdateParams = { effectId, intensity, p1, p2 ->
                                    viewModel.updateEffectParams(clip.id, effectId, intensity, p1, p2)
                                },
                                onClose = { viewModel.closeSheet() }
                            )
                        } ?: viewModel.closeSheet()
                    }

                    ActiveSheet.SPEED_RAMP -> {
                        selectedClip?.let { clip ->
                            SpeedCurveSheet(
                                clip = clip,
                                onUpdateSpeed = { speed, preset -> viewModel.updateClipSpeed(clip.id, speed, preset) },
                                onClose = { viewModel.closeSheet() }
                            )
                        } ?: viewModel.closeSheet()
                    }

                    ActiveSheet.AUDIO_FX -> {
                        selectedClip?.let { clip ->
                            AudioSheet(
                                clip = clip,
                                onUpdateAudio = { vol, fin, fout, pitch ->
                                    viewModel.updateClipAudio(clip.id, vol, fin, fout, pitch)
                                },
                                onClose = { viewModel.closeSheet() }
                            )
                        } ?: viewModel.closeSheet()
                    }

                    ActiveSheet.COLOR_GRADE -> {
                        selectedClip?.let { clip ->
                            ColorGradingSheet(
                                clip = clip,
                                onUpdateColor = { filter, exp, con, sat, temp, vig ->
                                    viewModel.updateClipColorGrade(clip.id, filter, exp, con, sat, temp, vig)
                                },
                                onClose = { viewModel.closeSheet() }
                            )
                        } ?: viewModel.closeSheet()
                    }

                    ActiveSheet.TEXT_STYLE, ActiveSheet.SHAPE_STYLE -> {
                        selectedClip?.let { clip ->
                            TextAndShapeSheet(
                                clip = clip,
                                onUpdateText = { text, font, size, col, bg, anim ->
                                    viewModel.updateClipText(clip.id, text, font, size, col, bg, anim)
                                },
                                onUpdateShape = { shape, fill, stroke, sWidth, corner ->
                                    viewModel.updateClipShape(clip.id, shape, fill, stroke, sWidth, corner)
                                },
                                onUpdateBlendMode = { mode ->
                                    viewModel.updateClipBlendMode(clip.id, mode)
                                },
                                onClose = { viewModel.closeSheet() }
                            )
                        } ?: viewModel.closeSheet()
                    }

                    ActiveSheet.TRANSITIONS -> {
                        selectedClip?.let { clip ->
                            TransitionSheet(
                                clip = clip,
                                onUpdateTransitions = { tIn, tOut, dur ->
                                    viewModel.updateClipTransitions(clip.id, tIn, tOut, dur)
                                },
                                onClose = { viewModel.closeSheet() }
                            )
                        } ?: viewModel.closeSheet()
                    }

                    ActiveSheet.ADD_LAYER -> {
                        AddLayerSheet(
                            onAddLayer = { clipType, title, text, shape ->
                                viewModel.addClipLayer(clipType, title, text, shape)
                            },
                            onClose = { viewModel.closeSheet() }
                        )
                    }

                    ActiveSheet.EXPORT, ActiveSheet.EXPORT_DIALOG -> {
                        var exportSettings by remember { mutableStateOf(ExportSettings()) }
                        ExportSheet(
                            project = project,
                            settings = exportSettings,
                            exportProgress = exportProgress,
                            isExportComplete = isExportComplete,
                            onUpdateSettings = { exportSettings = it },
                            onStartExport = { viewModel.startExportSimulation(exportSettings) },
                            onDismiss = { viewModel.closeSheet() }
                        )
                    }

                    ActiveSheet.NONE, ActiveSheet.PROJECT_SETTINGS -> {
                        viewModel.closeSheet()
                    }
                }
            }
        }
    }
}
