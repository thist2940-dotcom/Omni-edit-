package com.example.model

import java.util.UUID

enum class AspectRatioType(val label: String, val ratio: Float, val iconDescription: String) {
    VERTICAL_9_16("9:16 (TikTok/Reels)", 9f / 16f, "Vertical"),
    WIDE_16_9("16:9 (YouTube/Landscape)", 16f / 9f, "Widescreen"),
    SQUARE_1_1("1:1 (Instagram Post)", 1f, "Square"),
    PORTRAIT_4_5("4:5 (Feed)", 4f / 5f, "Portrait"),
    CINEMA_21_9("21:9 (Cinematic Scope)", 21f / 9f, "Ultrawide")
}

enum class TrackType(val label: String) {
    VIDEO("Main Video"),
    OVERLAY("PIP / Overlay"),
    TEXT("Text & Titles"),
    SHAPE("Vector Shapes"),
    AUDIO("Audio & BGM"),
    EFFECT("Adjustment / FX")
}

enum class ClipType {
    VIDEO,
    AUDIO,
    TEXT,
    SHAPE,
    ADJUSTMENT
}

enum class BlendModeType(val label: String) {
    NORMAL("Normal"),
    MULTIPLY("Multiply"),
    SCREEN("Screen"),
    OVERLAY("Overlay"),
    COLOR_DODGE("Color Dodge"),
    DIFFERENCE("Difference"),
    ADD("Add / Lighten"),
    DARKEN("Darken")
}

enum class FilterPresetType(val label: String) {
    NONE("Normal"),
    CYBERPUNK_NEON("Cyberpunk Neon"),
    TEAL_ORANGE("Teal & Orange"),
    VINTAGE_VHS("Vintage 90s VHS"),
    NOIR_BW("Classic Noir B&W"),
    ANIME_VIBRANT("Anime Vibrant"),
    WARM_SUNSET("Golden Sunset"),
    EMERALD_CINEMA("Emerald Film")
}

enum class TransitionType(val label: String) {
    NONE("None"),
    FADE_BLACK("Fade Black"),
    FLASH_WHITE("Flash White"),
    ZOOM_IN("Zoom In"),
    ZOOM_OUT("Zoom Out"),
    GLITCH_BURST("Glitch Burst"),
    WHIP_LEFT("Whip Left"),
    WHIP_RIGHT("Whip Right"),
    IRIS_CLOSE("Iris Wipe")
}

enum class EffectType(val label: String, val category: String) {
    RGB_SPLIT("RGB Chromatic Aberration", "Distortion"),
    GLITCH_SCANLINES("Glitch & Scanlines", "Stylize"),
    DEEP_GLOW("Deep Bloom Glow", "Light & Flare"),
    WAVE_WARP("Wave Warp & Ripple", "Distortion"),
    FILM_GRAIN("35mm Film Grain", "Stylize"),
    CHROMA_KEY("Chroma Key (Green Screen)", "Matte"),
    LENS_DISTORTION("Fisheye Lens", "Distortion"),
    CAMERA_SHAKE("After Effects Shake / Wiggle", "Motion"),
    VIGNETTE("Cinematic Vignette", "Color")
}

enum class EasingCurveType(val label: String) {
    LINEAR("Linear"),
    EASE_IN("Ease In"),
    EASE_OUT("Ease Out"),
    EASE_IN_OUT("Ease In-Out (Smooth)"),
    BEZIER_CUSTOM("Custom Bézier Graph"),
    BOUNCE("Bounce"),
    ELASTIC("Elastic Spring")
}

enum class KeyframeProperty(val label: String) {
    POSITION_X("Position X"),
    POSITION_Y("Position Y"),
    SCALE("Scale"),
    ROTATION("Rotation"),
    OPACITY("Opacity")
}

enum class SpeedPreset(val label: String, val speed: Float) {
    NORMAL_1X("1.0x Normal", 1.0f),
    SLOW_MO("0.5x Slow-Mo", 0.5f),
    EXTRA_SLOW("0.2x Ultra Slow", 0.2f),
    FAST_2X("2.0x Fast", 2.0f),
    FAST_4X("4.0x Hyper", 4.0f),
    FLASH_IN("Flash In (Fast -> Slow)", 1.0f),
    FLASH_OUT("Flash Out (Slow -> Fast)", 1.0f),
    MONTAGE("Montage Wave Curve", 1.0f),
    HERO("Hero Speed Ramp", 1.0f)
}

