package com.android.dialer.keypad.domain

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import com.android.dialer.data.core.store.DeviceSettings
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
class DtmfTonePlayerImplTest {

    private val toneGenerator = mockk<ToneGenerator>(relaxed = true)
    private val deviceSettings = mockk<DeviceSettings>()
    private val audioManager = mockk<AudioManager>()
    private var isDtmfToneEnabled = true
    private var ringerMode = AudioManager.RINGER_MODE_NORMAL

    @Before
    fun setUp() {
        every { deviceSettings.isDtmfToneWhenDialingEnabled() } answers { isDtmfToneEnabled }
        every { audioManager.ringerMode } answers { ringerMode }
    }

    @Test
    fun playStartsTheToneAfterAcquire() {
        val player = createPlayer()
        player.acquire()

        player.play(tone = ToneGenerator.TONE_DTMF_5)

        verify(exactly = 1) {
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_5, TONE_LENGTH_INFINITE)
        }
    }

    @Test
    fun playHonoursAnExplicitDuration() {
        val player = createPlayer()
        player.acquire()

        player.play(tone = ToneGenerator.TONE_PROP_NACK, durationMs = TONE_LENGTH_MS)

        verify(exactly = 1) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, TONE_LENGTH_MS)
        }
    }

    @Test
    fun playDoesNothingBeforeAcquire() {
        val player = createPlayer()

        player.play(tone = ToneGenerator.TONE_DTMF_1)

        verify(exactly = 0) { toneGenerator.startTone(any(), any()) }
    }

    @Test
    fun playDoesNothingAfterRelease() {
        val player = createPlayer()
        player.acquire()
        player.release()

        player.play(tone = ToneGenerator.TONE_DTMF_1)

        verify(exactly = 1) { toneGenerator.release() }
        verify(exactly = 0) { toneGenerator.startTone(any(), any()) }
    }

    @Test
    fun playIsSuppressedWhenTheDtmfSettingIsOff() {
        isDtmfToneEnabled = false
        val player = createPlayer()
        player.acquire()

        player.play(tone = ToneGenerator.TONE_DTMF_1)

        verify(exactly = 0) { toneGenerator.startTone(any(), any()) }
    }

    @Test
    fun playIsSuppressedInSilentMode() {
        ringerMode = AudioManager.RINGER_MODE_SILENT
        val player = createPlayer()
        player.acquire()

        player.play(tone = ToneGenerator.TONE_DTMF_1)

        verify(exactly = 0) { toneGenerator.startTone(any(), any()) }
    }

    @Test
    fun playIsSuppressedInVibrateMode() {
        ringerMode = AudioManager.RINGER_MODE_VIBRATE
        val player = createPlayer()
        player.acquire()

        player.play(tone = ToneGenerator.TONE_DTMF_1)

        verify(exactly = 0) { toneGenerator.startTone(any(), any()) }
    }

    @Test
    fun ringerModeIsRereadOnEveryPlay() {
        val player = createPlayer()
        player.acquire()

        ringerMode = AudioManager.RINGER_MODE_SILENT
        player.play(tone = ToneGenerator.TONE_DTMF_1)
        ringerMode = AudioManager.RINGER_MODE_NORMAL
        player.play(tone = ToneGenerator.TONE_DTMF_2)

        verify(exactly = 0) { toneGenerator.startTone(ToneGenerator.TONE_DTMF_1, any()) }
        verify(exactly = 1) {
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_2, TONE_LENGTH_INFINITE)
        }
    }

    @Test
    fun stopStopsTheTone() {
        val player = createPlayer()
        player.acquire()
        player.play(tone = ToneGenerator.TONE_DTMF_1)

        player.stop()

        verify(exactly = 1) { toneGenerator.stopTone() }
    }

    @Test
    fun stopIgnoresSilentModeSoAPlayingToneCanAlwaysBeStopped() {
        val player = createPlayer()
        player.acquire()
        player.play(tone = ToneGenerator.TONE_DTMF_1)

        ringerMode = AudioManager.RINGER_MODE_SILENT
        player.stop()

        verify(exactly = 1) { toneGenerator.stopTone() }
    }

    @Test
    fun stopStopsTheToneEvenIfTheSettingIsTurnedOffMidPress() {
        val player = createPlayer()
        player.acquire()
        player.play(tone = ToneGenerator.TONE_DTMF_1)

        // A held key plays an infinite tone. If stop honoured the setting, flipping it off before
        // the release would leave the tone sounding forever.
        isDtmfToneEnabled = false
        player.stop()

        verify(exactly = 1) { toneGenerator.stopTone() }
    }

    @Test
    fun stopBeforeAcquireDoesNothing() {
        val player = createPlayer()

        player.stop()

        verify(exactly = 0) { toneGenerator.stopTone() }
    }

    @Test
    fun acquireTwiceCreatesOnlyOneGenerator() {
        var created = 0
        val player = DtmfTonePlayerImpl(
            deviceSettings = deviceSettings,
            audioManager = audioManager,
            toneGeneratorFactory = {
                created += 1
                toneGenerator
            },
        )

        player.acquire()
        player.acquire()

        assert(created == 1) { "expected a single generator, created $created" }
    }

    @Test
    fun aGeneratorThatFailsToConstructIsTolerated() {
        val player = DtmfTonePlayerImpl(
            deviceSettings = deviceSettings,
            audioManager = audioManager,
            toneGeneratorFactory = { throw IllegalStateException("no audio session") },
        )

        player.acquire()
        player.play(tone = ToneGenerator.TONE_DTMF_1)
        player.stop()
        player.release()
    }

    private fun createPlayer(): DtmfTonePlayerImpl {
        return DtmfTonePlayerImpl(
            deviceSettings = deviceSettings,
            audioManager = audioManager,
            toneGeneratorFactory = { toneGenerator },
        )
    }
}
