package com.android.dialer.data.phone.formatter

import android.os.Build
import android.telephony.TelephonyManager
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class PhoneNumberFormatterImplTest {

    private val telephonyManager = mockk<TelephonyManager>()
    private val defaultLocale = Locale.getDefault()

    @Before
    fun setUp() {
        Locale.setDefault(Locale.UK)
    }

    @After
    fun tearDown() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun formatForDisplay_usesTheNetworkCountry() {
        every { telephonyManager.networkCountryIso } returns "us"

        assertEquals("(650) 253-0000", createFormatter().formatForDisplay(US_NUMBER))
    }

    @Test
    fun formatForDisplay_withoutANetworkCountry_fallsBackToTheLocale() {
        every { telephonyManager.networkCountryIso } returns ""

        assertEquals("020 7946 0958", createFormatter().formatForDisplay(UK_NUMBER))
    }

    @Test
    fun formatForDisplay_withAnUnformattableNumber_returnsItUnchanged() {
        every { telephonyManager.networkCountryIso } returns "us"

        assertEquals("1-800-FLOWERS", createFormatter().formatForDisplay("1-800-FLOWERS"))
    }

    @Test
    fun formatForDisplay_underATurkishLocale_stillUppercasesTheCountry() {
        Locale.setDefault(Locale.forLanguageTag("tr-TR"))
        every { telephonyManager.networkCountryIso } returns "in"

        assertEquals("098765 43210", createFormatter().formatForDisplay(INDIAN_NUMBER))
    }

    @Test
    fun formatForDisplay_withoutTelephony_fallsBackToTheLocale() {
        assertEquals(
            "020 7946 0958",
            PhoneNumberFormatterImpl(telephonyManager = null).formatForDisplay(UK_NUMBER),
        )
    }

    private fun createFormatter(): PhoneNumberFormatterImpl {
        return PhoneNumberFormatterImpl(telephonyManager = telephonyManager)
    }

    private companion object {
        private const val US_NUMBER = "6502530000"
        private const val UK_NUMBER = "02079460958"
        private const val INDIAN_NUMBER = "09876543210"
    }
}
