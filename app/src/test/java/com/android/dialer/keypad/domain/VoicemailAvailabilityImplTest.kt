package com.android.dialer.keypad.domain

import android.content.Context
import android.os.Build
import android.telecom.PhoneAccountHandle
import android.telephony.TelephonyManager
import com.android.dialer.data.core.store.DeviceSettings
import com.android.dialer.telecom.TelecomUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
@OptIn(ExperimentalCoroutinesApi::class)
class VoicemailAvailabilityImplTest {

    private val context: Context = RuntimeEnvironment.getApplication()
    private val telephonyManager = mockk<TelephonyManager>()
    private val deviceSettings = mockk<DeviceSettings>()
    private val simAccount = mockk<PhoneAccountHandle>()
    private val secondSimAccount = mockk<PhoneAccountHandle>()

    @Before
    fun setUp() {
        mockkStatic(TelecomUtil::class)
        every { TelecomUtil.getSubscriptionPhoneAccounts(any()) } returns emptyList()
        every { TelecomUtil.getDefaultOutgoingPhoneAccount(any(), any()) } returns null
        every { TelecomUtil.getVoicemailNumber(any(), any()) } returns null
        every { telephonyManager.voiceMailNumber } returns null
    }

    @After
    fun tearDown() {
        unmockkStatic(TelecomUtil::class)
    }

    @Test
    fun singleSimIsReachableWhenTelephonyHasAVoicemailNumber() {
        runTest {
            every { telephonyManager.voiceMailNumber } returns "+15551234567"

            assertTrue(createAvailability().isVoicemailReachable())
        }
    }

    @Test
    fun singleSimIsNotReachableWithoutAVoicemailNumber() {
        runTest {
            every { telephonyManager.voiceMailNumber } returns null

            assertFalse(createAvailability().isVoicemailReachable())
        }
    }

    @Test
    fun singleSimIsNotReachableWithAnEmptyVoicemailNumber() {
        runTest {
            every { telephonyManager.voiceMailNumber } returns ""

            assertFalse(createAvailability().isVoicemailReachable())
        }
    }

    @Test
    fun aChosenAccountIsAskedInsteadOfTelephony() {
        runTest {
            every { TelecomUtil.getDefaultOutgoingPhoneAccount(any(), any()) } returns simAccount
            every { TelecomUtil.getVoicemailNumber(any(), simAccount) } returns "+15557654321"
            // Telephony would answer differently; the chosen account must win.
            every { telephonyManager.voiceMailNumber } returns null

            assertTrue(createAvailability().isVoicemailReachable())
        }
    }

    @Test
    fun multiSimWithoutADefaultIsReachableSoTelecomCanAsk() {
        runTest {
            every { TelecomUtil.getSubscriptionPhoneAccounts(any()) } returns
                listOf(simAccount, secondSimAccount)
            every { TelecomUtil.getDefaultOutgoingPhoneAccount(any(), any()) } returns null
            every { telephonyManager.voiceMailNumber } returns null

            // No number anywhere, but the call is still placed so Telecom shows its "Call with" picker.
            assertTrue(createAvailability().isVoicemailReachable())
        }
    }

    @Test
    fun multiSimWithADefaultFallsBackToTheNumberCheck() {
        runTest {
            every { TelecomUtil.getSubscriptionPhoneAccounts(any()) } returns
                listOf(simAccount, secondSimAccount)
            every { TelecomUtil.getDefaultOutgoingPhoneAccount(any(), any()) } returns simAccount
            every { TelecomUtil.getVoicemailNumber(any(), simAccount) } returns null

            assertFalse(createAvailability().isVoicemailReachable())
        }
    }

    @Test
    fun isNotReachableWhenReadingTheAccountsThrows() {
        runTest {
            every { TelecomUtil.getSubscriptionPhoneAccounts(any()) } throws
                SecurityException("no READ_PHONE_STATE")

            assertFalse(createAvailability().isVoicemailReachable())
        }
    }

    @Test
    fun isNotReachableWhenReadingTheVoicemailNumberThrows() {
        runTest {
            every {
                telephonyManager.voiceMailNumber
            } throws SecurityException("no READ_PHONE_STATE")

            assertFalse(createAvailability().isVoicemailReachable())
        }
    }

    @Test
    fun airplaneModeComesFromTheDeviceSettings() {
        runTest {
            every { deviceSettings.isAirplaneModeOn() } returns true

            assertTrue(createAvailability().isAirplaneModeOn())
        }
    }

    @Test
    fun noAirplaneModeWhenTheDeviceSettingsSayOff() {
        runTest {
            every { deviceSettings.isAirplaneModeOn() } returns false

            assertFalse(createAvailability().isAirplaneModeOn())
        }
    }

    private fun createAvailability(): VoicemailAvailabilityImpl {
        return VoicemailAvailabilityImpl(
            context = context,
            telephonyManager = telephonyManager,
            deviceSettings = deviceSettings,
            ioDispatcher = UnconfinedTestDispatcher(),
        )
    }
}
