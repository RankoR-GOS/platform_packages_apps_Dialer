package com.android.dialer.ui.recents.screen.recentsviewmodel

import app.cash.turbine.test
import com.android.dialer.ui.recents.model.RecentsContentUiState
import com.android.dialer.ui.recents.model.RecentsUiState
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class RecentsViewModelStateTest : BaseRecentsViewModelTest() {

    @Test
    fun uiState_mapsTheAllFilterSnapshotWithTheCurrentTime() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(RecentsUiState(), awaitItem())
                assertEquals(MAPPED_STATE, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            verify(exactly = 1) { repository.observeSnapshot() }
            verify(exactly = 1) {
                uiStateMapper.map(snapshot = SNAPSHOT, nowMillis = NOW_MILLIS)
            }
        }
    }

    @Test
    fun uiState_afterAMinute_mapsAgainWithTheNewTime() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            val laterState = RecentsUiState(
                content = RecentsContentUiState.Empty(
                    message = "a minute later",
                    actionLabel = "call",
                ),
            )
            every { currentTimeProvider.currentTimeMillis() } returnsMany listOf(
                NOW_MILLIS,
                NOW_MILLIS + MINUTE_MILLIS,
            )
            every { uiStateMapper.map(SNAPSHOT, NOW_MILLIS + MINUTE_MILLIS) } returns laterState
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem()
                assertEquals(MAPPED_STATE, awaitItem())

                advanceTimeBy(MINUTE_MILLIS + 1L)

                assertEquals(laterState, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun uiState_whenACollectorReturnsWithinTheTimeout_keepsTheRepositoryFlow() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            var isClosed = false
            every { repository.observeSnapshot() } returns callbackFlow {
                trySend(SNAPSHOT)
                awaitClose { isClosed = true }
            }
            val viewModel = createViewModel()

            val first = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()
            first.cancel()
            advanceTimeBy(STOP_TIMEOUT_MILLIS - 1L)
            val second = backgroundScope.launch { viewModel.uiState.collect {} }
            advanceTimeBy(STOP_TIMEOUT_MILLIS + 1L)

            assertFalse(isClosed)
            verify(exactly = 1) { repository.observeSnapshot() }
            second.cancel()
        }
    }

    @Test
    fun uiState_whenTheLastCollectorLeaves_stopsTheRepositoryFlowAfterTheTimeout() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            var isClosed = false
            every { repository.observeSnapshot() } returns callbackFlow {
                trySend(SNAPSHOT)
                awaitClose { isClosed = true }
            }
            val viewModel = createViewModel()

            val collector = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()
            collector.cancel()
            advanceTimeBy(STOP_TIMEOUT_MILLIS - 1L)
            assertFalse(isClosed)

            advanceTimeBy(2L)

            assertTrue(isClosed)
        }
    }
}
