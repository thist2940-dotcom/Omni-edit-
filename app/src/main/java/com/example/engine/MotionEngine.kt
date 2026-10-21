package com.example.engine

import androidx.compose.ui.graphics.BlendMode
import com.example.model.*
import kotlin.math.*

object MotionEngine {

    data class EvaluatedTransform(
        val posX: Float,
        val posY: Float,
        val scaleX: Float,
        val scaleY: Float,
        val rotationDeg: Float,
        val opacity: Float,
        val transitionFlashAlpha: Float = 0f,
        val rgbSplitOffset: Float = 0f,
        val glitchOffset: Float = 0f,
        val glowIntensity: Float = 0f,
        val waveWarpPhase: Float = 0f,
        val filmGrainNoise: Float = 0f,
        val cameraShakeX: Float = 0f,
        val cameraShakeY: Float = 0f,
        val cameraShakeRot: Float = 0f
    )

    fun evaluateClipTransform(clip: ClipItem, currentTimeMs: Long): EvaluatedTransform? {
        if (currentTimeMs < clip.startTimeMs || currentTimeMs > (clip.startTimeMs + clip.durationMs)) {
            return null
        }

        val localTime = currentTimeMs - clip.startTimeMs

        // Base property evaluations with Keyframes
        val posX = evaluateProperty(clip, KeyframeProperty.POSITION_X, clip.posX, currentTimeMs)
        val posY = evaluateProperty(clip, KeyframeProperty.POSITION_Y, clip.posY, currentTimeMs)
        val scale = evaluateProperty(clip, KeyframeProperty.SCALE, clip.scaleX, currentTimeMs)
        val rotation = evaluateProperty(clip, KeyframeProperty.ROTATION, clip.rotationDeg, currentTimeMs)
        var opacity = evaluateProperty(clip, KeyframeProperty.OPACITY, clip.opacity, currentTimeMs)

        var finalScaleX = scale * clip.scaleX
        var finalScaleY = scale * clip.scaleY
        var finalRotation = rotation
        var finalPosX = posX
        var finalPosY = posY
        var flashAlpha = 0f

        // Transitions at Start
        val transDuration = clip.transitionDurationMs.coerceAtLeast(100L)
        if (clip.transitionIn != TransitionType.NONE && localTime < transDuration) {
            val progress = (localTime.toFloat() / transDuration).coerceIn(0f, 1f)
            when (clip.transitionIn) {
                TransitionType.FADE_BLACK -> {
                    opacity *= progress
                }
                TransitionType.FLASH_WHITE -> {
                    flashAlpha = (1f - progress) * 0.9f
                }
                TransitionType.ZOOM_IN -> {
                    finalScaleX *= (0.3f + 0.7f * progress)
                    finalScaleY *= (0.3f + 0.7f * progress)
                    opacity *= progress
                }
                TransitionType.ZOOM_OUT -> {
                    finalScaleX *= (1.5f - 0.5f * progress)
                    finalScaleY *= (1.5f - 0.5f * progress)
                    opacity *= progress
                }
                TransitionType.WHIP_LEFT -> {
                    finalPosX += (1f - progress) * 400f
                }
                TransitionType.WHIP_RIGHT -> {
                    finalPosX -= (1f - progress) * 400f
                }
                TransitionType.GLITCH_BURST -> {
                    if (progress < 0.7f) {
                        finalPosX += sin(localTime * 0.08f) * 40f * (1f - progress)
                    }
                }
                TransitionType.IRIS_CLOSE -> {
                    finalScaleX *= progress
                    finalScaleY *= progress
                }
                TransitionType.NONE -> {}
            }
        }

        // Transitions at End
        val remainingTime = (clip.startTimeMs + clip.durationMs) - currentTimeMs
        if (clip.transitionOut != TransitionType.NONE && remainingTime < transDuration) {
            val progress = (remainingTime.toFloat() / transDuration).coerceIn(0f, 1f)
            when (clip.transitionOut) {
                TransitionType.FADE_BLACK -> {
                    opacity *= progress
                }
                TransitionType.FLASH_WHITE -> {
                    flashAlpha = max(flashAlpha, (1f - progress) * 0.9f)
                }
                TransitionType.ZOOM_OUT -> {
                    finalScaleX *= (0.3f + 0.7f * progress)
                    finalScaleY *= (0.3f + 0.7f * progress)
                    opacity *= progress
                }
                TransitionType.ZOOM_IN -> {
                    finalScaleX *= (1.5f - 0.5f * progress)
                    finalScaleY *= (1.5f - 0.5f * progress)
                    opacity *= progress
                }
                TransitionType.GLITCH_BURST -> {
                    finalPosX += sin(currentTimeMs * 0.1f) * 35f * (1f - progress)
                }
                TransitionType.WHIP_LEFT -> {
                    finalPosX -= (1f - progress) * 400f
                }
                TransitionType.WHIP_RIGHT -> {
                    finalPosX += (1f - progress) * 400f
                }
                TransitionType.IRIS_CLOSE -> {
                    finalScaleX *= progress
                    finalScaleY *= progress
                }
                TransitionType.NONE -> {}
            }
        }

        // Effects Evaluation
        var rgbSplit = 0f
        var glitchOffset = 0f
        var glowIntensity = 0f
        var wavePhase = 0f
        var filmGrain = 0f
        var shakeX = 0f
        var shakeY = 0f
        var shakeRot = 0f

        for (effect in clip.effects) {
            if (!effect.isEnabled) continue
            when (effect.type) {
                EffectType.RGB_SPLIT -> {
                    rgbSplit = effect.intensity * 24f * (0.8f + 0.2f * sin(currentTimeMs * 0.015f))
                }
                EffectType.GLITCH_SCANLINES -> {
                    val isSpike = (sin(currentTimeMs * 0.035f) > 0.65f)
                    if (isSpike) {
                        glitchOffset = effect.intensity * 30f * sin(currentTimeMs * 0.1f)
                    }
                }
                EffectType.DEEP_GLOW -> {
                    glowIntensity = effect.intensity
                }
                EffectType.WAVE_WARP -> {
                    wavePhase = (currentTimeMs * 0.006f * (effect.param1 + 0.5f)) % (2 * PI.toFloat())
                }
                EffectType.FILM_GRAIN -> {
                    filmGrain = effect.intensity
                }
                EffectType.CAMERA_SHAKE -> {
                    val freq = 0.02f * (effect.param1 * 2f + 0.5f)
                    val amp = effect.intensity * 35f
                    shakeX = (sin(currentTimeMs * freq) + sin(currentTimeMs * freq * 1.7f)) * amp * 0.5f
                    shakeY = (cos(currentTimeMs * freq * 1.3f) + cos(currentTimeMs * freq * 0.8f)) * amp * 0.5f
                    shakeRot = sin(currentTimeMs * freq * 0.9f) * effect.intensity * 5f
                }
                EffectType.LENS_DISTORTION -> {}
                EffectType.CHROMA_KEY -> {}
                EffectType.VIGNETTE -> {}
            }
        }

        return EvaluatedTransform(
            posX = finalPosX + shakeX,
            posY = finalPosY + shakeY,
            scaleX = finalScaleX,
            scaleY = finalScaleY,
            rotationDeg = finalRotation + shakeRot,
            opacity = opacity.coerceIn(0f, 1f),
            transitionFlashAlpha = flashAlpha,
            rgbSplitOffset = rgbSplit,
            glitchOffset = glitchOffset,
            glowIntensity = glowIntensity,
            waveWarpPhase = wavePhase,
            filmGrainNoise = filmGrain,
            cameraShakeX = shakeX,
            cameraShakeY = shakeY,
            cameraShakeRot = shakeRot
        )
    }

