package com.android.dialer.ui.recents.mapper.recentsitemuimapperimpl

import android.os.Build
import android.provider.CallLog.Calls
import com.android.dialer.R
import com.android.dialer.testutil.callLogEntry
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsItemUiMapperImplVoicemailTest : BaseRecentsItemUiMapperImplTest() {

    @Test
    fun map_withAVoicemailNumber_usesTheLabelAndAvatarBeforeCachedContactData() {
        val entry = callLogEntry(id = 1L, cachedName = "Contact", photoUri = "content://photo/1")
            .copy(isVoicemailNumber = true)

        val model = map(entry)

        assertEquals(string(R.string.voicemail_string), model.primaryText)
        assertEquals(DISPLAY_TIME, model.secondaryText)
        assertTrue(model.contentDescription.contains(string(R.string.voicemail_string)))
        assertTrue(model.contentDescription.contains(LONG_TIME))
        assertFalse(model.contentDescription.contains("Contact"))
        assertTrue(model.avatar.isVoicemail)
        assertNull(model.avatar.photoUri)
        assertNull(model.avatar.letter)
        assertEquals(entry.number, model.number)
    }

    @Test
    fun map_withAVideoFlagOnAVoicemailNumber_keepsOnlyTheVoiceCallback() {
        val model = map(
            callLogEntry(id = 1L, features = Calls.FEATURES_VIDEO).copy(isVoicemailNumber = true)
        )

        assertTrue(model.canCallBack)
        assertFalse(model.canVideoCall)
        assertFalse(model.isVideoCall)
        assertFalse(model.canMessage)
        assertFalse(model.canAddContact)
        assertFalse(model.canEditNumberBeforeCall)
        assertFalse(model.canBlockNumber)
        assertEquals(
            "text-${R.string.description_call_action} ${string(R.string.voicemail_string)}",
            model.callActionLabel,
        )
    }

    @Test
    fun map_withRestrictedPresentation_keepsItBeforeTheVoicemailLabel() {
        val entry = callLogEntry(id = 1L).copy(
            isVoicemailNumber = true,
            numberPresentation = Calls.PRESENTATION_RESTRICTED,
        )
        every { canPlaceCall(any(), Calls.PRESENTATION_RESTRICTED) } returns false

        val model = map(entry)

        assertEquals(string(R.string.private_num_non_verizon), model.primaryText)
        assertFalse(model.canCallBack)
        assertNull(model.callActionLabel)
    }

    @Test
    fun map_withAnEmergencyNumber_keepsItsLabelAndVoiceActionBeforeVoicemailAndVideo() {
        every { isEmergencyNumber(any()) } returns true
        val model = map(
            callLogEntry(id = 1L, features = Calls.FEATURES_VIDEO).copy(isVoicemailNumber = true)
        )

        assertEquals(string(R.string.emergency_number), model.primaryText)
        assertFalse(model.avatar.isVoicemail)
        assertFalse(model.isVideoCall)
        assertFalse(model.canBlockNumber)
        assertEquals(
            "text-${R.string.description_call_action} ${string(R.string.emergency_number)}",
            model.callActionLabel,
        )
    }
}
