package com.android.dialer.ui.recents.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.ui.core.DialerTheme
import com.android.dialer.ui.recents.common.RECENTS_LIST_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_TITLE_TEST_TAG
import com.android.dialer.ui.recents.common.previewActionLabels
import com.android.dialer.ui.recents.common.previewRecentsItem
import com.android.dialer.ui.recents.common.recentsItemTestTag
import com.android.dialer.ui.recents.model.RecentsContentUiState
import com.android.dialer.ui.recents.model.RecentsEffect
import com.android.dialer.ui.recents.model.RecentsListItemUiModel
import com.android.dialer.ui.recents.model.RecentsUiState
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class RecentsStateRestorationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val restorationTester = StateRestorationTester(composeTestRule)
    private val uiState = MutableStateFlow(entriesState(count = ROW_COUNT))
    private val screenModel = mockk<RecentsScreenModel>()

    @Before
    fun stubScreenModel() {
        every { screenModel.uiState } returns uiState
        every { screenModel.effects } returns emptyFlow<RecentsEffect>()
        every { screenModel.onAction(any()) } just runs
    }

    @Test
    fun scrollPosition_afterStateRestore_isRestored() {
        setContent()
        composeTestRule.onNodeWithTag(testTag = RECENTS_LIST_TEST_TAG)
            .performScrollToKey(key = ANCHOR_ID)

        restorationTester.emulateSavedInstanceStateRestore()

        composeTestRule.onNodeWithTag(testTag = recentsItemTestTag(CallLogEntryId(ANCHOR_ID)))
            .assertExists()
        composeTestRule.onAllNodesWithTag(testTag = recentsItemTestTag(CallLogEntryId(1L)))
            .assertCountEquals(expectedSize = 0)
    }

    @Test
    fun sheetTarget_afterStateRestore_isRestoredById() {
        setContent()
        openSheet(id = 3L)

        restorationTester.emulateSavedInstanceStateRestore()

        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_TITLE_TEST_TAG)
            .assertTextEquals("Caller 3")
    }

    @Test
    fun sheetTarget_whenTheSavedEntryIsGoneAfterRestore_isCleared() {
        setContent()
        openSheet(id = 3L)

        uiState.value = entriesState(count = 2)
        restorationTester.emulateSavedInstanceStateRestore()

        composeTestRule.onAllNodesWithTag(testTag = RECENTS_SHEET_TITLE_TEST_TAG)
            .assertCountEquals(expectedSize = 0)
        assertEquals(2, (uiState.value.content as RecentsContentUiState.Entries).items.size)
    }

    private fun setContent() {
        restorationTester.setContent {
            DialerTheme {
                RecentsScreen(
                    screenModel = screenModel,
                    effectHandler = mockk(relaxed = true),
                    onRequestPermission = {},
                )
            }
        }
    }

    private fun openSheet(id: Long) {
        composeTestRule.onNodeWithTag(testTag = recentsItemTestTag(CallLogEntryId(value = id)))
            .performClick()
        composeTestRule.waitForIdle()
    }

    private fun entriesState(count: Int): RecentsUiState {
        return RecentsUiState(
            content = RecentsContentUiState.Entries(
                items = (1..count).map { id ->
                    RecentsListItemUiModel.Entry(
                        item = previewRecentsItem(
                            entryId = CallLogEntryId(value = id.toLong()),
                            primaryText = "Caller $id",
                        ),
                    )
                }.toImmutableList(),
            ),
            actionLabels = previewActionLabels(),
        )
    }

    private companion object {
        const val ROW_COUNT = 60
        const val ANCHOR_ID = 40L
    }
}
