package com.example.engine

import android.media.AudioManager
import android.media.ToneGenerator

object AudioSimulator {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (_: Exception) {}
    }

    fun playSfx(sfxType: String) {
        try {
            when (sfxType) {
                "whoosh" -> toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
                "camera" -> toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 60)
                "glitch" -> toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 120)
                "beat" -> toneGenerator?.startTone(ToneGenerator.TONE_DTMF_0, 70)
                "keyframe" -> toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                "split" -> toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 90)
                else -> toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
            }
        } catch (_: Exception) {}
    }
}
