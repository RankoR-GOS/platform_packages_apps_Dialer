package com.android.dialer.data.recents.repository.syntheticrecentsrepository

import app.cash.turbine.test
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.data.recents.model.CallLogFilter
import com.android.dialer.data.recents.model.RecentsWriteFailure
import com.android.dialer.data.recents.model.RecentsWriteResult
import com.android.dialer.data.recents.repository.SyntheticCallLogScenario
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class SyntheticRecentsRepositoryWritesTest : BaseSyntheticRecentsRepositoryTest() {

    @Test
    fun delete_withTheNamedIds_removesOnlyThoseRowsAndEmitsAgain() {
        runTest {
            val repository = repository(scenario = SyntheticCallLogScenario.Populated)

            repository.observeSnapshot(filter = CallLogFilter.All).test {
                val before = awaitItem().entries
                val removed = before.first().entryId

                assertEquals(
                    RecentsWriteResult.Completed,
                    repository.delete(entryIds = listOf(removed)),
                )

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

            val result = repository.delete(entryIds = emptyList())

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

            val result = repository.delete(entryIds = listOf(CallLogEntryId(value = 1L)))

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

            assertEquals(
                RecentsWriteResult.Completed,
                repository.markRead(entryIds = listOf(unread.entryId)),
            )

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

            val result = repository.markRead(entryIds = emptyList())

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

            repository.delete(entryIds = listOf(before.first().entryId))

            val after = repository.observeSnapshot(filter = CallLogFilter.All).first().entries
            assertEquals(before.size - 1, after.size)
        }
    }
}
