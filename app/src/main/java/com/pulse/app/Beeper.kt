package com.pulse.app

import android.media.AudioManager
import android.media.ToneGenerator

/** Pitidos por el canal de alarma, para que suenen aunque el teléfono esté en vibración. */
class Beeper {
    private val tone: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_ALARM, 90)
    } catch (e: Exception) {
        null
    }

    fun short() {
        try {
            tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 160)
        } catch (e: Exception) {
        }
    }

    fun long() {
        try {
            tone?.startTone(ToneGenerator.TONE_DTMF_D, 750)
        } catch (e: Exception) {
        }
    }

    fun release() {
        try {
            tone?.release()
        } catch (e: Exception) {
        }
    }
}
