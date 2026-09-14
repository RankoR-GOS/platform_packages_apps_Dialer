package com.android.dialer.keypad.domain

import android.os.VibrationEffect
import android.os.Vibrator
import javax.inject.Inject

internal interface Vibration {
    fun vibrate(durationMs: Long)
}

internal class VibrationImpl @Inject constructor(
    private val vibrator: Vibrator,
) : Vibration {

    override fun vibrate(durationMs: Long) {
        val vibration = VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibration)
    }
}
