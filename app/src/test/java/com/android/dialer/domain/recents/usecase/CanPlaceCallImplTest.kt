package com.android.dialer.domain.recents.usecase

import android.os.Build
import android.provider.CallLog.Calls
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class CanPlaceCallImplTest {

    private val canPlaceCall = CanPlaceCallImpl()

    @Test
    fun invoke_withAnAllowedNumber_returnsTrue() {
        assertTrue(canPlaceCall(number = NUMBER, presentation = Calls.PRESENTATION_ALLOWED))
    }

    @Test
    fun invoke_withARestrictedPresentation_returnsFalse() {
        assertFalse(canPlaceCall(number = NUMBER, presentation = Calls.PRESENTATION_RESTRICTED))
    }

    @Test
    fun invoke_withALegacyUnknownNumber_returnsFalse() {
        assertFalse(canPlaceCall(number = "-1", presentation = Calls.PRESENTATION_ALLOWED))
    }

    private companion object {
        private const val NUMBER = "+15551234567"
    }
}
