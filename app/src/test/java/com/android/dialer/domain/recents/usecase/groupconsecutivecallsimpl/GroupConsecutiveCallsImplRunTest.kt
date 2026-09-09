package com.android.dialer.domain.recents.usecase.groupconsecutivecallsimpl

import android.os.Build
import android.provider.CallLog.Calls
import com.android.dialer.compat.telephony.TelephonyManagerCompat
import com.android.dialer.data.recents.model.CallType
import com.android.dialer.domain.recents.usecase.GroupConsecutiveCallsImpl
import com.android.dialer.testutil.TEST_TIMESTAMP_MILLIS
import com.android.dialer.testutil.callLogEntry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class GroupConsecutiveCallsImplRunTest {

    private val groupConsecutiveCalls = GroupConsecutiveCallsImpl()

    @Test
    fun invoke_withNoEntries_returnsNoGroups() {
        val grouped = groupConsecutiveCalls(entries = emptyList())

        assertEquals(0, grouped.size)
    }

    @Test
    fun invoke_withASingleCall_returnsItWithACountOfOne() {
        val grouped = groupConsecutiveCalls(entries = listOf(callLogEntry(id = 1L)))

        assertEquals(listOf(1L), grouped.map { entry -> entry.entryId.value })
        assertEquals(listOf(1), grouped.map { entry -> entry.groupedCallCount })
    }

    @Test
    fun invoke_withConsecutiveCallsToTheSameNumber_collapsesThemOntoTheMostRecentCall() {
        val entries = listOf(
            callLogEntry(id = 3L, number = SHARED_NUMBER, timestampMillis = LATEST_MILLIS),
            callLogEntry(id = 2L, number = SHARED_NUMBER, timestampMillis = LATEST_MILLIS - 1L),
            callLogEntry(id = 1L, number = SHARED_NUMBER, timestampMillis = LATEST_MILLIS - 2L),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(1, grouped.size)
        assertEquals(3L, grouped.first().entryId.value)
        assertEquals(LATEST_MILLIS, grouped.first().timestampMillis)
        assertEquals(3, grouped.first().groupedCallCount)
    }

    @Test
    fun invoke_withCallsToDifferentNumbers_keepsThemInSeparateEntries() {
        val entries = listOf(
            callLogEntry(id = 2L, number = SHARED_NUMBER),
            callLogEntry(id = 1L, number = OTHER_NUMBER),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(listOf(2L, 1L), grouped.map { entry -> entry.entryId.value })
        assertEquals(listOf(1, 1), grouped.map { entry -> entry.groupedCallCount })
    }

    @Test
    fun invoke_whenTheRunIsInterrupted_startsANewRunForTheSameNumber() {
        val entries = listOf(
            callLogEntry(id = 4L, number = SHARED_NUMBER),
            callLogEntry(id = 3L, number = SHARED_NUMBER),
            callLogEntry(id = 2L, number = OTHER_NUMBER),
            callLogEntry(id = 1L, number = SHARED_NUMBER),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(listOf(4L, 2L, 1L), grouped.map { entry -> entry.entryId.value })
        assertEquals(listOf(2, 1, 1), grouped.map { entry -> entry.groupedCallCount })
    }

    @Test
    fun invoke_withAHundredConsecutiveCallsToOneNumber_countsThemAllOnOneEntry() {
        val entries = (HUNDRED_CALLS downTo 1).map { id ->
            callLogEntry(
                id = id.toLong(),
                number = SHARED_NUMBER,
                callType = CallType.Missed,
                isRead = false,
            )
        }

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(1, grouped.size)
        assertEquals(HUNDRED_CALLS, grouped.first().groupedCallCount)
        assertEquals(HUNDRED_CALLS.toLong(), grouped.first().entryId.value)
    }

    @Test
    fun invoke_withAnsweredOutgoingAndMissedCallsToOneNumber_groupsThemAll() {
        val entries = listOf(
            callLogEntry(id = 3L, number = SHARED_NUMBER, callType = CallType.Missed),
            callLogEntry(id = 2L, number = SHARED_NUMBER, callType = CallType.Outgoing),
            callLogEntry(id = 1L, number = SHARED_NUMBER, callType = CallType.Answered),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(1, grouped.size)
        assertEquals(3, grouped.first().groupedCallCount)
    }

    @Test
    fun invoke_withAVoicemailCall_neverGroupsIt() {
        val entries = listOf(
            callLogEntry(id = 3L, number = SHARED_NUMBER),
            callLogEntry(id = 2L, number = SHARED_NUMBER, callType = CallType.Voicemail),
            callLogEntry(id = 1L, number = SHARED_NUMBER),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(listOf(3L, 2L, 1L), grouped.map { entry -> entry.entryId.value })
        assertEquals(listOf(1, 1, 1), grouped.map { entry -> entry.groupedCallCount })
    }

    @Test
    fun invoke_withTwoBlockedCalls_groupsThem() {
        val entries = listOf(
            callLogEntry(id = 2L, number = SHARED_NUMBER, callType = CallType.Blocked),
            callLogEntry(id = 1L, number = SHARED_NUMBER, callType = CallType.Blocked),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(1, grouped.size)
        assertEquals(2, grouped.first().groupedCallCount)
    }

    @Test
    fun invoke_withABlockedCallBesideAnAnsweredCall_keepsThemInSeparateEntries() {
        val entries = listOf(
            callLogEntry(id = 2L, number = SHARED_NUMBER, callType = CallType.Blocked),
            callLogEntry(id = 1L, number = SHARED_NUMBER),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(2, grouped.size)
    }

    @Test
    fun invoke_withAnAnsweredCallBesideABlockedCall_keepsThemInSeparateEntries() {
        val entries = listOf(
            callLogEntry(id = 2L, number = SHARED_NUMBER),
            callLogEntry(id = 1L, number = SHARED_NUMBER, callType = CallType.Blocked),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(2, grouped.size)
    }

    @Test
    fun invoke_withDifferentAssistedDialingBits_keepsTheCallsInSeparateEntries() {
        val entries = listOf(
            callLogEntry(
                id = 2L,
                number = SHARED_NUMBER,
                features = TelephonyManagerCompat.FEATURES_ASSISTED_DIALING,
            ),
            callLogEntry(id = 1L, number = SHARED_NUMBER),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(2, grouped.size)
    }

    @Test
    fun invoke_withAVideoCallBesideAVoiceCall_keepsThemInSeparateEntries() {
        val entries = listOf(
            callLogEntry(id = 2L, number = SHARED_NUMBER, features = Calls.FEATURES_VIDEO),
            callLogEntry(id = 1L, number = SHARED_NUMBER),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(2, grouped.size)
    }

    @Test
    fun invoke_withAnUnknownCallType_groupsItLikeAnyOtherType() {
        val unknown = CallType.Unknown(rawType = UNKNOWN_RAW_CALL_TYPE)
        val entries = listOf(
            callLogEntry(id = 2L, number = SHARED_NUMBER, callType = unknown),
            callLogEntry(id = 1L, number = SHARED_NUMBER, callType = unknown),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(1, grouped.size)
        assertEquals(2, grouped.first().groupedCallCount)
    }

    @Test
    fun invoke_withRowsDifferingOnlyInNonMergeTerms_groupsThem() {
        val entries = listOf(
            callLogEntry(
                id = 3L,
                number = SHARED_NUMBER,
                numberPresentation = Calls.PRESENTATION_PAYPHONE,
            ),
            callLogEntry(id = 3L, number = SHARED_NUMBER, timestampMillis = Long.MAX_VALUE),
            callLogEntry(id = 2L, number = SHARED_NUMBER, durationSeconds = -1L),
            callLogEntry(id = 1L, number = SHARED_NUMBER, timestampMillis = 0L, isRead = false),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(1, grouped.size)
        assertEquals(3L, grouped.first().entryId.value)
        assertEquals(entries.size, grouped.first().groupedCallCount)
    }

    @Test
    fun invoke_whenTheInputAlreadyCarriesACount_replacesItWithTheRunSize() {
        val entries = listOf(
            callLogEntry(id = 3L, number = SHARED_NUMBER, groupedCallCount = PRESET_COUNT),
            callLogEntry(id = 2L, number = SHARED_NUMBER, groupedCallCount = PRESET_COUNT),
            callLogEntry(id = 1L, number = OTHER_NUMBER, groupedCallCount = PRESET_COUNT),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(listOf(2, 1), grouped.map { entry -> entry.groupedCallCount })
    }

    private companion object {
        private const val SHARED_NUMBER = "+15551234567"
        private const val OTHER_NUMBER = "+15559876543"
        private const val HUNDRED_CALLS = 100
        private const val UNKNOWN_RAW_CALL_TYPE = 42
        private const val PRESET_COUNT = 9
        private const val LATEST_MILLIS = TEST_TIMESTAMP_MILLIS
    }
}
