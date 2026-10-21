package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ProjectEntity
import com.example.data.ProjectRepository
import com.example.engine.AudioSimulator
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class ActiveSheet {
    NONE,
    KEYFRAME_GRAPH,
    EFFECTS_RACK,
    COLOR_GRADE,
    TEXT_STYLE,
    SHAPE_STYLE,
    SPEED_RAMP,
    AUDIO_FX,
    TRANSITIONS,
    EXPORT,
    EXPORT_DIALOG,
    ADD_LAYER,
    PROJECT_SETTINGS
}

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository
    val allProjects: StateFlow<List<Project>>
    val savedProjects: StateFlow<List<ProjectEntity>>

    private val defaultInitialProject = SampleMediaLibrary.createCyberpunkTemplate()

    private val _currentProject = MutableStateFlow<Project?>(defaultInitialProject)
    val currentProject: StateFlow<Project?> = _currentProject.asStateFlow()

    val project: StateFlow<Project> = _currentProject
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = defaultInitialProject
        )

    private val _currentTimeMs = MutableStateFlow(0L)
    val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLooping = MutableStateFlow(true)
    val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    private val _selectedClipId = MutableStateFlow<String?>(null)
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    private val _selectedTrackId = MutableStateFlow<String?>(null)
    val selectedTrackId: StateFlow<String?> = _selectedTrackId.asStateFlow()

    private val _selectedKeyframeId = MutableStateFlow<String?>(null)
    val selectedKeyframeId: StateFlow<String?> = _selectedKeyframeId.asStateFlow()

    private val _activeKeyframeProperty = MutableStateFlow(KeyframeProperty.SCALE)
    val activeKeyframeProperty: StateFlow<KeyframeProperty> = _activeKeyframeProperty.asStateFlow()

    private val _activeSheet = MutableStateFlow(ActiveSheet.NONE)
    val activeSheet: StateFlow<ActiveSheet> = _activeSheet.asStateFlow()

    private val _safeGuideOverlay = MutableStateFlow(false)
    val safeGuideOverlay: StateFlow<Boolean> = _safeGuideOverlay.asStateFlow()

    private val _exportSettings = MutableStateFlow(ExportSettings())
    val exportSettings: StateFlow<ExportSettings> = _exportSettings.asStateFlow()

    private val _exportProgress = MutableStateFlow<Float?>(null)
    val exportProgress: StateFlow<Float?> = _exportProgress.asStateFlow()

    private val _isExportComplete = MutableStateFlow(false)
    val isExportComplete: StateFlow<Boolean> = _isExportComplete.asStateFlow()

    // Undo / Redo history
    private val undoStack = mutableListOf<Project>()
    private val redoStack = mutableListOf<Project>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private var playbackJob: Job? = null
    private var exportJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ProjectRepository(database.projectDao())
        allProjects = repository.allProjects.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        savedProjects = repository.allProjectEntities.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.ensureDefaultTemplates()
            // If current project is null, select the first project or cyberpunk template
            allProjects.collect { projects ->
                if (_currentProject.value == null && projects.isNotEmpty()) {
                    loadProject(projects.first().id)
                }
            }
        }
    }

    private fun pushHistory() {
        val current = _currentProject.value ?: return
        undoStack.add(current)
        if (undoStack.size > 20) undoStack.removeAt(0)
        redoStack.clear()
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = false
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = _currentProject.value ?: return
        redoStack.add(current)
        val prev = undoStack.removeAt(undoStack.lastIndex)
        _currentProject.value = prev
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
        viewModelScope.launch { repository.saveProject(prev) }
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _currentProject.value ?: return
        undoStack.add(current)
        val next = redoStack.removeAt(redoStack.lastIndex)
        _currentProject.value = next
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
        viewModelScope.launch { repository.saveProject(next) }
    }

    fun loadProject(id: String) {
        pause()
        viewModelScope.launch {
            val proj = repository.getProjectById(id)
            if (proj != null) {
                _currentProject.value = proj
                _currentTimeMs.value = 0L
                _selectedClipId.value = proj.tracks.flatMap { it.clips }.firstOrNull()?.id
                undoStack.clear()
                redoStack.clear()
                _canUndo.value = false
                _canRedo.value = false
            }
        }
    }

    fun createNewProject(name: String, aspectRatio: AspectRatioType, fps: Int, durationMs: Long) {
        pause()
        pushHistory()
        val videoTrackId = UUID.randomUUID().toString()
        val audioTrackId = UUID.randomUUID().toString()
        val textTrackId = UUID.randomUUID().toString()

        val sampleAsset = SampleMediaLibrary.STOCK_VIDEOS.first()
        val starterClip = ClipItem(
            id = UUID.randomUUID().toString(),
            trackId = videoTrackId,
            startTimeMs = 0L,
            durationMs = durationMs.coerceAtMost(sampleAsset.durationMs),
            clipType = ClipType.VIDEO,
            title = sampleAsset.title,
            assetId = sampleAsset.id,
            assetColorHex = sampleAsset.primaryColorHex
        )

        val newProj = Project(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "New Composition" },
            aspectRatio = aspectRatio,
            fps = fps,
            durationMs = durationMs,
            tracks = listOf(
                Track(id = videoTrackId, type = TrackType.VIDEO, name = "V1: Main Video", clips = listOf(starterClip)),
                Track(id = textTrackId, type = TrackType.TEXT, name = "V2: Titles", clips = emptyList()),
                Track(id = audioTrackId, type = TrackType.AUDIO, name = "A1: Soundtrack", clips = emptyList())
            )
        )

        _currentProject.value = newProj
        _currentTimeMs.value = 0L
        _selectedClipId.value = starterClip.id
        viewModelScope.launch {
            repository.saveProject(newProj)
        }
    }

    fun togglePlay() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        val proj = _currentProject.value ?: return
        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val stepMs = 33L // ~30fps smooth ticker
            while (_isPlaying.value) {
                delay(stepMs)
                val nextTime = _currentTimeMs.value + stepMs
                if (nextTime >= proj.durationMs) {
                    if (_isLooping.value) {
                        _currentTimeMs.value = 0L
                    } else {
                        _currentTimeMs.value = proj.durationMs
                        _isPlaying.value = false
                        break
                    }
                } else {
                    _currentTimeMs.value = nextTime
                }
            }
        }
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
    }

    fun seekTo(timeMs: Long) {
        val proj = _currentProject.value ?: return
        _currentTimeMs.value = timeMs.coerceIn(0L, proj.durationMs)
    }

    fun stepFrame(forward: Boolean) {
        val proj = _currentProject.value ?: return
        pause()
        val frameMs = (1000f / proj.fps).toLong().coerceAtLeast(16L)
        val delta = if (forward) frameMs else -frameMs
        seekTo(_currentTimeMs.value + delta)
    }

    fun selectClip(clipId: String?) {
        _selectedClipId.value = clipId
        if (clipId != null) {
            val project = _currentProject.value ?: return
            val track = project.tracks.find { t -> t.clips.any { it.id == clipId } }
            _selectedTrackId.value = track?.id
        }
    }

    fun setActiveSheet(sheet: ActiveSheet) {
        _activeSheet.value = sheet
    }

    fun toggleSafeGuideOverlay() {
        _safeGuideOverlay.value = !_safeGuideOverlay.value
    }

    fun toggleLooping() {
        _isLooping.value = !_isLooping.value
    }

    // --- CapCut Timeline Operations ---

    fun splitClipAtPlayhead() {
        val project = _currentProject.value ?: return
        val playhead = _currentTimeMs.value
        val clipId = _selectedClipId.value ?: findActiveClipAtPlayhead(project, playhead)?.id ?: return

        pushHistory()
        AudioSimulator.playSfx("split")

        var newSelectedId: String? = null
        val updatedTracks = project.tracks.map { track ->
            val clipToSplit = track.clips.find { it.id == clipId }
            if (clipToSplit != null && playhead > clipToSplit.startTimeMs && playhead < (clipToSplit.startTimeMs + clipToSplit.durationMs)) {
                val firstDuration = playhead - clipToSplit.startTimeMs
                val secondDuration = clipToSplit.durationMs - firstDuration
                val firstClip = clipToSplit.copy(durationMs = firstDuration)
                val secondClipId = UUID.randomUUID().toString()
                newSelectedId = secondClipId
                val secondClip = clipToSplit.copy(
                    id = secondClipId,
                    startTimeMs = playhead,
                    durationMs = secondDuration,
                    sourceOffsetMs = clipToSplit.sourceOffsetMs + firstDuration
                )

                val newClips = track.clips.flatMap { c ->
                    if (c.id == clipId) listOf(firstClip, secondClip) else listOf(c)
                }
                track.copy(clips = newClips)
            } else {
                track
            }
        }

        val updatedProj = project.copy(tracks = updatedTracks, lastModified = System.currentTimeMillis())
        _currentProject.value = updatedProj
        if (newSelectedId != null) _selectedClipId.value = newSelectedId
        viewModelScope.launch { repository.saveProject(updatedProj) }
    }

    private fun findActiveClipAtPlayhead(project: Project, playhead: Long): ClipItem? {
        for (track in project.tracks.reversed()) {
            val clip = track.clips.find { playhead >= it.startTimeMs && playhead <= (it.startTimeMs + it.durationMs) }
            if (clip != null) return clip
        }
        return null
    }

    fun deleteSelectedClip() {
        val project = _currentProject.value ?: return
        val clipId = _selectedClipId.value ?: return
        pushHistory()
        val updatedTracks = project.tracks.map { track ->
            track.copy(clips = track.clips.filter { it.id != clipId })
        }
        val updatedProj = project.copy(tracks = updatedTracks, lastModified = System.currentTimeMillis())
        _currentProject.value = updatedProj
        _selectedClipId.value = null
        viewModelScope.launch { repository.saveProject(updatedProj) }
    }

    fun duplicateSelectedClip() {
        val project = _currentProject.value ?: return
        val clipId = _selectedClipId.value ?: return
        pushHistory()
        val updatedTracks = project.tracks.map { track ->
            val clip = track.clips.find { it.id == clipId }
            if (clip != null) {
                val duplicated = clip.copy(
                    id = UUID.randomUUID().toString(),
                    startTimeMs = clip.startTimeMs + clip.durationMs,
                    title = "${clip.title} (Copy)"
                )
                val newClips = track.clips.toMutableList()
                newClips.add(duplicated)
                track.copy(clips = newClips.sortedBy { it.startTimeMs })
            } else {
                track
            }
        }
        val updatedProj = project.copy(tracks = updatedTracks, lastModified = System.currentTimeMillis())
        _currentProject.value = updatedProj
        viewModelScope.launch { repository.saveProject(updatedProj) }
    }

    fun trimClipStart(clipId: String, newStartTimeMs: Long) {
        val project = _currentProject.value ?: return
        val updatedTracks = project.tracks.map { track ->
            val clip = track.clips.find { it.id == clipId }
            if (clip != null) {
                val clampedStart = newStartTimeMs.coerceIn(0L, clip.startTimeMs + clip.durationMs - 200L)
                val delta = clampedStart - clip.startTimeMs
                val updatedClip = clip.copy(
                    startTimeMs = clampedStart,
                    durationMs = clip.durationMs - delta,
                    sourceOffsetMs = clip.sourceOffsetMs + delta
                )
                track.copy(clips = track.clips.map { if (it.id == clipId) updatedClip else it })
            } else {
                track
            }
        }
        val updatedProj = project.copy(tracks = updatedTracks, lastModified = System.currentTimeMillis())
        _currentProject.value = updatedProj
        viewModelScope.launch { repository.saveProject(updatedProj) }
    }

    fun trimClipEnd(clipId: String, newDurationMs: Long) {
        val project = _currentProject.value ?: return
        val updatedTracks = project.tracks.map { track ->
            val clip = track.clips.find { it.id == clipId }
            if (clip != null) {
                val clampedDuration = newDurationMs.coerceAtLeast(300L)
                val updatedClip = clip.copy(durationMs = clampedDuration)
                track.copy(clips = track.clips.map { if (it.id == clipId) updatedClip else it })
            } else {
                track
            }
        }
        val updatedProj = project.copy(tracks = updatedTracks, lastModified = System.currentTimeMillis())
        _currentProject.value = updatedProj
        viewModelScope.launch { repository.saveProject(updatedProj) }
    }

    fun updateClipTransform(
        clipId: String,
        posX: Float? = null,
        posY: Float? = null,
        scaleX: Float? = null,
        scaleY: Float? = null,
        rotationDeg: Float? = null,
        opacity: Float? = null,
        blendMode: BlendModeType? = null
    ) {
        mutateClip(clipId) { clip ->
            clip.copy(
                posX = posX ?: clip.posX,
                posY = posY ?: clip.posY,
                scaleX = scaleX ?: clip.scaleX,
                scaleY = scaleY ?: clip.scaleY,
                rotationDeg = rotationDeg ?: clip.rotationDeg,
                opacity = opacity ?: clip.opacity,
                blendMode = blendMode ?: clip.blendMode
            )
        }
    }

    fun updateClipSpeed(clipId: String, speed: Float, preset: SpeedPreset) {
        mutateClip(clipId) { clip ->
            clip.copy(speed = speed, speedPreset = preset)
        }
    }

    fun updateClipTransitions(clipId: String, transIn: TransitionType, transOut: TransitionType, durationMs: Long) {
        mutateClip(clipId) { clip ->
            clip.copy(transitionIn = transIn, transitionOut = transOut, transitionDurationMs = durationMs)
        }
    }

    fun updateClipColorGrading(
        clipId: String,
        filterPreset: FilterPresetType,
        exposure: Float,
        contrast: Float,
        saturation: Float,
        temperature: Float,
        vignette: Float
    ) {
        mutateClip(clipId) { clip ->
            clip.copy(
                filterPreset = filterPreset,
                exposure = exposure,
                contrast = contrast,
                saturation = saturation,
                temperature = temperature,
                vignette = vignette
            )
        }
    }

    fun updateClipAudio(clipId: String, volume: Float, fadeInMs: Long, fadeOutMs: Long, pitchPreset: VoicePitchPreset) {
        mutateClip(clipId) { clip ->
            clip.copy(volume = volume, fadeInMs = fadeInMs, fadeOutMs = fadeOutMs, pitchPreset = pitchPreset)
        }
    }

    fun updateClipText(
        clipId: String,
        text: String,
        fontStyle: String,
        fontSizeSp: Float,
        fontColorHex: String,
        textBgColorHex: String,
        animation: String
    ) {
        mutateClip(clipId) { clip ->
            clip.copy(
                text = text,
                fontStyle = fontStyle,
                fontSizeSp = fontSizeSp,
                fontColorHex = fontColorHex,
                textBgColorHex = textBgColorHex,
                textAnimation = animation
            )
        }
    }

    fun updateClipShape(
        clipId: String,
        shapeType: ShapeType,
        fillHex: String,
        strokeHex: String,
        strokeWidth: Float,
        cornerRadius: Float
    ) {
        mutateClip(clipId) { clip ->
            clip.copy(
                shapeType = shapeType,
                shapeFillHex = fillHex,
                shapeStrokeHex = strokeHex,
                shapeStrokeWidthDp = strokeWidth,
                shapeCornerRadiusDp = cornerRadius
            )
        }
    }

    // --- Alight Motion Keyframe Operations ---

    fun setActiveKeyframeProperty(prop: KeyframeProperty) {
        _activeKeyframeProperty.value = prop
    }

    fun addOrToggleKeyframeAtPlayhead() {
        val project = _currentProject.value ?: return
        val clipId = _selectedClipId.value ?: return
        val clip = project.tracks.flatMap { it.clips }.find { it.id == clipId } ?: return
        val playhead = _currentTimeMs.value
        val prop = _activeKeyframeProperty.value

        pushHistory()
        AudioSimulator.playSfx("keyframe")

        val existingIndex = clip.keyframes.indexOfFirst { it.property == prop && kotlin.math.abs(it.timeMs - playhead) < 150L }
        val newKeyframes = clip.keyframes.toMutableList()

        if (existingIndex >= 0) {
            // Remove keyframe
            newKeyframes.removeAt(existingIndex)
        } else {
            // Add keyframe with current evaluated value
            val currentValue = when (prop) {
                KeyframeProperty.POSITION_X -> clip.posX
                KeyframeProperty.POSITION_Y -> clip.posY
                KeyframeProperty.SCALE -> clip.scaleX
                KeyframeProperty.ROTATION -> clip.rotationDeg
                KeyframeProperty.OPACITY -> clip.opacity
            }
            val newKf = KeyframePoint(
                timeMs = playhead,
                property = prop,
                value = currentValue,
                easing = EasingCurveType.EASE_IN_OUT
            )
            newKeyframes.add(newKf)
            newKeyframes.sortBy { it.timeMs }
            _selectedKeyframeId.value = newKf.id
        }

        mutateClip(clipId) { it.copy(keyframes = newKeyframes) }
    }

    fun updateKeyframeBezier(clipId: String, keyframeId: String, easing: EasingCurveType, cp1x: Float, cp1y: Float, cp2x: Float, cp2y: Float) {
        mutateClip(clipId) { clip ->
            val updated = clip.keyframes.map { kf ->
                if (kf.id == keyframeId) {
                    kf.copy(easing = easing, cp1x = cp1x, cp1y = cp1y, cp2x = cp2x, cp2y = cp2y)
                } else kf
            }
            clip.copy(keyframes = updated)
        }
    }

    fun updateKeyframeBezier(keyframeId: String, easing: EasingCurveType, cp1x: Float, cp1y: Float, cp2x: Float, cp2y: Float) {
        val clipId = _selectedClipId.value ?: return
        updateKeyframeBezier(clipId, keyframeId, easing, cp1x, cp1y, cp2x, cp2y)
    }

    fun jumpToKeyframe(forward: Boolean) {
        val project = _currentProject.value ?: return
        val clipId = _selectedClipId.value ?: return
        val clip = project.tracks.flatMap { it.clips }.find { it.id == clipId } ?: return
        val playhead = _currentTimeMs.value

        val targetKf = if (forward) {
            clip.keyframes.filter { it.timeMs > playhead + 50L }.minByOrNull { it.timeMs }
        } else {
            clip.keyframes.filter { it.timeMs < playhead - 50L }.maxByOrNull { it.timeMs }
        }

        if (targetKf != null) {
            seekTo(targetKf.timeMs)
            _selectedKeyframeId.value = targetKf.id
            _activeKeyframeProperty.value = targetKf.property
        }
    }

    // --- After Effects Effects Rack Operations ---

    fun addEffectToClip(clipId: String, type: EffectType) {
        pushHistory()
        mutateClip(clipId) { clip ->
            val newEffect = EffectInstance(
                type = type,
                intensity = 0.6f,
                param1 = 0.5f,
                param2 = 0.5f
            )
            clip.copy(effects = clip.effects + newEffect)
        }
    }

    fun toggleEffect(clipId: String, effectId: String) {
        mutateClip(clipId) { clip ->
            val updated = clip.effects.map { eff ->
                if (eff.id == effectId) eff.copy(isEnabled = !eff.isEnabled) else eff
            }
            clip.copy(effects = updated)
        }
    }

    fun removeEffect(clipId: String, effectId: String) {
        pushHistory()
        mutateClip(clipId) { clip ->
            clip.copy(effects = clip.effects.filter { it.id != effectId })
        }
    }

    fun updateEffectParam(clipId: String, effectId: String, intensity: Float, param1: Float, param2: Float) {
        mutateClip(clipId) { clip ->
            val updated = clip.effects.map { eff ->
                if (eff.id == effectId) eff.copy(intensity = intensity, param1 = param1, param2 = param2) else eff
            }
            clip.copy(effects = updated)
        }
    }

    // --- Add Layers & Tracks ---

    fun addLayer(type: ClipType, title: String, text: String = "", shapeType: ShapeType = ShapeType.RECTANGLE) {
        val project = _currentProject.value ?: return
        pushHistory()

        val matchingTrackType = when (type) {
            ClipType.VIDEO -> TrackType.OVERLAY
            ClipType.TEXT -> TrackType.TEXT
            ClipType.SHAPE -> TrackType.SHAPE
            ClipType.AUDIO -> TrackType.AUDIO
            ClipType.ADJUSTMENT -> TrackType.EFFECT
        }

        var targetTrack = project.tracks.find { it.type == matchingTrackType }
        val newTracks = project.tracks.toMutableList()

        if (targetTrack == null) {
            val newTrack = Track(
                id = UUID.randomUUID().toString(),
                type = matchingTrackType,
                name = matchingTrackType.label
            )
            newTracks.add(newTrack)
            targetTrack = newTrack
        }

        val playhead = _currentTimeMs.value
        val clipDuration = 4000L.coerceAtMost(project.durationMs - playhead).coerceAtLeast(1000L)
        val newClip = ClipItem(
            id = UUID.randomUUID().toString(),
            trackId = targetTrack.id,
            startTimeMs = playhead,
            durationMs = clipDuration,
            clipType = type,
            title = title,
            text = text.ifBlank { "New Text Layer" },
            shapeType = shapeType
        )

        val updatedTracks = newTracks.map { track ->
            if (track.id == targetTrack.id) {
                track.copy(clips = (track.clips + newClip).sortedBy { it.startTimeMs })
            } else track
        }

        val updatedProj = project.copy(tracks = updatedTracks, lastModified = System.currentTimeMillis())
        _currentProject.value = updatedProj
        _selectedClipId.value = newClip.id
        viewModelScope.launch { repository.saveProject(updatedProj) }
    }

    // --- Export Engine ---

    fun updateExportSettings(settings: ExportSettings) {
        _exportSettings.value = settings
    }

    fun startExport() {
        pause()
        _isExportComplete.value = false
        _exportProgress.value = 0.01f
        exportJob?.cancel()
        exportJob = viewModelScope.launch {
            val totalSteps = 40
            for (step in 1..totalSteps) {
                delay(60L)
                _exportProgress.value = step.toFloat() / totalSteps
            }
            _isExportComplete.value = true
        }
    }

    fun dismissExport() {
        exportJob?.cancel()
        _exportProgress.value = null
        _isExportComplete.value = false
        _activeSheet.value = ActiveSheet.NONE
    }

    fun deleteKeyframe(clipId: String, keyframeId: String) {
        pushHistory()
        mutateClip(clipId) { clip ->
            clip.copy(keyframes = clip.keyframes.filter { it.id != keyframeId })
        }
    }

    fun openProject(id: String) = loadProject(id)

    fun loadTemplate(template: Project) {
        pause()
        val newId = UUID.randomUUID().toString()
        val newProject = template.copy(id = newId, name = "${template.name} (Copy)")
        _currentProject.value = newProject
        _currentTimeMs.value = 0L
        _selectedClipId.value = newProject.tracks.flatMap { it.clips }.firstOrNull()?.id
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
        viewModelScope.launch { repository.saveProject(newProject) }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch { repository.deleteProject(id) }
    }

    fun togglePlayback() = togglePlay()

    fun toggleLoop() = toggleLooping()

    fun toggleSafeGuide() = toggleSafeGuideOverlay()

    fun toggleKeyframeAtCurrentTime() = addOrToggleKeyframeAtPlayhead()

    fun openSheet(sheet: ActiveSheet) = setActiveSheet(sheet)

    fun closeSheet() {
        _activeSheet.value = ActiveSheet.NONE
    }

    fun toggleEffectEnabled(clipId: String, effectId: String) = toggleEffect(clipId, effectId)

    fun removeEffectFromClip(clipId: String, effectId: String) = removeEffect(clipId, effectId)

    fun updateEffectParams(clipId: String, effectId: String, intensity: Float, p1: Float, p2: Float) =
        updateEffectParam(clipId, effectId, intensity, p1, p2)

    fun updateClipColorGrade(clipId: String, filter: FilterPresetType, exp: Float, con: Float, sat: Float, temp: Float, vig: Float) =
        updateClipColorGrading(clipId, filter, exp, con, sat, temp, vig)

    fun updateClipBlendMode(clipId: String, blendMode: BlendModeType) =
        updateClipTransform(clipId = clipId, blendMode = blendMode)

    fun addClipLayer(type: ClipType, title: String, text: String = "", shapeType: ShapeType = ShapeType.RECTANGLE) =
        addLayer(type, title, text, shapeType)

    fun startExportSimulation(settings: ExportSettings) {
        updateExportSettings(settings)
        startExport()
    }

    private fun mutateClip(clipId: String, mutation: (ClipItem) -> ClipItem) {
        val project = _currentProject.value ?: return
        val updatedTracks = project.tracks.map { track ->
            if (track.clips.any { it.id == clipId }) {
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == clipId) mutation(clip) else clip
                })
            } else track
        }
        val updatedProj = project.copy(tracks = updatedTracks, lastModified = System.currentTimeMillis())
        _currentProject.value = updatedProj
        viewModelScope.launch { repository.saveProject(updatedProj) }
    }
}
