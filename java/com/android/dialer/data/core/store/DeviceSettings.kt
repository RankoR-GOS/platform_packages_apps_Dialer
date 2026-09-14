package com.android.dialer.data.core.store

import android.content.ContentResolver
import android.provider.Settings
import javax.inject.Inject

/** Device-wide settings the user changes outside this app, read afresh on every call. */
internal interface DeviceSettings {

    fun isDtmfToneWhenDialingEnabled(): Boolean

    fun isAirplaneModeOn(): Boolean
}

internal class DeviceSettingsImpl @Inject constructor(
    private val contentResolver: ContentResolver,
) : DeviceSettings {

    override fun isDtmfToneWhenDialingEnabled(): Boolean {
        return Settings.System.getInt(
            contentResolver,
            Settings.System.DTMF_TONE_WHEN_DIALING,
            ENABLED,
        ) == ENABLED
    }

    override fun isAirplaneModeOn(): Boolean {
        return Settings.Global.getInt(
            contentResolver,
            Settings.Global.AIRPLANE_MODE_ON,
            DISABLED,
        ) == ENABLED
    }

    private companion object {
        private const val DISABLED = 0
        private const val ENABLED = 1
    }
}
