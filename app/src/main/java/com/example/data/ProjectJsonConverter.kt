package com.example.data

import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject

object ProjectJsonConverter {

    fun toJson(project: Project): String {
        val root = JSONObject()
        root.put("id", project.id)
        root.put("name", project.name)
        root.put("aspectRatio", project.aspectRatio.name)
        root.put("width", project.width)
        root.put("height", project.height)
        root.put("fps", project.fps)
        root.put("durationMs", project.durationMs)
        root.put("lastModified", project.lastModified)

        val tracksArray = JSONArray()
        for (track in project.tracks) {
            val trackObj = JSONObject()
            trackObj.put("id", track.id)
            trackObj.put("type", track.type.name)
            trackObj.put("name", track.name)
            trackObj.put("isMuted", track.isMuted)
            trackObj.put("isLocked", track.isLocked)
            trackObj.put("isVisible", track.isVisible)

            val clipsArray = JSONArray()
            for (clip in track.clips) {
                val clipObj = JSONObject()
                clipObj.put("id", clip.id)
                clipObj.put("trackId", clip.trackId)
                clipObj.put("startTimeMs", clip.startTimeMs)
                clipObj.put("durationMs", clip.durationMs)
                clipObj.put("sourceOffsetMs", clip.sourceOffsetMs)
                clipObj.put("clipType", clip.clipType.name)
                clipObj.put("title", clip.title)

                clipObj.put("text", clip.text)
                clipObj.put("fontStyle", clip.fontStyle)
                clipObj.put("fontSizeSp", clip.fontSizeSp.toDouble())
                clipObj.put("fontColorHex", clip.fontColorHex)
                clipObj.put("textBgColorHex", clip.textBgColorHex)
                clipObj.put("textAnimation", clip.textAnimation)

                clipObj.put("shapeType", clip.shapeType.name)
                clipObj.put("shapeFillHex", clip.shapeFillHex)
                clipObj.put("shapeStrokeHex", clip.shapeStrokeHex)
                clipObj.put("shapeStrokeWidthDp", clip.shapeStrokeWidthDp.toDouble())
                clipObj.put("shapeCornerRadiusDp", clip.shapeCornerRadiusDp.toDouble())

                clipObj.put("assetId", clip.assetId)
                clipObj.put("assetColorHex", clip.assetColorHex)

                clipObj.put("volume", clip.volume.toDouble())
                clipObj.put("fadeInMs", clip.fadeInMs)
                clipObj.put("fadeOutMs", clip.fadeOutMs)
                clipObj.put("pitchPreset", clip.pitchPreset.name)

                clipObj.put("speed", clip.speed.toDouble())
                clipObj.put("speedPreset", clip.speedPreset.name)
                clipObj.put("transitionIn", clip.transitionIn.name)
                clipObj.put("transitionOut", clip.transitionOut.name)
                clipObj.put("transitionDurationMs", clip.transitionDurationMs)

                clipObj.put("posX", clip.posX.toDouble())
                clipObj.put("posY", clip.posY.toDouble())
                clipObj.put("scaleX", clip.scaleX.toDouble())
                clipObj.put("scaleY", clip.scaleY.toDouble())
                clipObj.put("rotationDeg", clip.rotationDeg.toDouble())
                clipObj.put("opacity", clip.opacity.toDouble())
                clipObj.put("blendMode", clip.blendMode.name)

                clipObj.put("filterPreset", clip.filterPreset.name)
                clipObj.put("exposure", clip.exposure.toDouble())
                clipObj.put("contrast", clip.contrast.toDouble())
                clipObj.put("saturation", clip.saturation.toDouble())
                clipObj.put("temperature", clip.temperature.toDouble())
                clipObj.put("vignette", clip.vignette.toDouble())

                val kfArray = JSONArray()
                for (kf in clip.keyframes) {
                    val kfObj = JSONObject()
                    kfObj.put("id", kf.id)
                    kfObj.put("timeMs", kf.timeMs)
                    kfObj.put("property", kf.property.name)
                    kfObj.put("value", kf.value.toDouble())
                    kfObj.put("easing", kf.easing.name)
                    kfObj.put("cp1x", kf.cp1x.toDouble())
                    kfObj.put("cp1y", kf.cp1y.toDouble())
                    kfObj.put("cp2x", kf.cp2x.toDouble())
                    kfObj.put("cp2y", kf.cp2y.toDouble())
                    kfArray.put(kfObj)
                }
                clipObj.put("keyframes", kfArray)

                val effArray = JSONArray()
                for (eff in clip.effects) {
                    val effObj = JSONObject()
                    effObj.put("id", eff.id)
                    effObj.put("type", eff.type.name)
                    effObj.put("isEnabled", eff.isEnabled)
                    effObj.put("intensity", eff.intensity.toDouble())
                    effObj.put("param1", eff.param1.toDouble())
                    effObj.put("param2", eff.param2.toDouble())
                    effArray.put(effObj)
                }
                clipObj.put("effects", effArray)

                clipsArray.put(clipObj)
            }
            trackObj.put("clips", clipsArray)
            tracksArray.put(trackObj)
        }
        root.put("tracks", tracksArray)

        return root.toString()
    }

