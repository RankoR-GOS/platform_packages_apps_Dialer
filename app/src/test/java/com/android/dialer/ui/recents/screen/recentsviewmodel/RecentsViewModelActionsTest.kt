package com.android.dialer.ui.recents.screen.recentsviewmodel

import app.cash.turbine.test
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.data.recents.model.RecentsWriteFailure
import com.android.dialer.data.recents.model.RecentsWriteResult
import com.android.dialer.ui.recents.model.RecentsAction as Action
import com.android.dialer.ui.recents.model.RecentsEffect as Effect
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class RecentsViewModelActionsTest : BaseRecentsViewModelTest() {

    @Test
    fun screenResumed_refreshesTheCallLog() {
        every { repository.refresh() } just runs

        createViewModel().onAction(Action.ScreenResumed)

        verify(exactly = 1) { repository.refresh() }
    }

    @Test
    fun callLogPermissionGranted_refreshesTheCallLog() {
        every { repository.refresh() } just runs

        createViewModel().onAction(Action.CallLogPermissionGranted)

        verify(exactly = 1) { repository.refresh() }
    }

    @Test
    fun grantPermissionClicked_emitsRequestCallLogPermission() {
        assertEffect(Action.GrantPermissionClicked, Effect.RequestCallLogPermission)
    }

    @Test
    fun callBackClicked_emitsPlaceCallWithThatNumber() {
        assertEffect(Action.CallBackClicked(number = NUMBER), Effect.PlaceCall(number = NUMBER))
    }

    @Test
    fun videoCallClicked_emitsPlaceVideoCallWithThatNumber() {
        assertEffect(
            Action.VideoCallClicked(number = NUMBER),
            Effect.PlaceVideoCall(number = NUMBER),
        )
    }

    @Test
    fun messageClicked_emitsSendMessageWithThatNumber() {
        assertEffect(Action.MessageClicked(number = NUMBER), Effect.SendMessage(number = NUMBER))
    }

    @Test
    fun addContactClicked_emitsAddContactWithThatNumber() {
        assertEffect(Action.AddContactClicked(number = NUMBER), Effect.AddContact(number = NUMBER))
    }

    @Test
    fun copyNumberClicked_emitsCopyNumberWithThatNumber() {
        assertEffect(Action.CopyNumberClicked(number = NUMBER), Effect.CopyNumber(number = NUMBER))
    }

    @Test
    fun deleteConfirmed_deletesThatOneEntryAndRaisesNothing() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            coEvery { repository.delete(listOf(ENTRY_ID)) } returns RecentsWriteResult.Completed
            val viewModel = createViewModel()

            viewModel.effects.test {
                viewModel.onAction(Action.DeleteConfirmed(entryId = ENTRY_ID))
                advanceUntilIdle()

                expectNoEvents()
            }
            coVerify(exactly = 1) { repository.delete(listOf(ENTRY_ID)) }
        }
    }

    @Test
    fun deleteConfirmed_whenTheDeleteFails_emitsWriteFailed() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            coEvery { repository.delete(any()) } returns
                RecentsWriteResult.Failed(cause = RecentsWriteFailure.Storage)
            val viewModel = createViewModel()

            viewModel.effects.test {
                viewModel.onAction(Action.DeleteConfirmed(entryId = ENTRY_ID))
                advanceUntilIdle()

                assertEquals(Effect.WriteFailed, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun clearHistoryConfirmed_clearsTheCallLogAndRaisesNothing() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            coEvery { repository.clearHistory() } returns RecentsWriteResult.Completed
            val viewModel = createViewModel()

            viewModel.effects.test {
                viewModel.onAction(Action.ClearHistoryConfirmed)
                advanceUntilIdle()

                expectNoEvents()
            }
            coVerify(exactly = 1) { repository.clearHistory() }
        }
    }

    @Test
    fun clearHistoryConfirmed_whenTheClearFails_emitsWriteFailed() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            coEvery { repository.clearHistory() } returns
                RecentsWriteResult.Failed(cause = RecentsWriteFailure.PermissionRevoked)
            val viewModel = createViewModel()

            viewModel.effects.test {
                viewModel.onAction(Action.ClearHistoryConfirmed)
                advanceUntilIdle()

                assertEquals(Effect.WriteFailed, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    private fun assertEffect(action: Action, expected: Effect) {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            val viewModel = createViewModel()

            viewModel.effects.test {
                viewModel.onAction(action)

                assertEquals(expected, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    private companion object {
        private const val NUMBER = "+15551234567"
        private val ENTRY_ID = CallLogEntryId(value = 7L)
    }
}
