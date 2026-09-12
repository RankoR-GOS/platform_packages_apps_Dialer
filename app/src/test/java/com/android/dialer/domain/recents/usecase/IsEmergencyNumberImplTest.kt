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
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class IsEmergencyNumberImplTest {

    private val telephonyManager = mockk<TelephonyManager>()

    @Test
    fun invoke_withABlankNumber_returnsFalseWithoutAskingTelephony() {
        assertFalse(createUseCase()(" "))

        verify(exactly = 0) { telephonyManager.isEmergencyNumber(any()) }
    }

    @Test
    fun invoke_withoutTelephony_returnsFalse() {
        assertFalse(IsEmergencyNumberImpl(telephonyManager = null)(EMERGENCY_NUMBER))
    }

    @Test
    fun invoke_whenTelephonyErrorContainsPrivateData_logsOnlyTheFailureCategory() {
        val privateData = "reviewer42@example.invalid +15550101999"
        every { telephonyManager.isEmergencyNumber(any()) } throws
            IllegalStateException(privateData, IllegalArgumentException(privateData))
        ShadowLog.clear()

        assertFalse(createUseCase()(EMERGENCY_NUMBER))

        val logs = ShadowLog.getLogs().map { it.msg }
        assertTrue(logs.any { it.contains("telephony unavailable") })
        assertFalse(logs.any { it.contains(privateData) })
    }

    private fun createUseCase(): IsEmergencyNumberImpl {
        return IsEmergencyNumberImpl(telephonyManager = telephonyManager)
    }

    private companion object {
        private const val EMERGENCY_NUMBER = "911"
    }
}
