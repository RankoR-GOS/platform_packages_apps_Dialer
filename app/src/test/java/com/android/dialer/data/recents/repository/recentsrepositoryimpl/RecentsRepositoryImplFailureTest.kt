package com.android.dialer.data.recents.repository.recentsrepositoryimpl

import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteDiskIOException
import android.database.sqlite.SQLiteFullException
import android.os.Build
import android.os.Bundle
import app.cash.turbine.test
import com.android.dialer.data.recents.model.CallLogFilter
import com.android.dialer.testutil.callLogRow
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsRepositoryImplFailureTest : BaseRecentsRepositoryImplTest() {

    @Test
    fun observeSnapshot_whenTheProviderThrowsSqliteDiskIo_showsTheEmptyStateThenKeepsObserving() {
        assertFailureShowsTheEmptyState(error = SQLiteDiskIOException("disk io"))
    }

    @Test
    fun observeSnapshot_whenTheDiskIsFull_showsTheEmptyStateThenKeepsObserving() {
        assertFailureShowsTheEmptyState(error = SQLiteFullException("disk full"))
    }

    @Test
    fun observeSnapshot_whenTheCallLogIsCorrupt_showsTheEmptyStateThenKeepsObserving() {
        assertFailureShowsTheEmptyState(error = SQLiteDatabaseCorruptException("corrupt"))
    }

    @Test
    fun observeSnapshot_whenTheProviderRejectsTheQuery_showsTheEmptyStateThenKeepsObserving() {
        assertFailureShowsTheEmptyState(error = IllegalArgumentException("Invalid token"))
    }

    @Test
    fun observeSnapshot_whenALaterReadFails_keepsTheRows() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubCallLogQuery(rows = listOf(callLogRow(id = 1L)))
            val observerSlot = stubObserverRegistration()

            createRepository().observeSnapshot(filter = CallLogFilter.All).test {
                assertEquals(1, awaitItem().entries.size)

                stubQueryThrows(error = SQLiteDiskIOException("disk io"))
                observerSlot.captured.onChange(false)
                advanceUntilIdle()

                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun observeSnapshot_whenTheFirstReadFailsThenRecovers_replacesTheEmptyState() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubQueryThrows(error = SQLiteDiskIOException("disk io"))
            val observerSlot = stubObserverRegistration()

            createRepository().observeSnapshot(filter = CallLogFilter.All).test {
                assertTrue(awaitItem().entries.isEmpty())

                stubCallLogQuery(rows = listOf(callLogRow(id = 1L)))
                observerSlot.captured.onChange(false)

                assertEquals(1, awaitItem().entries.size)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun observeSnapshot_whenTheProviderThrowsSecurityException_reportsThePermissionAsRevoked() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubQueryThrows(error = SecurityException("revoked mid-query"))
            stubObserverRegistration()

            val snapshot = createRepository().observeSnapshot(filter = CallLogFilter.All).first()

            assertFalse(snapshot.isPermissionGranted)
            assertTrue(snapshot.entries.isEmpty())
        }
    }

    @Test
    fun observeSnapshot_whenTheProviderThrowsIllegalState_propagates() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubQueryThrows(error = IllegalStateException(PROGRAMMING_ERROR))
            stubObserverRegistration()

            try {
                createRepository().observeSnapshot(filter = CallLogFilter.All).first()
                fail("expected the IllegalStateException to propagate")
            } catch (e: IllegalStateException) {
                assertEquals(PROGRAMMING_ERROR, e.message)
            }
        }
    }

    @Test
    fun observeSnapshot_withoutTheCallLogPermission_reportsItWithoutQuerying() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubObserverRegistration()

            val snapshot = createRepository(isCallLogGranted = false)
                .observeSnapshot(filter = CallLogFilter.All)
                .first()

            assertFalse(snapshot.isPermissionGranted)
            assertTrue(snapshot.entries.isEmpty())
            verify(exactly = 0) { contentResolver.query(any(), any(), any<Bundle>(), any()) }
        }
    }

    private fun assertFailureShowsTheEmptyState(error: Throwable) {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubQueryThrows(error = error)
            val observerSlot = stubObserverRegistration()

            createRepository().observeSnapshot(filter = CallLogFilter.All).test {
                val shown = awaitItem()

                assertTrue(shown.isPermissionGranted)
                assertTrue(shown.entries.isEmpty())

                observerSlot.captured.onChange(false)
                advanceUntilIdle()
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
            verify(exactly = 2) { contentResolver.query(any(), any(), any<Bundle>(), any()) }
        }
    }

    private companion object {
        private const val PROGRAMMING_ERROR = "the repository read a column it never asked for"
    }
}
