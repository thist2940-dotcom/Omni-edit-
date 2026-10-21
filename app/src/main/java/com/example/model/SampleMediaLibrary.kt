package com.example.model

import java.util.UUID

object SampleMediaLibrary {

    data class StockAsset(
        val id: String,
        val title: String,
        val type: ClipType,
        val durationMs: Long,
        val primaryColorHex: String,
        val secondaryColorHex: String,
        val iconLabel: String
    )

    val STOCK_VIDEOS = listOf(
        StockAsset("cyber_neon", "Cyberpunk Neon City", ClipType.VIDEO, 12000L, "#7C3AED", "#EC4899", "Neon Street"),
        StockAsset("sunset_wave", "Sunset Ocean Waves", ClipType.VIDEO, 15000L, "#F59E0B", "#EF4444", "Golden Coast"),
        StockAsset("mountain_drone", "Alpine Drone Flythrough", ClipType.VIDEO, 10000L, "#0D9488", "#0284C7", "Glacier Ridge"),
        StockAsset("abstract_tunnel", "Cosmic Hyper Tunnel", ClipType.VIDEO, 14000L, "#3B82F6", "#8B5CF6", "Warp Field"),
        StockAsset("urban_skater", "Tokyo Night Urban Drift", ClipType.VIDEO, 10000L, "#10B981", "#3B82F6", "City Motion")
    )

    val STOCK_AUDIO = listOf(
        StockAsset("bgm_synthwave", "Cyberpunk Synth Odyssey", ClipType.AUDIO, 15000L, "#8B5CF6", "#06B6D4", "128 BPM Synth"),
        StockAsset("bgm_lofi", "Midnight Lo-Fi Coffee", ClipType.AUDIO, 18000L, "#D97706", "#92400E", "85 BPM Chill"),
        StockAsset("sfx_whoosh", "Cinematic Sub Bass Swoosh", ClipType.AUDIO, 2000L, "#EF4444", "#F59E0B", "SFX Impact"),
        StockAsset("sfx_glitch", "Digital Glitch & Static Burst", ClipType.AUDIO, 1500L, "#06B6D4", "#3B82F6", "SFX Glitch"),
        StockAsset("sfx_camera", "Fast Shutter Click Click", ClipType.AUDIO, 1000L, "#10B981", "#059669", "SFX Click"),
        StockAsset("sfx_riser", "Tension Tension Riser Build", ClipType.AUDIO, 4000L, "#F43F5E", "#881337", "SFX Riser")
    )

