package com.android.dialer.ui.recents.component

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.ui.core.DialerTheme
import com.android.dialer.ui.recents.common.previewRecentsItem
import com.android.dialer.ui.recents.common.recentsItemTestTag
import com.android.dialer.ui.recents.model.RecentsListItemUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class RecentsItemsAnimationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val items = mutableStateOf<ImmutableList<RecentsListItemUiModel>>(persistentListOf())

    @Test
    fun row_whenARowIsInsertedAboveIt_slidesDownOverSeveralFrames() {
        composeTestRule.mainClock.autoAdvance = false
        setContent(entries(ANCHOR_ID))
        val restingTop = rowTop(id = ANCHOR_ID)

        composeTestRule.runOnUiThread { items.value = entries(NEW_ID, ANCHOR_ID) }
        composeTestRule.mainClock.advanceTimeByFrame()
        composeTestRule.mainClock.advanceTimeByFrame()
        val midwayTop = rowTop(id = ANCHOR_ID)

        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()
        val settledTop = rowTop(id = ANCHOR_ID)

        assertTrue(settledTop > restingTop)
        assertTrue(midwayTop < settledTop)
    }

    @Test
    fun row_whenARowAboveItIsRemoved_slidesUpOverSeveralFrames() {
        composeTestRule.mainClock.autoAdvance = false
        setContent(entries(NEW_ID, ANCHOR_ID))
        val restingTop = rowTop(id = ANCHOR_ID)

        composeTestRule.runOnUiThread { items.value = entries(ANCHOR_ID) }
        composeTestRule.mainClock.advanceTimeByFrame()
        composeTestRule.mainClock.advanceTimeByFrame()
        val midwayTop = rowTop(id = ANCHOR_ID)

        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()
        val settledTop = rowTop(id = ANCHOR_ID)

        assertTrue(settledTop < restingTop)
        assertTrue(midwayTop > settledTop)
    }

    @Test
    fun row_whenTheListIsUnchanged_staysWhereItSettled() {
        setContent(entries(NEW_ID, ANCHOR_ID))
        val restingTop = rowTop(id = ANCHOR_ID)

        composeTestRule.runOnUiThread { items.value = entries(NEW_ID, ANCHOR_ID) }
        composeTestRule.waitForIdle()

        assertEquals(restingTop, rowTop(id = ANCHOR_ID), 0f)
    }

    private fun setContent(initial: ImmutableList<RecentsListItemUiModel>) {
        items.value = initial
        composeTestRule.setContent {
            DialerTheme {
                RecentsItems(
                    items = items.value,
                    listState = rememberLazyListState(),
                    onItemEvent = {},
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun rowTop(id: Long): Float {
        return composeTestRule.onNodeWithTag(testTag = recentsItemTestTag(CallLogEntryId(id)))
            .fetchSemanticsNode()
            .positionInRoot
            .y
    }

    private fun entries(vararg ids: Long): ImmutableList<RecentsListItemUiModel> {
        return ids.map { id ->
            RecentsListItemUiModel.Entry(
                item = previewRecentsItem(
                    entryId = CallLogEntryId(value = id),
                    primaryText = "Caller $id",
                ),
            )
        }.toImmutableList()
    }

    private companion object {
        const val ANCHOR_ID = 1L
        const val NEW_ID = 2L
    }
}
