package com.android.dialer.ui.recents.mapper.recentsitemuimapperimpl

import android.os.Build
import android.provider.CallLog.Calls
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.data.recents.model.CallType
import com.android.dialer.testutil.callLogEntry
import com.android.dialer.ui.recents.model.RecentsCallTypeIcon
import io.mockk.every
import java.util.Locale
import kotlinx.collections.immutable.persistentListOf
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
internal class RecentsItemUiMapperImplFlagsTest : BaseRecentsItemUiMapperImplTest() {

    @Test
    fun map_mapsEachCallTypeToItsIcon() {
        val icons = listOf(
            CallType.Answered to RecentsCallTypeIcon.Incoming,
            CallType.Outgoing to RecentsCallTypeIcon.Outgoing,
            CallType.Missed to RecentsCallTypeIcon.Missed,
            CallType.Rejected to RecentsCallTypeIcon.Missed,
            CallType.Blocked to RecentsCallTypeIcon.Blocked,
            CallType.Voicemail to RecentsCallTypeIcon.Voicemail,
            CallType.Unknown(rawType = UNKNOWN_RAW_TYPE) to RecentsCallTypeIcon.Missed,
        )

        icons.forEach { (callType, icon) ->
            assertEquals(icon, map(callLogEntry(id = 1L, callType = callType)).callTypeIcon)
        }
    }

    @Test
    fun map_withUnreadMissedCall_marksTheRow() {
        val entry = callLogEntry(id = 1L, callType = CallType.Missed, isRead = false)

        assertTrue(map(entry).isUnreadMissedCall)
    }

    @Test
    fun map_withReadMissedCall_doesNotMarkTheRow() {
        val entry = callLogEntry(id = 1L, callType = CallType.Missed, isRead = true)

        assertFalse(map(entry).isUnreadMissedCall)
    }

    @Test
    fun map_withUnreadAnsweredCall_doesNotMarkTheRow() {
        val entry = callLogEntry(id = 1L, callType = CallType.Answered, isRead = false)

        assertFalse(map(entry).isUnreadMissedCall)
    }

    @Test
    fun map_withGroupedCalls_labelsTheCount() {
        val model = map(callLogEntry(id = 1L, groupedCallCount = 3))

        assertEquals("(3)", model.groupedCallCountLabel)
    }

    @Test
    fun map_withGroupedCallsUnderArabicDigits_localisesTheCount() {
        val defaultLocale = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag("ar-EG-u-nu-arab"))

        try {
            val model = map(callLogEntry(id = 1L, groupedCallCount = 3))

            assertEquals("(\u0663)", model.groupedCallCountLabel)
        } finally {
            Locale.setDefault(defaultLocale)
        }
    }

    @Test
    fun map_withASingleCall_omitsTheCountLabel() {
        val model = map(callLogEntry(id = 1L, groupedCallCount = 1))

        assertNull(model.groupedCallCountLabel)
    }

    @Test
    fun map_withVideoFeatureAndACallableNumber_allowsBothCallActions() {
        val model = map(callLogEntry(id = 1L, features = Calls.FEATURES_VIDEO))

        assertTrue(model.canCallBack)
        assertTrue(model.canVideoCall)
    }

    @Test
    fun map_withoutVideoFeature_allowsOnlyTheVoiceCall() {
        val model = map(callLogEntry(id = 1L))

        assertTrue(model.canCallBack)
        assertFalse(model.canVideoCall)
    }

    @Test
    fun map_whenTheNumberCannotBeCalled_disablesBothCallActions() {
        every { canPlaceCall(any(), any()) } returns false

        val model = map(callLogEntry(id = 1L, features = Calls.FEATURES_VIDEO))

        assertFalse(model.canCallBack)
        assertFalse(model.canVideoCall)
    }

    @Test
    fun map_withALatinInitial_uppercasesItForTheAvatar() {
        val model = map(callLogEntry(id = 1L, cachedName = "ada"))

        assertEquals('A', model.avatar.letter)
    }

    @Test
    fun map_withANonLatinInitial_leavesTheAvatarLetterEmpty() {
        val model = map(callLogEntry(id = 1L, cachedName = "山田太郎"))

        assertNull(model.avatar.letter)
    }

    @Test
    fun map_copiesTheNumberAndTheContactUrisOntoTheModel() {
        val entry = callLogEntry(
            id = 1L,
            number = "6502530000",
            photoUri = "content://photo/1",
            lookupUri = "content://contacts/lookup/1",
        )

        val model = map(entry)

        assertEquals("6502530000", model.number)
        assertEquals("content://photo/1", model.avatar.photoUri)
        assertEquals("content://contacts/lookup/1", model.avatar.lookupUri)
    }

    private companion object {
        private const val UNKNOWN_RAW_TYPE = 42
    }

    @Test
    fun map_carriesEveryGroupedEntryIdForDelete() {
        val groupIds = persistentListOf(
            CallLogEntryId(value = 3L),
            CallLogEntryId(value = 2L),
            CallLogEntryId(value = 1L),
        )

        val model = map(callLogEntry(id = 3L).copy(groupedEntryIds = groupIds))

        assertEquals(groupIds, model.groupedEntryIds)
    }
}
