package com.android.dialer.ui.recents.mapper.recentsitemuimapperimpl

import android.os.Build
import android.provider.CallLog.Calls
import android.provider.ContactsContract.CommonDataKinds.Phone
import com.android.dialer.R
import com.android.dialer.compat.telephony.TelephonyManagerCompat
import com.android.dialer.testutil.callLogEntry
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsItemUiMapperImplMetadataTest : BaseRecentsItemUiMapperImplTest() {

    @Test
    fun map_withAnUnformattedExtension_displaysAndSpeaksItOnce() {
        val model = map(callLogEntry(id = 1L, number = "123").copy(postDialDigits = ",45;67"))

        assertEquals("formatted 123,45;67", model.displayNumber)
        assertEquals("1 2 3 4 5 6 7", model.spokenDisplayNumber)
        assertEquals("123,45;67", model.callbackNumber)
        assertEquals("123", model.number)
    }

    @Test
    fun map_withAProviderFormattedExtension_doesNotAppendItAgain() {
        val model = map(
            callLogEntry(id = 1L).copy(formattedNumber = "123,45", postDialDigits = ",45"),
        )

        assertEquals("123,45", model.displayNumber)
    }

    @Test
    fun map_withAnEmptyFormattedNumber_usesTheNumberAndExtension() {
        val model = map(
            callLogEntry(
                id = 1L,
                number = "123"
            ).copy(formattedNumber = "", postDialDigits = ",45"),
        )

        assertEquals("formatted 123,45", model.displayNumber)
    }

    @Test
    fun map_whenTheDisplayPreferenceChanges_usesTheCurrentNameWithTheSameEntry() {
        val entry = callLogEntry(id = 1L, cachedName = "Ada Lovelace")
            .copy(alternativeName = "Lovelace, Ada")
        assertEquals("Ada Lovelace", map(entry).primaryText)
        every { contactDisplayPreferences.getDisplayName(any(), any()) } answers { secondArg() }

        val model = map(entry)

        assertEquals("Lovelace, Ada", model.primaryText)
        assertEquals('L', model.avatar.letter)
        assertTrue(model.contentDescription.contains("Lovelace, Ada"))
    }

    @Test
    fun map_withCarrierPresenceOnAVoiceCall_exposesVideoWithoutChangingThePrimaryAction() {
        val entry = callLogEntry(id = 1L).copy(
            supportsVideoPresence = true,
            carrierPresence = Phone.CARRIER_PRESENCE_VT_CAPABLE,
        )
        val model = map(entry)

        assertTrue(model.canVideoCall)
        assertFalse(model.isVideoCall)
        assertTrue(
            model.callActionLabel.orEmpty().contains(R.string.description_call_action.toString())
        )
    }

    @Test
    fun map_withoutEitherCarrierCapability_doesNotOfferVideoForVoiceCalls() {
        val entry = callLogEntry(id = 1L)

        assertFalse(map(entry.copy(supportsVideoPresence = true)).canVideoCall)
        assertFalse(
            map(entry.copy(carrierPresence = Phone.CARRIER_PRESENCE_VT_CAPABLE)).canVideoCall
        )
    }

    @Test
    fun map_withARecordedVideoCall_keepsItsAccountAndPrimaryVideoAction() {
        val model = map(
            callLogEntry(id = 1L, features = Calls.FEATURES_VIDEO).copy(
                accountComponentName = "example/.Service",
                accountId = "sim2",
            )
        )

        assertTrue(model.canVideoCall)
        assertTrue(model.isVideoCall)
        assertEquals("example/.Service", model.accountComponentName)
        assertEquals("sim2", model.accountId)
    }

    @Test
    fun map_withAViaNumberAndNoAccountLabel_displaysAndSpeaksTheViaNumber() {
        val model = map(callLogEntry(id = 1L).copy(viaNumber = "123"))

        assertEquals("string-${R.string.call_log_via_number}:123", model.accountLabel)
        assertTrue(
            model.contentDescription.contains("string-${R.string.description_via_number}:1 2 3")
        )
    }

    @Test
    fun map_withAnAccountAndViaNumber_preservesBothInTheLabelAndDescription() {
        val model = map(callLogEntry(id = 1L).copy(accountLabel = "SIM 2", viaNumber = "123"))

        assertEquals(
            "string-${R.string.call_log_via_number_phone_account}:SIM 2, 123",
            model.accountLabel
        )
        assertTrue(model.contentDescription.contains("SIM 2, 1 2 3"))
    }

    @Test
    fun map_withFeatureBits_exposesAllRecordedMarkers() {
        val model = map(
            callLogEntry(
                id = 1L,
                features = Calls.FEATURES_HD_CALL or
                    Calls.FEATURES_RTT or TelephonyManagerCompat.FEATURES_ASSISTED_DIALING
            )
        )

        assertTrue(model.isHdCall)
        assertTrue(model.isRttCall)
        assertTrue(model.isAssistedDialing)

        val plain = map(callLogEntry(id = 2L))

        assertFalse(plain.isHdCall)
        assertFalse(plain.isRttCall)
        assertFalse(plain.isAssistedDialing)
    }
}
