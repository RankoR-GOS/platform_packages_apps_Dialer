package com.android.dialer.ui.recents.component

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.ui.core.DialerTheme
import com.android.dialer.ui.recents.common.RECENTS_SHEET_CALL_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_TITLE_TEST_TAG
import com.android.dialer.ui.recents.common.previewActionLabels
import com.android.dialer.ui.recents.common.previewRecentsItem
import com.android.dialer.ui.recents.model.RecentsItemUiModel
import com.android.dialer.ui.recents.model.RecentsSheetAction
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class RecentsActionsSheetHideTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val target = mutableStateOf<RecentsItemUiModel?>(null)
    private val emittedActions = mutableListOf<RecentsSheetAction>()

    @Test
    fun sheet_whenAnActionIsTapped_deliversItAtTheTap() {
        setSheet()
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_CALL_TEST_TAG).performClick()
        composeTestRule.mainClock.advanceTimeByFrame()

        assertEquals(listOf<RecentsSheetAction>(RecentsSheetAction.Call), emittedActions)
    }

    @Test
    fun sheet_whenItsTargetIsCleared_staysComposedWhileItHides() {
        setSheet()
        composeTestRule.mainClock.autoAdvance = false

        target.value = null
        composeTestRule.mainClock.advanceTimeByFrame()
        composeTestRule.mainClock.advanceTimeByFrame()

        composeTestRule
            .onNodeWithTag(testTag = RECENTS_SHEET_TITLE_TEST_TAG, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun sheet_whenItsTargetIsCleared_leavesOnceTheHideCompletes() {
        setSheet()

        target.value = null

        composeTestRule
            .onAllNodesWithTag(testTag = RECENTS_SHEET_TITLE_TEST_TAG, useUnmergedTree = true)
            .assertCountEquals(expectedSize = 0)
    }

    private fun setSheet() {
        target.value = previewRecentsItem(
            entryId = CallLogEntryId(value = 7L),
            primaryText = "Caller 7",
        )
        composeTestRule.setContent {
            DialerTheme {
                RecentsActionsSheet(
                    target = target.value,
                    labels = previewActionLabels(),
                    onAction = { action -> emittedActions.add(action) },
                    onDismissRequest = {},
                )
            }
        }
        composeTestRule.waitForIdle()
    }
}