    fun createCyberpunkTemplate(): Project {
        val videoTrackId = UUID.randomUUID().toString()
        val textTrackId = UUID.randomUUID().toString()
        val shapeTrackId = UUID.randomUUID().toString()
        val audioTrackId = UUID.randomUUID().toString()
        val fxTrackId = UUID.randomUUID().toString()

        val mainVideoClip = ClipItem(
            id = UUID.randomUUID().toString(),
            trackId = videoTrackId,
            startTimeMs = 0L,
            durationMs = 9000L,
            clipType = ClipType.VIDEO,
            title = "Cyberpunk Neon City",
            assetId = "cyber_neon",
            assetColorHex = "#7C3AED",
            filterPreset = FilterPresetType.CYBERPUNK_NEON,
            contrast = 1.3f,
            saturation = 1.4f,
            vignette = 0.35f,
            transitionIn = TransitionType.FLASH_WHITE,
            transitionOut = TransitionType.GLITCH_BURST,
            effects = listOf(
                EffectInstance(type = EffectType.RGB_SPLIT, intensity = 0.65f, param1 = 0.7f),
                EffectInstance(type = EffectType.DEEP_GLOW, intensity = 0.8f, param1 = 0.6f)
            )
        )

        val animatedTextClip = ClipItem(
            id = UUID.randomUUID().toString(),
            trackId = textTrackId,
            startTimeMs = 800L,
            durationMs = 6000L,
            clipType = ClipType.TEXT,
            title = "Title Overlay",
            text = "CYBERPUNK // 2088",
            fontSizeSp = 34f,
            fontColorHex = "#00F0FF",
            textBgColorHex = "#120B24CC",
            posY = -40f,
            keyframes = listOf(
                KeyframePoint(timeMs = 800L, property = KeyframeProperty.SCALE, value = 0.4f, easing = EasingCurveType.BOUNCE),
                KeyframePoint(timeMs = 1800L, property = KeyframeProperty.SCALE, value = 1.0f, easing = EasingCurveType.EASE_OUT),
                KeyframePoint(timeMs = 4500L, property = KeyframeProperty.ROTATION, value = 0f, easing = EasingCurveType.EASE_IN_OUT),
                KeyframePoint(timeMs = 5500L, property = KeyframeProperty.ROTATION, value = 6f, easing = EasingCurveType.EASE_IN_OUT)
            ),
            effects = listOf(
                EffectInstance(type = EffectType.GLITCH_SCANLINES, intensity = 0.75f, param1 = 0.8f)
            )
        )

        val vectorShapeClip = ClipItem(
            id = UUID.randomUUID().toString(),
            trackId = shapeTrackId,
            startTimeMs = 1200L,
            durationMs = 5000L,
            clipType = ClipType.SHAPE,
            title = "HUD Diamond",
            shapeType = ShapeType.STAR,
            shapeFillHex = "#FF007F",
            shapeStrokeHex = "#00F0FF",
            shapeStrokeWidthDp = 3f,
            scaleX = 0.7f,
            scaleY = 0.7f,
            blendMode = BlendModeType.SCREEN,
            keyframes = listOf(
                KeyframePoint(timeMs = 1200L, property = KeyframeProperty.ROTATION, value = 0f, easing = EasingCurveType.LINEAR),
                KeyframePoint(timeMs = 6200L, property = KeyframeProperty.ROTATION, value = 360f, easing = EasingCurveType.LINEAR)
            )
        )

        val audioClip = ClipItem(
            id = UUID.randomUUID().toString(),
            trackId = audioTrackId,
            startTimeMs = 0L,
            durationMs = 9000L,
            clipType = ClipType.AUDIO,
            title = "Cyber Synthwave Beat",
            assetId = "bgm_synthwave",
            assetColorHex = "#8B5CF6",
            volume = 0.9f,
            fadeInMs = 600L,
            fadeOutMs = 1200L
        )

        val fxAdjustmentClip = ClipItem(
            id = UUID.randomUUID().toString(),
            trackId = fxTrackId,
            startTimeMs = 2000L,
            durationMs = 4500L,
            clipType = ClipType.ADJUSTMENT,
            title = "AE Camera Shake & Grain",
            effects = listOf(
                EffectInstance(type = EffectType.CAMERA_SHAKE, intensity = 0.6f, param1 = 0.5f),
                EffectInstance(type = EffectType.FILM_GRAIN, intensity = 0.45f, param1 = 0.8f)
            )
        )

        return Project(
            name = "Cyberpunk Glitch Reel",
            aspectRatio = AspectRatioType.VERTICAL_9_16,
            durationMs = 9000L,
            tracks = listOf(
                Track(id = videoTrackId, type = TrackType.VIDEO, name = "V1: Base Video", clips = listOf(mainVideoClip)),
                Track(id = shapeTrackId, type = TrackType.SHAPE, name = "V2: Vector Elements", clips = listOf(vectorShapeClip)),
                Track(id = textTrackId, type = TrackType.TEXT, name = "V3: Kinetic Typography", clips = listOf(animatedTextClip)),
                Track(id = fxTrackId, type = TrackType.EFFECT, name = "FX: Adjustment Layer", clips = listOf(fxAdjustmentClip)),
                Track(id = audioTrackId, type = TrackType.AUDIO, name = "A1: Soundtrack", clips = listOf(audioClip))
            )
        )
    }

