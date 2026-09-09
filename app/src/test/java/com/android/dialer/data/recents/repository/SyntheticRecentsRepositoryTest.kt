package com.android.dialer.data.recents.repository

import android.provider.CallLog
import app.cash.turbine.test
import com.android.dialer.data.recents.model.CallLogEntry
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.data.recents.model.CallLogFilter
import com.android.dialer.data.recents.model.CallType
import com.android.dialer.data.recents.model.RecentsWriteFailure
import com.android.dialer.data.recents.model.RecentsWriteResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class SyntheticRecentsRepositoryTest {

    private val selectedScenario = mockk<SyntheticCallLogScenarioSource>()

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
    fun delete_withTheNamedIds_removesOnlyThoseRowsAndEmitsAgain() {
        runTest {
            val repository = repository(scenario = SyntheticCallLogScenario.Populated)

            repository.observeSnapshot(filter = CallLogFilter.All).test {
                val before = awaitItem().entries
                val removed = before.first().entryId

                assertEquals(RecentsWriteResult.Completed, repository.delete(listOf(removed)))

                val after = awaitItem().entries
                assertEquals(before.size - 1, after.size)
                assertFalse(after.any { entry -> entry.entryId == removed })
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun delete_withAnEmptyList_reportsCompletedWithoutTouchingTheLog() {
        runTest {
            val repository = repository(scenario = SyntheticCallLogScenario.Populated)
            val before = repository.observeSnapshot(filter = CallLogFilter.All).first().entries

            val result = repository.delete(emptyList())

            assertEquals(RecentsWriteResult.Completed, result)
            assertEquals(
                before.size,
                repository.observeSnapshot(filter = CallLogFilter.All).first().entries.size,
            )
        }
    }

    @Test
    fun delete_withThePermissionDeniedScenario_reportsTheRevokedPermission() {
        runTest {
            val repository = repository(scenario = SyntheticCallLogScenario.PermissionDenied)
            repository.observeSnapshot(filter = CallLogFilter.All).first()

            val result = repository.delete(listOf(CallLogEntryId(value = 1L)))

            assertEquals(
                RecentsWriteResult.Failed(cause = RecentsWriteFailure.PermissionRevoked),
                result,
            )
        }
    }

    @Test
    fun markRead_withTheNamedIds_marksOnlyThoseRowsRead() {
        runTest {
            val repository = repository(scenario = SyntheticCallLogScenario.Populated)
            val before = repository.observeSnapshot(filter = CallLogFilter.All).first().entries
            val unread = before.first { entry -> !entry.isRead }

            assertEquals(RecentsWriteResult.Completed, repository.markRead(listOf(unread.entryId)))

            val after = repository.observeSnapshot(filter = CallLogFilter.All).first().entries
            assertTrue(after.first { entry -> entry.entryId == unread.entryId }.isRead)
            assertNotEquals(
                before.count { entry -> entry.isRead },
                after.count { entry -> entry.isRead },
            )
        }
    }

    @Test
    fun markRead_withAnEmptyList_reportsCompletedWithoutTouchingTheLog() {
        runTest {
            val repository = repository(scenario = SyntheticCallLogScenario.Populated)
            val before = repository.observeSnapshot(filter = CallLogFilter.All).first().entries

            val result = repository.markRead(emptyList())

            assertEquals(RecentsWriteResult.Completed, result)
            assertEquals(
                before.count { entry -> entry.isRead },
                repository.observeSnapshot(filter = CallLogFilter.All)
                    .first()
                    .entries
                    .count { entry -> entry.isRead },
            )
        }
    }

    @Test
    fun clearHistory_withThePermissionDeniedScenario_reportsTheRevokedPermission() {
        runTest {
            val repository = repository(scenario = SyntheticCallLogScenario.PermissionDenied)
            repository.observeSnapshot(filter = CallLogFilter.All).first()

            assertEquals(
                RecentsWriteResult.Failed(cause = RecentsWriteFailure.PermissionRevoked),
                repository.clearHistory(),
            )
        }
    }

    @Test
    fun clearHistory_emptiesTheLogAndKeepsThePermission() {
        runTest {
            val repository = repository(scenario = SyntheticCallLogScenario.Populated)

            repository.observeSnapshot(filter = CallLogFilter.All).test {
                assertTrue(awaitItem().entries.isNotEmpty())

                assertEquals(RecentsWriteResult.Completed, repository.clearHistory())

                val cleared = awaitItem()
                assertTrue(cleared.entries.isEmpty())
                assertTrue(cleared.isPermissionGranted)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun observeSnapshot_afterADelete_keepsTheEditOnTheNextCollection() {
        runTest {
            val repository = repository(scenario = SyntheticCallLogScenario.Populated)
            val before = repository.observeSnapshot(filter = CallLogFilter.All).first().entries

            repository.delete(listOf(before.first().entryId))

            val after = repository.observeSnapshot(filter = CallLogFilter.All).first().entries
            assertEquals(before.size - 1, after.size)
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

    private fun repository(scenario: SyntheticCallLogScenario?): SyntheticRecentsRepository {
        every { selectedScenario() } returns scenario

        return SyntheticRecentsRepository(selectedScenario = selectedScenario)
    }

    private suspend fun snapshot(
        scenario: SyntheticCallLogScenario?,
        filter: CallLogFilter = CallLogFilter.All,
    ) = repository(scenario = scenario).observeSnapshot(filter = filter).first()

    private companion object {
        private const val THIRTY_DIGITS = 30
        private const val HOSTILE_RUN_SIZE = 100
        private const val FUTURE_MARGIN_MILLIS = 1_806_240_000_000L
    }
}
