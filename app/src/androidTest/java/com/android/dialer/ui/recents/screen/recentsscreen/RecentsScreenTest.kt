package com.android.dialer.ui.recents.screen.recentsscreen

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.ui.recents.common.RECENTS_EMPTY_ACTION_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_PERMISSION_ACTION_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_ADD_CONTACT_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_CALL_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_COPY_NUMBER_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_DELETE_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_MESSAGE_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_TITLE_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_VIDEO_CALL_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SNACKBAR_TEST_TAG
import com.android.dialer.ui.recents.common.previewRecentsItem
import com.android.dialer.ui.recents.common.recentsItemTestTag
import com.android.dialer.ui.recents.model.RecentsAction as Action
import com.android.dialer.ui.recents.model.RecentsContentUiState
import com.android.dialer.ui.recents.model.RecentsEffect as Effect
import com.android.dialer.ui.recents.model.RecentsItemUiModel
import io.mockk.verify
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class RecentsScreenTest : BaseRecentsScreenTest() {

    @Test
    fun screen_whenResumed_dispatchesScreenResumed() {
        setContent()

        composeTestRule.runOnIdle {
            verify(exactly = 1) { screenModel.onAction(Action.ScreenResumed) }
        }
    }

    @Test
    fun row_click_opensTheSheetForThatRow() {
        setContent(state = entriesState(item(id = 1L), item(id = 2L)))

        composeTestRule.onNodeWithTag(testTag = recentsItemTestTag(ENTRY_TWO)).performClick()

        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_TITLE_TEST_TAG)
            .assertTextEquals("Caller 2")
    }

    @Test
    fun sheet_delete_dispatchesDeleteForEveryCallOfTheGroupAndCloses() {
        val groupIds = persistentListOf(CallLogEntryId(value = 2L), CallLogEntryId(value = 1L))
        setContent(state = entriesState(item(id = 2L, groupedEntryIds = groupIds)))
        openSheet(id = 2L)

        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_DELETE_TEST_TAG)
            .performScrollTo()
            .performClick()

        composeTestRule.runOnIdle {
            verify(exactly = 1) {
                screenModel.onAction(Action.DeleteConfirmed(entryIds = groupIds))
            }
        }
        composeTestRule.onAllNodesWithTag(testTag = RECENTS_SHEET_TITLE_TEST_TAG)
            .assertCountEquals(expectedSize = 0)
    }

    @Test
    fun sheet_rows_dispatchTheMatchingActionWithThatRowsNumber() {
        setContent(state = entriesState(item(id = 1L, canVideoCall = true)))

        listOf(
            RECENTS_SHEET_CALL_TEST_TAG to Action.CallBackClicked(number = NUMBER_ONE),
            RECENTS_SHEET_VIDEO_CALL_TEST_TAG to Action.VideoCallClicked(number = NUMBER_ONE),
            RECENTS_SHEET_MESSAGE_TEST_TAG to Action.MessageClicked(number = NUMBER_ONE),
            RECENTS_SHEET_ADD_CONTACT_TEST_TAG to Action.AddContactClicked(number = NUMBER_ONE),
            RECENTS_SHEET_COPY_NUMBER_TEST_TAG to Action.CopyNumberClicked(number = NUMBER_ONE),
        ).forEach { (tag, expected) ->
            openSheet(id = 1L)

            composeTestRule.onNodeWithTag(testTag = tag).performScrollTo().performClick()

            composeTestRule.runOnIdle {
                verify(exactly = 1) { screenModel.onAction(expected) }
            }
        }
    }

    @Test
    fun sheet_rows_meetTheMinimumRowHeight() {
        setContent(state = entriesState(item(id = 1L)))
        openSheet(id = 1L)

        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_CALL_TEST_TAG)
            .assertHeightIsAtLeast(expectedMinHeight = SHEET_ROW_MIN_HEIGHT)
    }

    @Test
    fun sheet_delete_dispatchesAtTheTapEvenWhenItsRowLeavesTheSnapshotDuringTheHide() {
        setContent(state = entriesState(item(id = 1L), item(id = 2L)))
        openSheet(id = 2L)
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_DELETE_TEST_TAG)
            .performScrollTo()
            .performClick()
        composeTestRule.mainClock.advanceTimeByFrame()
        uiState.value = entriesState(item(id = 1L))
        composeTestRule.mainClock.autoAdvance = true

        composeTestRule.runOnIdle {
            verify(exactly = 1) {
                screenModel.onAction(
                    Action.DeleteConfirmed(entryIds = persistentListOf(CallLogEntryId(value = 2L))),
                )
            }
        }
        composeTestRule.onAllNodesWithTag(testTag = RECENTS_SHEET_TITLE_TEST_TAG)
            .assertCountEquals(expectedSize = 0)
    }

    @Test
    fun writeFailedEffect_showsTheSnackbarWithTheResolvedMessage() {
        setContent(state = entriesState(item(id = 1L)))

        effects.trySend(Effect.WriteFailed)

        composeTestRule.onNodeWithTag(testTag = RECENTS_SNACKBAR_TEST_TAG, useUnmergedTree = true)
            .assert(hasAnyDescendant(hasTextExactly(WRITE_FAILED_MESSAGE)))
    }

    private fun openSheet(id: Long) {
        composeTestRule.onNodeWithTag(testTag = recentsItemTestTag(CallLogEntryId(value = id)))
            .performClick()
        composeTestRule.waitForIdle()
    }

    private fun item(
        id: Long,
        canVideoCall: Boolean = false,
        groupedEntryIds: ImmutableList<CallLogEntryId> = persistentListOf(CallLogEntryId(id)),
    ): RecentsItemUiModel {
        return previewRecentsItem(
            entryId = CallLogEntryId(value = id),
            primaryText = "Caller $id",
            number = "+1555000$id",
            canVideoCall = canVideoCall,
            groupedEntryIds = groupedEntryIds,
        )
    }

    private companion object {
        val SHEET_ROW_MIN_HEIGHT = 56.dp
        val ENTRY_TWO = CallLogEntryId(value = 2L)
        const val NUMBER_ONE = "+15550001"
    }
}