    fun createAlightMotionKineticTemplate(): Project {
        val videoTrackId = UUID.randomUUID().toString()
        val textTrackId = UUID.randomUUID().toString()
        val shapeTrackId = UUID.randomUUID().toString()
        val audioTrackId = UUID.randomUUID().toString()

        val bgClip = ClipItem(
            id = UUID.randomUUID().toString(),
            trackId = videoTrackId,
            startTimeMs = 0L,
            durationMs = 8000L,
            clipType = ClipType.VIDEO,
            title = "Cosmic Warp Field",
            assetId = "abstract_tunnel",
            assetColorHex = "#3B82F6",
            filterPreset = FilterPresetType.ANIME_VIBRANT,
            saturation = 1.6f
        )

        val rotatingRing = ClipItem(
            id = UUID.randomUUID().toString(),
            trackId = shapeTrackId,
            startTimeMs = 500L,
            durationMs = 7000L,
            clipType = ClipType.SHAPE,
            title = "Bézier Pulsing Star",
            shapeType = ShapeType.STAR,
            shapeFillHex = "#EC4899",
            shapeStrokeHex = "#FBBF24",
            shapeStrokeWidthDp = 4f,
            blendMode = BlendModeType.ADD,
            keyframes = listOf(
                KeyframePoint(timeMs = 500L, property = KeyframeProperty.SCALE, value = 0.2f, easing = EasingCurveType.BEZIER_CUSTOM, cp1x = 0.17f, cp1y = 0.67f, cp2x = 0.12f, cp2y = 1.25f),
                KeyframePoint(timeMs = 2500L, property = KeyframeProperty.SCALE, value = 1.3f, easing = EasingCurveType.BEZIER_CUSTOM, cp1x = 0.25f, cp1y = 0.1f, cp2x = 0.25f, cp2y = 1.0f),
                KeyframePoint(timeMs = 4500L, property = KeyframeProperty.SCALE, value = 0.9f, easing = EasingCurveType.BEZIER_CUSTOM),
                KeyframePoint(timeMs = 500L, property = KeyframeProperty.ROTATION, value = 0f, easing = EasingCurveType.EASE_IN_OUT),
                KeyframePoint(timeMs = 6500L, property = KeyframeProperty.ROTATION, value = 720f, easing = EasingCurveType.EASE_IN_OUT)
            )
        )

        val kineticText = ClipItem(
            id = UUID.randomUUID().toString(),
            trackId = textTrackId,
            startTimeMs = 1000L,
            durationMs = 6000L,
            clipType = ClipType.TEXT,
            title = "Kinetic Title",
            text = "ALIGHT MOTION // CURVES",
            fontSizeSp = 30f,
            fontColorHex = "#FFFFFF",
            posY = 50f,
            keyframes = listOf(
                KeyframePoint(timeMs = 1000L, property = KeyframeProperty.POSITION_Y, value = 150f, easing = EasingCurveType.ELASTIC),
                KeyframePoint(timeMs = 2200L, property = KeyframeProperty.POSITION_Y, value = 30f, easing = EasingCurveType.EASE_OUT),
                KeyframePoint(timeMs = 4000L, property = KeyframeProperty.SCALE, value = 1.0f, easing = EasingCurveType.BOUNCE),
                KeyframePoint(timeMs = 5000L, property = KeyframeProperty.SCALE, value = 1.25f, easing = EasingCurveType.BOUNCE)
            ),
            effects = listOf(
                EffectInstance(type = EffectType.WAVE_WARP, intensity = 0.5f, param1 = 0.6f)
            )
        )

        val audioClip = ClipItem(
            id = UUID.randomUUID().toString(),
            trackId = audioTrackId,
            startTimeMs = 0L,
            durationMs = 8000L,
            clipType = ClipType.AUDIO,
            title = "Whoosh & Swoosh Riser",
            assetId = "sfx_whoosh",
            assetColorHex = "#EC4899",
            volume = 1.0f
        )

        return Project(
            name = "Alight Kinetic Typography",
            aspectRatio = AspectRatioType.VERTICAL_9_16,
            durationMs = 8000L,
            tracks = listOf(
                Track(id = videoTrackId, type = TrackType.VIDEO, name = "V1: Background", clips = listOf(bgClip)),
                Track(id = shapeTrackId, type = TrackType.SHAPE, name = "V2: Vector Shapes", clips = listOf(rotatingRing)),
                Track(id = textTrackId, type = TrackType.TEXT, name = "V3: Animated Typography", clips = listOf(kineticText)),
                Track(id = audioTrackId, type = TrackType.AUDIO, name = "A1: Audio Track", clips = listOf(audioClip))
            )
        )
    }

