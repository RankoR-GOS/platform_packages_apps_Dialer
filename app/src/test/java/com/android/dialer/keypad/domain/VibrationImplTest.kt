package com.android.dialer.keypad.domain

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
class VibrationImplTest {

    private val vibrator = mockk<Vibrator>(relaxed = true)

    @Test
    fun vibratesOnceForTheGivenDuration() {
        VibrationImpl(vibrator = vibrator).vibrate(durationMs = PULSE_MS)

        verify(exactly = 1) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(PULSE_MS, VibrationEffect.DEFAULT_AMPLITUDE),
            )
        }
    }

    private companion object {
        private const val PULSE_MS = 200L
    }
}
