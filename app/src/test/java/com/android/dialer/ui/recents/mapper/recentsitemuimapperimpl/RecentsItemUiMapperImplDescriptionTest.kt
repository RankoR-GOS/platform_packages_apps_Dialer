package com.android.dialer.ui.recents.mapper.recentsitemuimapperimpl

import android.os.Build
import com.android.dialer.R
import com.android.dialer.data.recents.model.CallType
import com.android.dialer.testutil.callLogEntry
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsItemUiMapperImplDescriptionTest : BaseRecentsItemUiMapperImplTest() {

    @Test
    fun map_withAnAnsweredCall_speaksTheAnsweredCallPlural() {
        assertSpokenAs(
            callType = CallType.Answered,
            plurals = R.plurals.a11y_new_call_log_entry_answered_call,
        )
    }

    @Test
    fun map_withAnOutgoingCall_speaksTheOutgoingCallPlural() {
        assertSpokenAs(
            callType = CallType.Outgoing,
            plurals = R.plurals.a11y_new_call_log_entry_outgoing_call,
        )
    }

    @Test
    fun map_withABlockedCall_speaksTheBlockedCallPlural() {
        assertSpokenAs(
            callType = CallType.Blocked,
            plurals = R.plurals.a11y_new_call_log_entry_blocked_call,
        )
    }

    @Test
    fun map_withAMissedCall_speaksTheMissedCallPlural() {
        assertSpokenAs(
            callType = CallType.Missed,
            plurals = R.plurals.a11y_new_call_log_entry_missed_call,
        )
    }

    @Test
    fun map_withARejectedVoicemailOrUnknownCall_speaksTheMissedCallPlural() {
        val plurals = R.plurals.a11y_new_call_log_entry_missed_call

        listOf(
            CallType.Rejected,
            CallType.Voicemail,
            CallType.Unknown(rawType = UNKNOWN_RAW_TYPE),
        ).forEach { callType ->
            val model = map(callLogEntry(id = 1L, cachedName = NAME, callType = callType))

            assertEquals("plurals-$plurals-1-$NAME; $LONG_TIME", model.contentDescription)
        }
    }

    private fun assertSpokenAs(callType: CallType, plurals: Int) {
        val model = map(callLogEntry(id = 1L, cachedName = NAME, callType = callType))

        assertEquals("plurals-$plurals-1-$NAME; $LONG_TIME", model.contentDescription)
        verify(exactly = 1) { resources.getQuantityString(plurals, 1) }
    }

    private companion object {
        const val NAME = "Ada"
        const val UNKNOWN_RAW_TYPE = 99
    }
}