    fun createCinematicVlogTemplate(): Project {
        val videoTrackId = UUID.randomUUID().toString()
        val textTrackId = UUID.randomUUID().toString()
        val audioTrackId = UUID.randomUUID().toString()

        val clipA = ClipItem(
            id = UUID.randomUUID().toString(),
            trackId = videoTrackId,
            startTimeMs = 0L,
            durationMs = 5000L,
            clipType = ClipType.VIDEO,
            title = "Ocean Sunrise",
            assetId = "sunset_wave",
            assetColorHex = "#F59E0B",
            filterPreset = FilterPresetType.TEAL_ORANGE,
            contrast = 1.15f,
            saturation = 1.25f,
            temperature = 0.35f,
            vignette = 0.4f,
            transitionOut = TransitionType.WHIP_LEFT,
            transitionDurationMs = 800L,
            speedPreset = SpeedPreset.HERO,
            effects = listOf(
                EffectInstance(type = EffectType.DEEP_GLOW, intensity = 0.5f)
            )
        )

        val clipB = ClipItem(
            id = UUID.randomUUID().toString(),
            trackId = videoTrackId,
            startTimeMs = 4500L,
            durationMs = 5500L,
            clipType = ClipType.VIDEO,
            title = "Glacier Drone",
            assetId = "mountain_drone",
            assetColorHex = "#0D9488",
            filterPreset = FilterPresetType.EMERALD_CINEMA,
            contrast = 1.2f,
            saturation = 1.2f,
            vignette = 0.3f,
            transitionIn = TransitionType.WHIP_RIGHT,
            speedPreset = SpeedPreset.SLOW_MO,
            effects = listOf(
                EffectInstance(type = EffectType.FILM_GRAIN, intensity = 0.4f)
            )
        )

        val titleClip = ClipItem(
            id = UUID.randomUUID().toString(),
            trackId = textTrackId,
            startTimeMs = 1000L,
            durationMs = 4000L,
            clipType = ClipType.TEXT,
            title = "Cinematic Title",
            text = "WANDERLUST // EP. 01",
            fontSizeSp = 32f,
            fontColorHex = "#FFFFFF",
            fontStyle = "Light Tracking",
            keyframes = listOf(
                KeyframePoint(timeMs = 1000L, property = KeyframeProperty.OPACITY, value = 0f, easing = EasingCurveType.EASE_IN),
                KeyframePoint(timeMs = 2000L, property = KeyframeProperty.OPACITY, value = 1f, easing = EasingCurveType.EASE_OUT),
                KeyframePoint(timeMs = 4000L, property = KeyframeProperty.OPACITY, value = 1f, easing = EasingCurveType.EASE_IN),
                KeyframePoint(timeMs = 5000L, property = KeyframeProperty.OPACITY, value = 0f, easing = EasingCurveType.EASE_OUT)
            )
        )

        val audioClip = ClipItem(
            id = UUID.randomUUID().toString(),
            trackId = audioTrackId,
            startTimeMs = 0L,
            durationMs = 10000L,
            clipType = ClipType.AUDIO,
            title = "Midnight Lo-Fi Coffee",
            assetId = "bgm_lofi",
            assetColorHex = "#D97706",
            volume = 0.85f,
            fadeInMs = 1000L,
            fadeOutMs = 1500L
        )

        return Project(
            name = "Cinematic Travel Vlog",
            aspectRatio = AspectRatioType.WIDE_16_9,
            durationMs = 10000L,
            tracks = listOf(
                Track(id = videoTrackId, type = TrackType.VIDEO, name = "V1: Main Footage", clips = listOf(clipA, clipB)),
                Track(id = textTrackId, type = TrackType.TEXT, name = "V2: Subtitles & Title", clips = listOf(titleClip)),
                Track(id = audioTrackId, type = TrackType.AUDIO, name = "A1: Soundtrack", clips = listOf(audioClip))
            )
        )
    }

    val ALL_TEMPLATES by lazy {
        listOf(
            createCyberpunkTemplate(),
            createAlightMotionKineticTemplate(),
            createCinematicVlogTemplate()
        )
    }
}
