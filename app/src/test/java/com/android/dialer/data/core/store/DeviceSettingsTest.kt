package com.android.dialer.data.core.store

import android.content.ContentResolver
import android.os.Build
import android.provider.Settings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
class DeviceSettingsTest {

    private val contentResolver: ContentResolver = RuntimeEnvironment
        .getApplication()
        .contentResolver
    private val deviceSettings = DeviceSettingsImpl(contentResolver = contentResolver)

    @Test
    fun dialingTonesAreOnUnlessTheUserTurnedThemOff() {
        assertTrue(deviceSettings.isDtmfToneWhenDialingEnabled())

        Settings.System.putInt(contentResolver, Settings.System.DTMF_TONE_WHEN_DIALING, OFF)

        assertFalse(deviceSettings.isDtmfToneWhenDialingEnabled())
    }

    @Test
    fun dialingTonesAreReadAfreshOnEveryCall() {
        Settings.System.putInt(contentResolver, Settings.System.DTMF_TONE_WHEN_DIALING, OFF)
        assertFalse(deviceSettings.isDtmfToneWhenDialingEnabled())

        Settings.System.putInt(contentResolver, Settings.System.DTMF_TONE_WHEN_DIALING, ON)

        assertTrue(deviceSettings.isDtmfToneWhenDialingEnabled())
    }

    @Test
    fun airplaneModeIsOffUnlessTurnedOn() {
        assertFalse(deviceSettings.isAirplaneModeOn())

        Settings.Global.putInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, ON)

        assertTrue(deviceSettings.isAirplaneModeOn())
    }

    private companion object {
        private const val OFF = 0
        private const val ON = 1
    }
}