enum class ShapeType(val label: String) {
    RECTANGLE("Rectangle"),
    CIRCLE("Circle"),
    STAR("Star"),
    TRIANGLE("Triangle"),
    WAVE("Wave Ribbon"),
    HEART("Heart")
}

enum class VoicePitchPreset(val label: String) {
    NORMAL("Normal"),
    CHIPMUNK("High Pitch / Chipmunk"),
    DEEP_BASS("Deep Voice"),
    ROBOT("Robotic Vocoder"),
    ECHO_REVERB("Concert Reverb")
}

data class KeyframePoint(
    val id: String = UUID.randomUUID().toString(),
    val timeMs: Long,
    val property: KeyframeProperty,
    val value: Float,
    val easing: EasingCurveType = EasingCurveType.EASE_IN_OUT,
    val cp1x: Float = 0.42f,
    val cp1y: Float = 0.0f,
    val cp2x: Float = 0.58f,
    val cp2y: Float = 1.0f
)

data class EffectInstance(
    val id: String = UUID.randomUUID().toString(),
    val type: EffectType,
    val isEnabled: Boolean = true,
    val intensity: Float = 0.5f,
    val param1: Float = 0.5f,
    val param2: Float = 0.5f
)

data class ClipItem(
    val id: String = UUID.randomUUID().toString(),
    val trackId: String,
    val startTimeMs: Long,
    val durationMs: Long,
    val sourceOffsetMs: Long = 0L,
    val clipType: ClipType,
    val title: String,

    // Visual media / Text / Shape payloads
    val text: String = "Double tap to edit",
    val fontStyle: String = "Bold",
    val fontSizeSp: Float = 28f,
    val fontColorHex: String = "#FFFFFF",
    val textBgColorHex: String = "#00000088",
    val textAnimation: String = "Fade In",

    val shapeType: ShapeType = ShapeType.RECTANGLE,
    val shapeFillHex: String = "#8B5CF6",
    val shapeStrokeHex: String = "#FFFFFF",
    val shapeStrokeWidthDp: Float = 2f,
    val shapeCornerRadiusDp: Float = 12f,

    val assetId: String = "cyber_neon",
    val assetColorHex: String = "#6366F1",

    // Audio properties
    val volume: Float = 1.0f,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val pitchPreset: VoicePitchPreset = VoicePitchPreset.NORMAL,

    // Speed & Transitions
    val speed: Float = 1.0f,
    val speedPreset: SpeedPreset = SpeedPreset.NORMAL_1X,
    val transitionIn: TransitionType = TransitionType.NONE,
    val transitionOut: TransitionType = TransitionType.NONE,
    val transitionDurationMs: Long = 500L,

    // Default static transforms (can be animated via keyframes)
    val posX: Float = 0f,
    val posY: Float = 0f,
    val scaleX: Float = 1.0f,
    val scaleY: Float = 1.0f,
    val rotationDeg: Float = 0f,
    val opacity: Float = 1.0f,
    val blendMode: BlendModeType = BlendModeType.NORMAL,

    // Color grading
    val filterPreset: FilterPresetType = FilterPresetType.NONE,
    val exposure: Float = 0f,    // -1f..1f
    val contrast: Float = 1f,    // 0.5f..2f
    val saturation: Float = 1f,  // 0f..2f
    val temperature: Float = 0f, // -1f..1f (Cool to Warm)
    val vignette: Float = 0f,    // 0f..1f

    // Motion & FX engines
    val keyframes: List<KeyframePoint> = emptyList(),
    val effects: List<EffectInstance> = emptyList()
)

data class Track(
    val id: String = UUID.randomUUID().toString(),
    val type: TrackType,
    val name: String,
    val isMuted: Boolean = false,
    val isLocked: Boolean = false,
    val isVisible: Boolean = true,
    val clips: List<ClipItem> = emptyList()
)

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val aspectRatio: AspectRatioType = AspectRatioType.VERTICAL_9_16,
    val width: Int = 1080,
    val height: Int = 1920,
    val fps: Int = 30,
    val durationMs: Long = 10000L,
    val tracks: List<Track> = emptyList(),
    val lastModified: Long = System.currentTimeMillis()
)

data class ExportSettings(
    val resolutionName: String = "1080p FHD",
    val width: Int = 1080,
    val height: Int = 1920,
    val fps: Int = 30,
    val bitrateName: String = "High (16 Mbps)",
    val format: String = "MP4 (H.264 / AAC)",
    val filename: String = "MotionFX_Export.mp4"
)
