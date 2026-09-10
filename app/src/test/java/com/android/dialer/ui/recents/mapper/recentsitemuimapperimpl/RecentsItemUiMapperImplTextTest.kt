package com.android.dialer.ui.recents.mapper.recentsitemuimapperimpl

import android.os.Build
import android.provider.CallLog.Calls
import android.provider.ContactsContract.CommonDataKinds.Phone
import com.android.dialer.R
import com.android.dialer.data.recents.model.CallType
import com.android.dialer.testutil.callLogEntry
import io.mockk.every
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsItemUiMapperImplTextTest : BaseRecentsItemUiMapperImplTest() {

    @Test
    fun map_withARelativeTimestamp_keepsItsDisplayWordsTogetherAndSpeechUnchanged() {
        val model = map(callLogEntry(id = 1L, geocodedLocation = LOCATION))

        assertEquals("$LOCATION •\u00A05\u00A0min\u00A0ago", model.secondaryText)
        assertTrue(model.contentDescription.contains(LONG_TIME))
        assertFalse(model.contentDescription.contains('\u00A0'))
    }

    @Test
    fun map_withEmergencyNumber_usesTheEmergencyLabelOverTheContactName() {
        every { isEmergencyNumber("911") } returns true

        val model = map(callLogEntry(id = 1L, number = "911", cachedName = "Ada"))

        assertEquals(string(R.string.emergency_number), model.primaryText)
    }

    @Test
    fun map_withUnknownPresentation_usesThePresentationName() {
        val entry = callLogEntry(
            id = 1L,
            number = "",
            numberPresentation = Calls.PRESENTATION_UNKNOWN,
        )

        assertEquals(string(R.string.unknown), map(entry).primaryText)
    }

    @Test
    fun map_withRestrictedPresentation_usesThePrivateNumberLabel() {
        val entry = callLogEntry(
            id = 1L,
            number = "",
            numberPresentation = Calls.PRESENTATION_RESTRICTED,
        )

        assertEquals(string(R.string.private_num_non_verizon), map(entry).primaryText)
    }

    @Test
    fun map_withPayphonePresentation_usesThePayphoneLabel() {
        val entry = callLogEntry(
            id = 1L,
            number = "",
            numberPresentation = Calls.PRESENTATION_PAYPHONE,
        )

        assertEquals(string(R.string.payphone), map(entry).primaryText)
    }

    @Test
    fun map_withRestrictedPresentationAndACachedName_prefersThePresentationName() {
        val entry = callLogEntry(
            id = 1L,
            cachedName = "Ada",
            numberPresentation = Calls.PRESENTATION_RESTRICTED,
        )

        assertEquals(string(R.string.private_num_non_verizon), map(entry).primaryText)
    }

    @Test
    fun map_withCachedName_usesTheContactName() {
        val model = map(callLogEntry(id = 1L, cachedName = "Ada"))

        assertEquals("Ada", model.primaryText)
    }

    @Test
    fun map_withACachedFormattedNumber_usesItWithoutFormattingAgain() {
        val entry = callLogEntry(id = 1L, number = RAW_NUMBER, formattedNumber = FORMATTED_NUMBER)

        val model = map(entry)

        assertEquals(FORMATTED_NUMBER, model.primaryText)
        verify(exactly = 0) { phoneNumberFormatter.formatForDisplay(any(), any()) }
    }

    @Test
    fun map_withoutACachedFormattedNumber_formatsItWithTheCallsCountry() {
        map(callLogEntry(id = 1L, number = "8765550100", countryIso = "JM"))

        verify { phoneNumberFormatter.formatForDisplay(number = "8765550100", countryIso = "JM") }
    }

    @Test
    fun map_withoutACachedFormattedNumber_formatsTheNumber() {
        val model = map(callLogEntry(id = 1L, number = RAW_NUMBER))

        assertEquals("formatted $RAW_NUMBER", model.primaryText)
    }

    @Test
    fun map_withoutNameOrNumber_usesTheUnknownLabel() {
        val model = map(callLogEntry(id = 1L, number = ""))

        assertEquals(string(R.string.new_call_log_unknown), model.primaryText)
    }

    @Test
    fun map_withGeocodedLocationAndNoContact_joinsThePlaceAndTheTimeWithABullet() {
        val model = map(callLogEntry(id = 1L, geocodedLocation = LOCATION))

        assertEquals("$LOCATION •\u00A0$DISPLAY_TIME", model.secondaryText)
    }

    @Test
    fun map_withAContactAndAMobileNumber_showsTheTypeLabelInsteadOfThePlace() {
        val entry = callLogEntry(
            id = 1L,
            geocodedLocation = LOCATION,
            cachedName = "Ada",
            numberType = Phone.TYPE_MOBILE,
        )

        assertEquals("$MOBILE_LABEL •\u00A0$DISPLAY_TIME", map(entry).secondaryText)
    }

    @Test
    fun map_withAContactAndACustomLabel_showsTheLabel() {
        val entry = callLogEntry(
            id = 1L,
            cachedName = "Ada",
            numberType = Phone.TYPE_CUSTOM,
            numberLabel = "Studio",
        )

        assertEquals("Studio •\u00A0$DISPLAY_TIME", map(entry).secondaryText)
    }

    @Test
    fun map_withAContactAndNoTypeLabel_showsTheNumberAndTheTime() {
        val entry = callLogEntry(
            id = 1L,
            number = "6502530000",
            geocodedLocation = LOCATION,
            cachedName = "Ada",
        )

        assertEquals("formatted 6502530000 •\u00A0$DISPLAY_TIME", map(entry).secondaryText)
    }

    @Test
    fun map_withAStrangerAndAMobileType_showsThePlaceNotTheLabel() {
        val entry = callLogEntry(
            id = 1L,
            geocodedLocation = LOCATION,
            numberType = Phone.TYPE_MOBILE,
        )

        assertEquals("$LOCATION •\u00A0$DISPLAY_TIME", map(entry).secondaryText)
    }

    @Test
    fun map_withoutGeocodedLocation_showsTheTimeAlone() {
        val model = map(callLogEntry(id = 1L))

        assertEquals(DISPLAY_TIME, model.secondaryText)
    }

    @Test
    fun map_withAVideoCall_putsTheVideoLabelBeforeThePlace() {
        val entry = callLogEntry(
            id = 1L,
            geocodedLocation = LOCATION,
            features = Calls.FEATURES_VIDEO,
        )

        assertEquals("$VIDEO_LABEL, $LOCATION •\u00A0$DISPLAY_TIME", map(entry).secondaryText)
    }

    @Test
    fun map_withEmergencyNumber_reducesTheSecondaryLineToTheTime() {
        every { isEmergencyNumber("911") } returns true
        val entry = callLogEntry(id = 1L, number = "911", geocodedLocation = LOCATION)

        assertEquals(DISPLAY_TIME, map(entry).secondaryText)
    }

    @Test
    fun map_describesTheRowWithTheCountThePrimaryTextAndTheSpelledOutLine() {
        val entry = callLogEntry(
            id = 1L,
            cachedName = "Ada",
            callType = CallType.Missed,
            groupedCallCount = 2,
            numberType = Phone.TYPE_MOBILE,
        )

        val model = map(entry)

        val plurals = R.plurals.a11y_new_call_log_entry_missed_call
        assertEquals("plurals-$plurals-2-Ada; $MOBILE_LABEL, $LONG_TIME", model.contentDescription)
        verify(exactly = 1) { resources.getQuantityString(plurals, 2) }
    }

    @Test
    fun map_withABareNumber_describesItDigitByDigit() {
        val entry = callLogEntry(id = 1L, number = RAW_NUMBER, formattedNumber = FORMATTED_NUMBER)

        val model = map(entry)

        val plurals = R.plurals.a11y_new_call_log_entry_answered_call
        assertEquals(
            "plurals-$plurals-1-$SPOKEN_NUMBER; $LONG_TIME",
            model.contentDescription,
        )
    }

    @Test
    fun map_speaksTheDisplayNumberDigitByDigit() {
        val entry = callLogEntry(id = 1L, number = RAW_NUMBER, formattedNumber = FORMATTED_NUMBER)

        assertEquals(SPOKEN_NUMBER, map(entry).spokenDisplayNumber)
    }

    @Test
    fun map_withASipAddress_speaksItAsWritten() {
        val entry = callLogEntry(id = 1L, number = SIP_ADDRESS, formattedNumber = SIP_ADDRESS)

        val model = map(entry)

        assertEquals(SIP_ADDRESS, model.spokenDisplayNumber)
        assertEquals(
            "plurals-${R.plurals.a11y_new_call_log_entry_answered_call}-1-$SIP_ADDRESS; $LONG_TIME",
            model.contentDescription,
        )
    }

    @Test
    fun map_withDigitsInASipUser_speaksTheWholeAddress() {
        val address = "review42@example.com"
        val entry = callLogEntry(id = 1L, number = address, formattedNumber = address)

        val model = map(entry)

        assertEquals(address, model.spokenDisplayNumber)
        assertEquals(
            "plurals-${R.plurals.a11y_new_call_log_entry_answered_call}-1-$address; $LONG_TIME",
            model.contentDescription,
        )
    }

    @Test
    fun map_withDigitsInASipHost_speaksTheWholeAddress() {
        val address = "sip:ada@office42.example.com"
        val entry = callLogEntry(id = 1L, number = address, formattedNumber = address)

        val model = map(entry)

        assertEquals(address, model.spokenDisplayNumber)
        assertEquals(
            "plurals-${R.plurals.a11y_new_call_log_entry_answered_call}-1-$address; $LONG_TIME",
            model.contentDescription,
        )
    }

    private companion object {
        private const val SIP_ADDRESS = "sip:ada@example.com"
        private const val RAW_NUMBER = "6502530000"
        private const val FORMATTED_NUMBER = "(650) 253-0000"
        private const val SPOKEN_NUMBER = "6 5 0 2 5 3 0 0 0 0"
    }
}
