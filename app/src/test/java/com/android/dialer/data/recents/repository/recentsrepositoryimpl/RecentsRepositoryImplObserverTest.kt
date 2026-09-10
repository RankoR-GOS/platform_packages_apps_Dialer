package com.android.dialer.data.recents.repository.recentsrepositoryimpl

import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import app.cash.turbine.test
import com.android.dialer.testutil.callLogCursor
import com.android.dialer.testutil.callLogRow
import io.mockk.every
import io.mockk.verify
import java.util.concurrent.Executors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsRepositoryImplObserverTest : BaseRecentsRepositoryImplTest() {

    private val queryDispatcher = Executors
        .newSingleThreadExecutor { runnable -> Thread(runnable, QUERY_THREAD_NAME) }
        .asCoroutineDispatcher()

    @After
    fun tearDown() {
        queryDispatcher.close()
    }

    @Test
    fun observeSnapshot_whenCollected_registersAContentObserverForDescendants() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubCallLogQuery(rows = emptyList())
            stubObserverRegistration()

            createRepository().observeSnapshot().first()

            verify(exactly = 1) {
                contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, any())
            }
        }
    }

    @Test
    fun observeSnapshot_withoutTheCallLogPermission_registersNoObserver() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubObserverRegistration()

            val snapshot = createRepository(isCallLogGranted = false)
                .observeSnapshot()
                .first()

            assertFalse(snapshot.isPermissionGranted)
            verify(exactly = 0) { contentResolver.registerContentObserver(any(), any(), any()) }
        }
    }

    @Test
    fun observeSnapshot_whenThePermissionArrivesWithARefresh_registersTheObserverAndQueries() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubCallLogQuery(rows = listOf(callLogRow(id = 1L)))
            stubObserverRegistration()
            val repository = createRepository(isCallLogGranted = false)

            repository.observeSnapshot().test {
                assertFalse(awaitItem().isPermissionGranted)

                every { isCallLogPermissionGranted() } returns true
                repository.refresh()

                assertEquals(1L, awaitItem().entries.single().entryId.value)
                cancelAndIgnoreRemainingEvents()
            }

            verify(exactly = 1) {
                contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, any())
            }
        }
    }

    @Test
    fun observeSnapshot_onContentChange_requeriesAndEmitsAgain() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubCallLogQuery(rows = listOf(callLogRow(id = 1L)))
            val observerSlot = stubObserverRegistration()

            createRepository().observeSnapshot().test {
                assertEquals(1L, awaitItem().entries.single().entryId.value)

                observerSlot.captured.onChange(false)

                assertEquals(1L, awaitItem().entries.single().entryId.value)
                cancelAndIgnoreRemainingEvents()
            }

            verify(exactly = 2) { contentResolver.query(any(), any(), any<Bundle>(), any()) }
        }
    }

    @Test
    fun observeSnapshot_onCancellation_unregistersTheContentObserverItRegistered() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubCallLogQuery(rows = emptyList())
            val observerSlot = stubObserverRegistration()

            createRepository().observeSnapshot().first()
            advanceUntilIdle()

            verify(exactly = 1) {
                contentResolver.unregisterContentObserver(observerSlot.captured)
            }
        }
    }

    @Test
    fun observeSnapshot_runsTheQueryOnTheInjectedDispatcher() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            var queryThreadName: String? = null
            every { contentResolver.query(any(), any(), any<Bundle>(), any()) } answers {
                queryThreadName = Thread.currentThread().name
                callLogCursor(callLogRow(id = 1L))
            }
            stubObserverRegistration()

            createRepository(dispatcher = queryDispatcher)
                .observeSnapshot()
                .first()

            assertTrue(
                "expected the query on $QUERY_THREAD_NAME, it ran on $queryThreadName",
                queryThreadName.orEmpty().startsWith(QUERY_THREAD_NAME),
            )
        }
    }

    @Test
    fun refresh_beforeAnyCollection_isDroppedRatherThanReplayed() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubCallLogQuery(rows = listOf(callLogRow(id = 1L)))
            stubObserverRegistration()
            val repository = createRepository()

            repository.refresh()
            repository.observeSnapshot().first()
            advanceUntilIdle()

            verify(exactly = 1) { contentResolver.query(any(), any(), any<Bundle>(), any()) }
        }
    }

    private companion object {
        private const val QUERY_THREAD_NAME = "recents-query"
    }
}
