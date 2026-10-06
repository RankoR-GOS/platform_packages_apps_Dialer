package com.android.dialer.keypad.domain

import android.media.AudioManager
import android.media.ToneGenerator
import com.android.dialer.common.LogUtil
import com.android.dialer.data.core.store.DeviceSettings
import javax.inject.Inject

/** Plays until [DtmfTonePlayer.stop]. */
internal const val TONE_LENGTH_INFINITE = -1

internal const val TONE_LENGTH_MS = 150

/** Local key feedback only; tones sent down a call are Telecom's. */
internal interface DtmfTonePlayer {

    fun acquire()

    fun release()

    fun play(tone: Int, durationMs: Int = TONE_LENGTH_INFINITE)

    fun stop()
}

/** A test seam: [ToneGenerator] is native audio that cannot be observed off-device. */
internal fun interface ToneGeneratorFactory {
    fun create(): ToneGenerator
}

/**
 * Reads the dialing-tone setting and ringer mode on every [play], as either can change without
 * leaving the keypad.
 *
 * [stop] is deliberately unguarded: a held key's tone is [TONE_LENGTH_INFINITE], and a stop that
 * checked the setting would strand it if the setting changed mid-press.
 */
internal class DtmfTonePlayerImpl @Inject constructor(
    private val deviceSettings: DeviceSettings,
    private val audioManager: AudioManager,
    private val toneGeneratorFactory: ToneGeneratorFactory,
) : DtmfTonePlayer {

    private val lock = Any()

    private var toneGenerator: ToneGenerator? = null

    override fun acquire() {
        synchronized(lock) {
            if (toneGenerator == null) {
                toneGenerator = createToneGenerator()
            }
        }
    }

    override fun release() {
        synchronized(lock) {
            toneGenerator?.release()
            toneGenerator = null
        }
    }

    override fun play(tone: Int, durationMs: Int) {
        if (!deviceSettings.isDtmfToneWhenDialingEnabled() || isSilenced()) {
            return
        }
        synchronized(lock) {
            val generator = toneGenerator
            if (generator == null) {
                LogUtil.w(TAG, "toneGenerator == null, dropping tone")
            } else {
                // Starting a tone stops whichever one is already playing.
                generator.startTone(tone, durationMs)
            }
        }
    }

    override fun stop() {
        synchronized(lock) {
            toneGenerator?.stopTone()
        }
    }

    // ToneGenerator throws when the platform cannot hand out an audio session.
    @Suppress("TooGenericExceptionCaught")
    private fun createToneGenerator(): ToneGenerator? {
        return try {
            toneGeneratorFactory.create()
        } catch (e: RuntimeException) {
            LogUtil.e(TAG, "Failed to create the local tone generator", e)
            null
        }
    }

    private fun isSilenced(): Boolean {
        val ringerMode = audioManager.ringerMode
        return ringerMode == AudioManager.RINGER_MODE_SILENT ||
            ringerMode == AudioManager.RINGER_MODE_VIBRATE
    }

    private companion object {
        private const val TAG = "DtmfTonePlayer"
    }
}

internal class ToneGeneratorFactoryImpl @Inject constructor() : ToneGeneratorFactory {

    override fun create(): ToneGenerator {
        return ToneGenerator(AudioManager.STREAM_DTMF, TONE_RELATIVE_VOLUME)
    }

    private companion object {
        private const val TONE_RELATIVE_VOLUME = 80
    }
}
