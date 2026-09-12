package com.android.dialer.data.recents.repository.recentsrepositoryimpl

import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import com.android.dialer.data.recents.model.CallType
import com.android.dialer.data.recents.repository.RecentsRepositoryImpl
import com.android.dialer.testutil.TEST_CALL_DURATION_SECONDS
import com.android.dialer.testutil.TEST_TIMESTAMP_MILLIS
import com.android.dialer.testutil.callLogCursor
import com.android.dialer.testutil.callLogRow
import io.mockk.every
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsRepositoryImplMappingTest : BaseRecentsRepositoryImplTest() {

    @Test
    fun observeSnapshot_withRows_mapsEveryColumnFromTheProjection() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            stubCallLogQuery(
                rows = listOf(
                    callLogRow(
                        id = 1L,
                        number = " +15550001 ",
                        formattedNumber = "+1 555-0001",
                        countryIso = "JM",
                        numberPresentation = CallLog.Calls.PRESENTATION_ALLOWED,
                        callType = CallLog.Calls.MISSED_TYPE,
                        duration = TEST_CALL_DURATION_SECONDS,
                        features = CallLog.Calls.FEATURES_VIDEO,
                        geocodedLocation = "Kingston, Jamaica",
                        cachedName = "Ada",
                        cachedPhotoUri = "content://photo/1",
                        cachedLookupUri = "content://contacts/lookup/1",
                        isRead = 0,
                    ),
                    callLogRow(id = 2L, cachedName = "   ", geocodedLocation = ""),
                ),
            )
            stubObserverRegistration()

            val snapshot = createRepository().observeSnapshot().first()

            assertEquals(2, snapshot.entries.size)
            assertTrue(snapshot.isPermissionGranted)

            val first = snapshot.entries.first()
            assertEquals(1L, first.entryId.value)
            assertEquals("+15550001", first.number)
            assertEquals("+1 555-0001", first.formattedNumber)
            assertEquals("JM", first.countryIso)
            assertEquals(CallLog.Calls.PRESENTATION_ALLOWED, first.numberPresentation)
            assertEquals("Kingston, Jamaica", first.geocodedLocation)
            assertEquals("Ada", first.cachedName)
            assertEquals("content://photo/1", first.photoUri)
            assertEquals("content://contacts/lookup/1", first.lookupUri)
            assertEquals(TEST_TIMESTAMP_MILLIS + 1L, first.timestampMillis)
            assertEquals(TEST_CALL_DURATION_SECONDS, first.durationSeconds)
            assertEquals(CallType.Missed, first.callType)
            assertTrue(first.isVideoCall)
            assertFalse(first.isRead)
            assertEquals(1, first.groupedCallCount)

            val second = snapshot.entries.last()
            assertNull(second.cachedName)
            assertNull(second.geocodedLocation)
            assertFalse(second.isVideoCall)
        }
    }

    @Test
    fun observeSnapshot_queriesExactlyTheProductionProjection() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            stubCallLogQuery(rows = listOf(callLogRow(id = 1L)))
            stubObserverRegistration()

            createRepository().observeSnapshot().first()

            assertEquals(
                RecentsRepositoryImpl.CALL_LOG_PROJECTION.toList(),
                capturedProjections.single()?.toList(),
            )
        }
    }

    @Test
    fun observeSnapshot_mapsEveryPlatformCallType() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            stubCallLogQuery(
                rows = listOf(
                    callLogRow(id = 1L, callType = CallLog.Calls.INCOMING_TYPE),
                    callLogRow(id = 2L, callType = CallLog.Calls.OUTGOING_TYPE),
                    callLogRow(id = 3L, callType = CallLog.Calls.MISSED_TYPE),
                    callLogRow(id = 4L, callType = CallLog.Calls.REJECTED_TYPE),
                    callLogRow(id = 5L, callType = CallLog.Calls.BLOCKED_TYPE),
                    callLogRow(id = 6L, callType = CallLog.Calls.VOICEMAIL_TYPE),
                    callLogRow(id = 7L, callType = CallLog.Calls.ANSWERED_EXTERNALLY_TYPE),
                ),
            )
            stubObserverRegistration()

            val snapshot = createRepository().observeSnapshot().first()

            assertEquals(
                listOf(
                    CallType.Answered,
                    CallType.Outgoing,
                    CallType.Missed,
                    CallType.Rejected,
                    CallType.Blocked,
                    CallType.Voicemail,
                    CallType.Answered,
                ),
                snapshot.entries.map { entry -> entry.callType },
            )
        }
    }

    @Test
    fun observeSnapshot_withAnUnknownCallType_keepsTheRawTypeOnTheEntry() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            stubCallLogQuery(rows = listOf(callLogRow(id = 1L, callType = UNKNOWN_CALL_TYPE)))
            stubObserverRegistration()

            val snapshot = createRepository().observeSnapshot().first()

            assertEquals(
                CallType.Unknown(rawType = UNKNOWN_CALL_TYPE),
                snapshot.entries.single().callType,
            )
        }
    }

    @Test
    fun observeSnapshot_withBlankOrNullOptionalColumns_normalisesThemToEmptyAndNull() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            stubCallLogQuery(
                rows = listOf(
                    callLogRow(
                        id = 1L,
                        number = null,
                        formattedNumber = " ",
                        countryIso = "",
                        cachedName = "",
                        cachedPhotoUri = "  ",
                        cachedLookupUri = "",
                        geocodedLocation = "  ",
                    ),
                ),
            )
            stubObserverRegistration()

            val snapshot = createRepository().observeSnapshot().first()

            val entry = snapshot.entries.single()
            assertEquals("", entry.number)
            assertNull(entry.formattedNumber)
            assertNull(entry.countryIso)
            assertNull(entry.cachedName)
            assertNull(entry.photoUri)
            assertNull(entry.lookupUri)
            assertNull(entry.geocodedLocation)
        }
    }

    @Test
    fun observeSnapshot_withRowsWhoseIdIsNotPositive_dropsThemAndKeepsTheRest() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            stubCallLogQuery(
                rows = listOf(callLogRow(id = 0L), callLogRow(id = -5L), callLogRow(id = 2L)),
            )
            stubObserverRegistration()

            val snapshot = createRepository().observeSnapshot().first()

            assertEquals(listOf(2L), snapshot.entries.map { entry -> entry.entryId.value })
        }
    }

    @Test
    fun observeSnapshot_withNullCursor_emitsAnEmptyGrantedSnapshot() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            every { contentResolver.query(any(), any(), any<Bundle>(), any()) } returns null
            stubObserverRegistration()

            val snapshot = createRepository().observeSnapshot().first()

            assertTrue(snapshot.entries.isEmpty())
            assertTrue(snapshot.isPermissionGranted)
        }
    }

    @Test
    fun observeSnapshot_withAnEmptyCursor_emitsAnEmptyGrantedSnapshot() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            stubCallLogQuery(rows = emptyList())
            stubObserverRegistration()

            val snapshot = createRepository().observeSnapshot().first()

            assertTrue(snapshot.entries.isEmpty())
            assertTrue(snapshot.isPermissionGranted)
        }
    }

    private companion object {
        private const val UNKNOWN_CALL_TYPE = 99
    }
}
