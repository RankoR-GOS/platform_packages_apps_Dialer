package com.android.dialer.domain.recents.usecase

import android.os.Build
import android.telephony.TelephonyManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class IsEmergencyNumberImplTest {

    private val telephonyManager = mockk<TelephonyManager>()

    @Test
    fun invoke_asksTelephonyForTheNumber() {
        every { telephonyManager.isEmergencyNumber(EMERGENCY_NUMBER) } returns true

        assertTrue(createUseCase()(EMERGENCY_NUMBER))
    }

    @Test
    fun invoke_withABlankNumber_returnsFalseWithoutAskingTelephony() {
        assertFalse(createUseCase()(" "))

        verify(exactly = 0) { telephonyManager.isEmergencyNumber(any()) }
    }

    @Test
    fun invoke_whenTelephonyIsUnavailable_returnsFalse() {
        every { telephonyManager.isEmergencyNumber(any()) } throws IllegalStateException("down")

        assertFalse(createUseCase()(EMERGENCY_NUMBER))
    }

    @Test
    fun invoke_withoutTelephony_returnsFalse() {
        assertFalse(IsEmergencyNumberImpl(telephonyManager = null)(EMERGENCY_NUMBER))
    }

    private fun createUseCase(): IsEmergencyNumberImpl {
        return IsEmergencyNumberImpl(telephonyManager = telephonyManager)
    }

    private companion object {
        private const val EMERGENCY_NUMBER = "911"
    }
}