    private fun evaluateProperty(
        clip: ClipItem,
        property: KeyframeProperty,
        defaultValue: Float,
        currentTimeMs: Long
    ): Float {
        val matchingKeyframes = clip.keyframes.filter { it.property == property }.sortedBy { it.timeMs }
        if (matchingKeyframes.isEmpty()) return defaultValue

        if (currentTimeMs <= matchingKeyframes.first().timeMs) {
            return matchingKeyframes.first().value
        }
        if (currentTimeMs >= matchingKeyframes.last().timeMs) {
            return matchingKeyframes.last().value
        }

        for (i in 0 until matchingKeyframes.size - 1) {
            val k1 = matchingKeyframes[i]
            val k2 = matchingKeyframes[i + 1]
            if (currentTimeMs in k1.timeMs..k2.timeMs) {
                val duration = (k2.timeMs - k1.timeMs).toFloat().coerceAtLeast(1f)
                val linearProgress = ((currentTimeMs - k1.timeMs).toFloat() / duration).coerceIn(0f, 1f)
                val easedProgress = evaluateEasing(linearProgress, k1.easing, k1.cp1x, k1.cp1y, k1.cp2x, k1.cp2y)
                return k1.value + (k2.value - k1.value) * easedProgress
            }
        }

        return defaultValue
    }