    fun fromJson(jsonStr: String): Project {
        val root = JSONObject(jsonStr)
        val id = root.optString("id")
        val name = root.optString("name", "Untitled Project")
        val aspectName = root.optString("aspectRatio", AspectRatioType.VERTICAL_9_16.name)
        val aspectRatio = runCatching { AspectRatioType.valueOf(aspectName) }.getOrDefault(AspectRatioType.VERTICAL_9_16)
        val width = root.optInt("width", 1080)
        val height = root.optInt("height", 1920)
        val fps = root.optInt("fps", 30)
        val durationMs = root.optLong("durationMs", 10000L)
        val lastModified = root.optLong("lastModified", System.currentTimeMillis())

        val tracksList = mutableListOf<Track>()
        val tracksArray = root.optJSONArray("tracks") ?: JSONArray()
        for (i in 0 until tracksArray.length()) {
            val trackObj = tracksArray.getJSONObject(i)
            val trackId = trackObj.optString("id")
            val trackTypeName = trackObj.optString("type", TrackType.VIDEO.name)
            val trackType = runCatching { TrackType.valueOf(trackTypeName) }.getOrDefault(TrackType.VIDEO)
            val trackName = trackObj.optString("name", "Track")
            val isMuted = trackObj.optBoolean("isMuted", false)
            val isLocked = trackObj.optBoolean("isLocked", false)
            val isVisible = trackObj.optBoolean("isVisible", true)

            val clipsList = mutableListOf<ClipItem>()
            val clipsArray = trackObj.optJSONArray("clips") ?: JSONArray()
            for (j in 0 until clipsArray.length()) {
                val clipObj = clipsArray.getJSONObject(j)
                val clipId = clipObj.optString("id")
                val clipTypeName = clipObj.optString("clipType", ClipType.VIDEO.name)
                val clipType = runCatching { ClipType.valueOf(clipTypeName) }.getOrDefault(ClipType.VIDEO)

                val kfList = mutableListOf<KeyframePoint>()
                val kfArray = clipObj.optJSONArray("keyframes") ?: JSONArray()
                for (k in 0 until kfArray.length()) {
                    val kfObj = kfArray.getJSONObject(k)
                    val propName = kfObj.optString("property", KeyframeProperty.SCALE.name)
                    val prop = runCatching { KeyframeProperty.valueOf(propName) }.getOrDefault(KeyframeProperty.SCALE)
                    val easingName = kfObj.optString("easing", EasingCurveType.EASE_IN_OUT.name)
                    val easing = runCatching { EasingCurveType.valueOf(easingName) }.getOrDefault(EasingCurveType.EASE_IN_OUT)
                    kfList.add(
                        KeyframePoint(
                            id = kfObj.optString("id"),
                            timeMs = kfObj.optLong("timeMs"),
                            property = prop,
                            value = kfObj.optDouble("value", 1.0).toFloat(),
                            easing = easing,
                            cp1x = kfObj.optDouble("cp1x", 0.42).toFloat(),
                            cp1y = kfObj.optDouble("cp1y", 0.0).toFloat(),
                            cp2x = kfObj.optDouble("cp2x", 0.58).toFloat(),
                            cp2y = kfObj.optDouble("cp2y", 1.0).toFloat()
                        )
                    )
                }

                val effList = mutableListOf<EffectInstance>()
                val effArray = clipObj.optJSONArray("effects") ?: JSONArray()
                for (e in 0 until effArray.length()) {
                    val effObj = effArray.getJSONObject(e)
                    val effName = effObj.optString("type", EffectType.RGB_SPLIT.name)
                    val effType = runCatching { EffectType.valueOf(effName) }.getOrDefault(EffectType.RGB_SPLIT)
                    effList.add(
                        EffectInstance(
                            id = effObj.optString("id"),
                            type = effType,
                            isEnabled = effObj.optBoolean("isEnabled", true),
                            intensity = effObj.optDouble("intensity", 0.5).toFloat(),
                            param1 = effObj.optDouble("param1", 0.5).toFloat(),
                            param2 = effObj.optDouble("param2", 0.5).toFloat()
                        )
                    )
                }

                val shapeName = clipObj.optString("shapeType", ShapeType.RECTANGLE.name)
                val shapeType = runCatching { ShapeType.valueOf(shapeName) }.getOrDefault(ShapeType.RECTANGLE)
                val pitchName = clipObj.optString("pitchPreset", VoicePitchPreset.NORMAL.name)
                val pitch = runCatching { VoicePitchPreset.valueOf(pitchName) }.getOrDefault(VoicePitchPreset.NORMAL)
                val speedPresetName = clipObj.optString("speedPreset", SpeedPreset.NORMAL_1X.name)
                val speedPreset = runCatching { SpeedPreset.valueOf(speedPresetName) }.getOrDefault(SpeedPreset.NORMAL_1X)
                val transInName = clipObj.optString("transitionIn", TransitionType.NONE.name)
                val transIn = runCatching { TransitionType.valueOf(transInName) }.getOrDefault(TransitionType.NONE)
                val transOutName = clipObj.optString("transitionOut", TransitionType.NONE.name)
                val transOut = runCatching { TransitionType.valueOf(transOutName) }.getOrDefault(TransitionType.NONE)
                val blendModeName = clipObj.optString("blendMode", BlendModeType.NORMAL.name)
                val blendMode = runCatching { BlendModeType.valueOf(blendModeName) }.getOrDefault(BlendModeType.NORMAL)
                val filterName = clipObj.optString("filterPreset", FilterPresetType.NONE.name)
                val filter = runCatching { FilterPresetType.valueOf(filterName) }.getOrDefault(FilterPresetType.NONE)

                clipsList.add(
                    ClipItem(
                        id = clipId,
                        trackId = trackId,
                        startTimeMs = clipObj.optLong("startTimeMs", 0L),
                        durationMs = clipObj.optLong("durationMs", 3000L),
                        sourceOffsetMs = clipObj.optLong("sourceOffsetMs", 0L),
                        clipType = clipType,
                        title = clipObj.optString("title", "Clip"),
                        text = clipObj.optString("text", "Text"),
                        fontStyle = clipObj.optString("fontStyle", "Bold"),
                        fontSizeSp = clipObj.optDouble("fontSizeSp", 28.0).toFloat(),
                        fontColorHex = clipObj.optString("fontColorHex", "#FFFFFF"),
                        textBgColorHex = clipObj.optString("textBgColorHex", "#00000088"),
                        textAnimation = clipObj.optString("textAnimation", "Fade In"),
                        shapeType = shapeType,
                        shapeFillHex = clipObj.optString("shapeFillHex", "#8B5CF6"),
                        shapeStrokeHex = clipObj.optString("shapeStrokeHex", "#FFFFFF"),
                        shapeStrokeWidthDp = clipObj.optDouble("shapeStrokeWidthDp", 2.0).toFloat(),
                        shapeCornerRadiusDp = clipObj.optDouble("shapeCornerRadiusDp", 12.0).toFloat(),
                        assetId = clipObj.optString("assetId", "cyber_neon"),
                        assetColorHex = clipObj.optString("assetColorHex", "#6366F1"),
                        volume = clipObj.optDouble("volume", 1.0).toFloat(),
                        fadeInMs = clipObj.optLong("fadeInMs", 0L),
                        fadeOutMs = clipObj.optLong("fadeOutMs", 0L),
                        pitchPreset = pitch,
                        speed = clipObj.optDouble("speed", 1.0).toFloat(),
                        speedPreset = speedPreset,
                        transitionIn = transIn,
                        transitionOut = transOut,
                        transitionDurationMs = clipObj.optLong("transitionDurationMs", 500L),
                        posX = clipObj.optDouble("posX", 0.0).toFloat(),
                        posY = clipObj.optDouble("posY", 0.0).toFloat(),
                        scaleX = clipObj.optDouble("scaleX", 1.0).toFloat(),
                        scaleY = clipObj.optDouble("scaleY", 1.0).toFloat(),
                        rotationDeg = clipObj.optDouble("rotationDeg", 0.0).toFloat(),
                        opacity = clipObj.optDouble("opacity", 1.0).toFloat(),
                        blendMode = blendMode,
                        filterPreset = filter,
                        exposure = clipObj.optDouble("exposure", 0.0).toFloat(),
                        contrast = clipObj.optDouble("contrast", 1.0).toFloat(),
                        saturation = clipObj.optDouble("saturation", 1.0).toFloat(),
                        temperature = clipObj.optDouble("temperature", 0.0).toFloat(),
                        vignette = clipObj.optDouble("vignette", 0.0).toFloat(),
                        keyframes = kfList,
                        effects = effList
                    )
                )
            }

            tracksList.add(
                Track(
                    id = trackId,
                    type = trackType,
                    name = trackName,
                    isMuted = isMuted,
                    isLocked = isLocked,
                    isVisible = isVisible,
                    clips = clipsList
                )
            )
        }

        return Project(
            id = id,
            name = name,
            aspectRatio = aspectRatio,
            width = width,
            height = height,
            fps = fps,
            durationMs = durationMs,
            tracks = tracksList,
            lastModified = lastModified
        )
    }
}
