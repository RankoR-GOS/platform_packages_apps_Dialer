package com.android.dialer.data.recents.repository.recentsrepositoryimpl

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteDiskIOException
import android.database.sqlite.SQLiteFullException
import android.os.Build
import android.provider.CallLog
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.data.recents.model.RecentsWriteFailure
import com.android.dialer.data.recents.model.RecentsWriteResult
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import java.util.concurrent.Executors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
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
internal class RecentsRepositoryImplWritesTest : BaseRecentsRepositoryImplTest() {

    private val writeDispatcher = Executors
        .newSingleThreadExecutor { runnable -> Thread(runnable, WRITE_THREAD_NAME) }
        .asCoroutineDispatcher()

    @After
    fun tearDown() {
        writeDispatcher.close()
    }

    @Test
    fun delete_deletesTheGivenIdsThroughTheCallLogUri() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            every { contentResolver.delete(any(), any(), any()) } returns 2

            val result = createRepository().delete(
                entryIds = listOf(CallLogEntryId(value = 1L), CallLogEntryId(value = 2L)),
            )

            assertEquals(RecentsWriteResult.Completed, result)
            verify(exactly = 1) {
                contentResolver.delete(
                    CallLog.Calls.CONTENT_URI,
                    "${CallLog.Calls._ID} IN (1,2)",
                    null,
                )
            }
        }
    }

    @Test
    fun delete_withEmptyInput_doesNotTouchTheProvider() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            val result = createRepository().delete(entryIds = emptyList())

            assertEquals(RecentsWriteResult.Completed, result)
            verify(exactly = 0) { contentResolver.delete(any(), any(), any()) }
        }
    }

    @Test
    fun delete_runsOnTheInjectedDispatcher() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            var writeThreadName: String? = null
            every { contentResolver.delete(any(), any(), any()) } answers {
                writeThreadName = Thread.currentThread().name
                1
            }

            createRepository(dispatcher = writeDispatcher)
                .delete(entryIds = listOf(CallLogEntryId(value = 1L)))

            assertTrue(
                "expected the delete on $WRITE_THREAD_NAME, it ran on $writeThreadName",
                writeThreadName.orEmpty().startsWith(WRITE_THREAD_NAME),
            )
        }
    }

    @Test
    fun delete_whenTheResolverThrowsSecurityException_returnsFailedPermissionRevoked() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            every { contentResolver.delete(any(), any(), any()) } throws SecurityException("no")

            val result = createRepository().delete(entryIds = listOf(CallLogEntryId(value = 1L)))

            assertEquals(
                RecentsWriteResult.Failed(cause = RecentsWriteFailure.PermissionRevoked),
                result,
            )
        }
    }

    @Test
    fun delete_whenTheResolverThrowsSqliteFull_returnsFailedStorage() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            every {
                contentResolver.delete(any(), any(), any())
            } throws SQLiteFullException("disk full")

            val result = createRepository().delete(entryIds = listOf(CallLogEntryId(value = 1L)))

            assertEquals(RecentsWriteResult.Failed(cause = RecentsWriteFailure.Storage), result)
        }
    }

    @Test
    fun delete_whenTheResolverThrowsIllegalState_propagates() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            every {
                contentResolver.delete(any(), any(), any())
            } throws IllegalStateException(PROGRAMMING_ERROR)

            try {
                createRepository().delete(entryIds = listOf(CallLogEntryId(value = 1L)))
                fail("expected the IllegalStateException to propagate")
            } catch (e: IllegalStateException) {
                assertEquals(PROGRAMMING_ERROR, e.message)
            }
        }
    }

    @Test
    fun markRead_setsIsReadOnTheGivenIdsAndNothingElse() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            val values = slot<ContentValues>()
            every { contentResolver.update(any(), capture(values), any(), any()) } returns 1

            val result = createRepository().markRead(
                entryIds = listOf(CallLogEntryId(value = 1L)),
            )

            assertEquals(RecentsWriteResult.Completed, result)
            assertEquals(1, values.captured.getAsInteger(CallLog.Calls.IS_READ))
            assertFalse(values.captured.containsKey(CallLog.Calls.NEW))
            assertEquals(1, values.captured.size())
            verify(exactly = 1) {
                contentResolver.update(
                    CallLog.Calls.CONTENT_URI,
                    any(),
                    "${CallLog.Calls._ID} IN (1)",
                    null,
                )
            }
        }
    }

    @Test
    fun markRead_whenTheCallLogIsCorrupt_returnsFailedStorage() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            every {
                contentResolver.update(any(), any(), any(), any())
            } throws SQLiteDatabaseCorruptException("corrupt")

            val result = createRepository().markRead(
                entryIds = listOf(CallLogEntryId(value = 1L)),
            )

            assertEquals(RecentsWriteResult.Failed(cause = RecentsWriteFailure.Storage), result)
        }
    }

    @Test
    fun markRead_withEmptyInput_doesNotTouchTheProvider() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            val result = createRepository().markRead(entryIds = emptyList())

            assertEquals(RecentsWriteResult.Completed, result)
            verify(exactly = 0) { contentResolver.update(any(), any(), any(), any()) }
        }
    }

    private companion object {
        private const val WRITE_THREAD_NAME = "recents-write"
        private const val PROGRAMMING_ERROR = "the provider handle was already closed"
    }
}