    fun evaluateEasing(
        t: Float,
        easing: EasingCurveType,
        cp1x: Float,
        cp1y: Float,
        cp2x: Float,
        cp2y: Float
    ): Float {
        return when (easing) {
            EasingCurveType.LINEAR -> t
            EasingCurveType.EASE_IN -> t * t * t
            EasingCurveType.EASE_OUT -> 1f - (1f - t).pow(3)
            EasingCurveType.EASE_IN_OUT -> {
                if (t < 0.5f) 4f * t * t * t else 1f - (-2f * t + 2f).pow(3) / 2f
            }
            EasingCurveType.BEZIER_CUSTOM -> cubicBezier(t, cp1x, cp1y, cp2x, cp2y)
            EasingCurveType.BOUNCE -> {
                val n1 = 7.5625f
                val d1 = 2.75f
                var x = t
                when {
                    x < 1 / d1 -> n1 * x * x
                    x < 2 / d1 -> {
                        x -= 1.5f / d1
                        n1 * x * x + 0.75f
                    }
                    x < 2.5 / d1 -> {
                        x -= 2.25f / d1
                        n1 * x * x + 0.9375f
                    }
                    else -> {
                        x -= 2.625f / d1
                        n1 * x * x + 0.984375f
                    }
                }
            }
            EasingCurveType.ELASTIC -> {
                val c4 = (2 * PI / 3).toFloat()
                when {
                    t == 0f -> 0f
                    t == 1f -> 1f
                    else -> (-(2.0).pow(10.0 * (t - 1)).toFloat()) * sin((t * 10f - 10.75f) * c4)
                }
            }
        }
    }

    // Cubic Bézier calculation using Newton-Raphson approximation for parameter x -> y
    fun cubicBezier(t: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
        if (x1 == y1 && x2 == y2) return t
        var u = t
        // 4 iterations of Newton-Raphson
        for (i in 0 until 4) {
            val currentX = sampleCurveX(u, x1, x2)
            val dx = sampleCurveDerivativeX(u, x1, x2)
            if (abs(dx) < 1e-5f) break
            u -= (currentX - t) / dx
            u = u.coerceIn(0f, 1f)
        }
        return sampleCurveY(u, y1, y2)
    }

    private fun sampleCurveX(t: Float, x1: Float, x2: Float): Float {
        val cx = 3.0f * x1
        val bx = 3.0f * (x2 - x1) - cx
        val ax = 1.0f - cx - bx
        return ((ax * t + bx) * t + cx) * t
    }

    private fun sampleCurveY(t: Float, y1: Float, y2: Float): Float {
        val cy = 3.0f * y1
        val by = 3.0f * (y2 - y1) - cy
        val ay = 1.0f - cy - by
        return ((ay * t + by) * t + cy) * t
    }

    private fun sampleCurveDerivativeX(t: Float, x1: Float, x2: Float): Float {
        val cx = 3.0f * x1
        val bx = 3.0f * (x2 - x1) - cx
        val ax = 1.0f - cx - bx
        return (3.0f * ax * t + 2.0f * bx) * t + cx
    }

    fun mapBlendMode(blendMode: BlendModeType): BlendMode {
        return when (blendMode) {
            BlendModeType.NORMAL -> BlendMode.SrcOver
            BlendModeType.MULTIPLY -> BlendMode.Multiply
            BlendModeType.SCREEN -> BlendMode.Screen
            BlendModeType.OVERLAY -> BlendMode.Overlay
            BlendModeType.COLOR_DODGE -> BlendMode.ColorDodge
            BlendModeType.DIFFERENCE -> BlendMode.Difference
            BlendModeType.ADD -> BlendMode.Plus
            BlendModeType.DARKEN -> BlendMode.Darken
        }
    }
}
