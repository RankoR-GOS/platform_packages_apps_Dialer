package com.android.dialer.data.recents.account

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import com.android.dialer.telecom.TelecomUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class PhoneAccountLookupImplTest {

    private val context = mockk<Context>()
    private val lookup = PhoneAccountLookupImpl(context)
    private val first = PhoneAccountHandle(ComponentName("example", "Service"), "sim1")
    private val second = PhoneAccountHandle(ComponentName("example", "Service"), "sim2")

    @Before
    fun setUp() {
        mockkStatic(TelecomUtil::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(TelecomUtil::class)
    }

    @Test
    fun invoke_withTwoAccounts_returnsTheLabelForEachRecordedHandle() {
        every { TelecomUtil.getCallCapablePhoneAccounts(context) } returns listOf(first, second)
        every { TelecomUtil.getPhoneAccount(context, first) } returns account(first, "Personal")
        every { TelecomUtil.getPhoneAccount(context, second) } returns account(second, "Work")

        assertEquals(mapOf(first to "Personal", second to "Work"), lookup().labels)
    }

    @Test
    fun invoke_withOneAccount_omitsTheRedundantLabel() {
        every { TelecomUtil.getCallCapablePhoneAccounts(context) } returns listOf(first)
        every { TelecomUtil.getPhoneAccount(context, first) } returns account(first, "Personal")

        assertTrue(lookup().labels.isEmpty())
    }

    @Test
    fun invoke_withVideoPresenceCapability_enablesContactPresenceChecks() {
        every { TelecomUtil.getCallCapablePhoneAccounts(context) } returns listOf(first)
        every { TelecomUtil.getPhoneAccount(context, first) } returns account(
            first,
            "Personal",
            PhoneAccount.CAPABILITY_VIDEO_CALLING or
                PhoneAccount.CAPABILITY_VIDEO_CALLING_RELIES_ON_PRESENCE,
        )

        assertTrue(lookup().supportsVideoPresence)
    }

    @Test
    fun invoke_withVideoButNoPresenceCapability_doesNotAssumeContactSupport() {
        every { TelecomUtil.getCallCapablePhoneAccounts(context) } returns listOf(first)
        every { TelecomUtil.getPhoneAccount(context, first) } returns account(
            first,
            "Personal",
            PhoneAccount.CAPABILITY_VIDEO_CALLING,
        )

        assertFalse(lookup().supportsVideoPresence)
    }

    @Test
    fun invoke_whenPermissionIsRevoked_returnsNoAccountMetadata() {
        every { TelecomUtil.getCallCapablePhoneAccounts(context) } throws SecurityException()

        assertEquals(PhoneAccountSnapshot(), lookup())
    }

    @Test
    fun invoke_whenAnAccountDisappears_doesNotInventALabel() {
        every { TelecomUtil.getCallCapablePhoneAccounts(context) } returns listOf(first, second)
        every { TelecomUtil.getPhoneAccount(context, first) } returns null
        every { TelecomUtil.getPhoneAccount(context, second) } returns account(second, "Work")

        assertEquals(mapOf(second to "Work"), lookup().labels)
    }

    private fun account(
        handle: PhoneAccountHandle,
        label: String,
        capabilities: Int = 0,
    ): PhoneAccount {
        return PhoneAccount.builder(handle, label).setCapabilities(capabilities).build()
    }
}
