package com.android.dialer.data.recents.repository.syntheticrecentsrepository

import android.provider.CallLog
import app.cash.turbine.test
import com.android.dialer.data.recents.model.CallLogEntry
import com.android.dialer.data.recents.model.CallLogFilter
import com.android.dialer.data.recents.model.CallType
import com.android.dialer.data.recents.repository.SyntheticCallLogScenario
import com.android.dialer.data.recents.repository.SyntheticRecentsRepository
import io.mockk.every
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class SyntheticRecentsRepositorySnapshotTest : BaseSyntheticRecentsRepositoryTest() {

    @Test
    fun observeSnapshot_withThePopulatedScenario_emitsRowsThatGroup() {
        runTest {
            val snapshot = snapshot(scenario = SyntheticCallLogScenario.Populated)

            assertTrue(snapshot.isPermissionGranted)
            assertTrue(snapshot.entries.size > 1)
            assertTrue(
                snapshot.entries
                    .groupBy { entry -> entry.number }
                    .any { (_, calls) -> calls.size > 1 },
            )
        }
    }

    @Test
    fun observeSnapshot_withTheEmptyScenario_reportsAGrantedAndEmptyLog() {
        runTest {
            val snapshot = snapshot(scenario = SyntheticCallLogScenario.Empty)

            assertTrue(snapshot.isPermissionGranted)
            assertTrue(snapshot.entries.isEmpty())
        }
    }

    @Test
    fun observeSnapshot_withThePermissionDeniedScenario_reportsThePermissionAsDenied() {
        runTest {
            val snapshot = snapshot(scenario = SyntheticCallLogScenario.PermissionDenied)

            assertFalse(snapshot.isPermissionGranted)
            assertTrue(snapshot.entries.isEmpty())
        }
    }

    @Test
    fun observeSnapshot_withTheHostileScenario_carriesEveryRowTheVisualSpecRecords() {
        runTest {
            val entries = snapshot(scenario = SyntheticCallLogScenario.Hostile).entries

            assertTrue(entries.any { entry -> entry.number.length == THIRTY_DIGITS })
            assertTrue(entries.any { entry -> entry.number.any(Char::isLetter) })
            assertTrue(entries.any { entry -> entry.number == "+" })
            assertTrue(entries.any { entry -> entry.durationSeconds < 0L })
            assertTrue(entries.any { entry -> entry.timestampMillis == 0L })
            assertTrue(entries.any { entry -> entry.cachedName?.isBlank() == true })
            assertEquals(
                setOf(
                    CallLog.Calls.PRESENTATION_ALLOWED,
                    CallLog.Calls.PRESENTATION_RESTRICTED,
                    CallLog.Calls.PRESENTATION_UNKNOWN,
                ),
                entries.map(CallLogEntry::numberPresentation).toSet(),
            )
        }
    }

    @Test
    fun observeSnapshot_withTheHostileScenario_carriesARunLongEnoughToShowAGroupCount() {
        runTest {
            val entries = snapshot(scenario = SyntheticCallLogScenario.Hostile).entries
            val longestRun = entries
                .groupBy { entry -> entry.number }
                .maxOf { (_, calls) -> calls.size }

            assertTrue(longestRun >= HOSTILE_RUN_SIZE)
        }
    }

    @Test
    fun observeSnapshot_withTheHostileScenario_carriesATimestampAfterTheSeedInstant() {
        runTest {
            val entries = snapshot(scenario = SyntheticCallLogScenario.Hostile).entries
            val newest = entries.maxOf(CallLogEntry::timestampMillis)
            val oldest = entries.minOf(CallLogEntry::timestampMillis)

            assertTrue(newest > oldest + FUTURE_MARGIN_MILLIS)
        }
    }

    @Test
    fun observeSnapshot_withTheHostileScenario_ordersRowsNewestFirst() {
        runTest {
            val timestamps = snapshot(scenario = SyntheticCallLogScenario.Hostile)
                .entries
                .map { entry -> entry.timestampMillis }

            assertEquals(timestamps.sortedDescending(), timestamps)
        }
    }

    @Test
    fun observeSnapshot_withTheAllFilter_dropsBlockedAndVoicemailRows() {
        runTest {
            val entries = snapshot(scenario = SyntheticCallLogScenario.Hostile).entries

            assertFalse(entries.any { entry -> entry.callType == CallType.Blocked })
            assertFalse(entries.any { entry -> entry.callType == CallType.Voicemail })
        }
    }

    @Test
    fun observeSnapshot_withTheMissedFilter_keepsOnlyMissedCalls() {
        runTest {
            val entries = snapshot(
                scenario = SyntheticCallLogScenario.Hostile,
                filter = CallLogFilter.Missed,
            ).entries

            assertTrue(entries.isNotEmpty())
            assertTrue(entries.all { entry -> entry.callType == CallType.Missed })
        }
    }

    @Test
    fun observeSnapshot_whenCollectedAgain_readsTheSelectedScenarioAgain() {
        runTest {
            every { selectedScenario() } returnsMany listOf(
                SyntheticCallLogScenario.Empty,
                SyntheticCallLogScenario.Populated,
            )
            val repository = SyntheticRecentsRepository(selectedScenario = selectedScenario)

            val first = repository.observeSnapshot(filter = CallLogFilter.All).first()
            val second = repository.observeSnapshot(filter = CallLogFilter.All).first()

            assertTrue(first.entries.isEmpty())
            assertTrue(second.entries.isNotEmpty())
        }
    }

    @Test
    fun observeSnapshot_whenTheSelectedScenarioChanges_reseedsOnRefresh() {
        runTest {
            every { selectedScenario() } returnsMany listOf(
                SyntheticCallLogScenario.Populated,
                SyntheticCallLogScenario.PermissionDenied,
            )
            val repository = SyntheticRecentsRepository(selectedScenario = selectedScenario)

            repository.observeSnapshot(filter = CallLogFilter.All).test {
                assertTrue(awaitItem().isPermissionGranted)

                repository.refresh()

                assertFalse(awaitItem().isPermissionGranted)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun observeSnapshot_withNoScenarioSelected_servesThePopulatedLog() {
        runTest {
            val snapshot = snapshot(scenario = null)

            assertTrue(snapshot.isPermissionGranted)
            assertTrue(snapshot.entries.isNotEmpty())
        }
    }
}
