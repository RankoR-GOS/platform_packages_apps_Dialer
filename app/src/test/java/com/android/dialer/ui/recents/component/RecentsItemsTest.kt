package com.android.dialer.ui.recents.component

import android.os.Build
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.testutil.RobolectricComposeActivityRule
import com.android.dialer.ui.core.DialerTheme
import com.android.dialer.ui.recents.common.RECENTS_DAY_HEADER_TEST_TAG
import com.android.dialer.ui.recents.common.previewRecentsItem
import com.android.dialer.ui.recents.common.recentsItemCallButtonTestTag
import com.android.dialer.ui.recents.common.recentsItemTestTag
import com.android.dialer.ui.recents.model.RecentsItemEvent as Event
import com.android.dialer.ui.recents.model.RecentsListItemUiModel
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsItemsTest {

    @get:Rule(order = 0)
    val componentActivityRule = RobolectricComposeActivityRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private val emittedEvents = mutableListOf<Event>()

    @Test
    fun row_click_emitsClickedForThatEntry() {
        setContent(entries = listOf(entry(id = 1L), entry(id = 2L)))

        composeTestRule.onNodeWithTag(testTag = rowTag(id = 2L)).performClick()

        composeTestRule.runOnIdle {
            assertEquals(listOf(Event.Clicked(entryId = entryId(2L))), emittedEvents)
        }
    }

    @Test
    fun callButton_click_emitsCallClickedWithTheNumber() {
        setContent(entries = listOf(entry(id = 1L)))

        composeTestRule.onNodeWithTag(testTag = callButtonTag(id = 1L)).performClick()

        composeTestRule.runOnIdle {
            assertEquals(
                listOf(Event.CallClicked(entryId = entryId(1L), number = NUMBER)),
                emittedEvents
            )
        }
    }

    @Test
    fun callButton_withPostDialDigits_emitsTheWholeVoiceCallbackNumber() {
        val row = entry(id = 1L)
        setContent(entries = listOf(row.copy(item = row.item.copy(postDialDigits = ",12;34"))))

        composeTestRule.onNodeWithTag(testTag = callButtonTag(id = 1L)).performClick()

        composeTestRule.runOnIdle {
            assertEquals(
                listOf(Event.CallClicked(entryId = entryId(1L), number = NUMBER + ",12;34")),
                emittedEvents,
            )
        }
    }

    @Test
    fun callButton_whenTheCallWasVideo_emitsVideoCallClickedWithTheNumber() {
        setContent(entries = listOf(entry(id = 1L, canVideoCall = true)))

        composeTestRule.onNodeWithTag(testTag = callButtonTag(id = 1L)).performClick()

        composeTestRule.runOnIdle {
            assertEquals(
                listOf(Event.VideoCallClicked(entryId = entryId(1L), number = NUMBER)),
                emittedEvents
            )
        }
    }

    @Test
    fun callButton_withAnOptionalVideoActionOnAVoiceEntry_emitsAVoiceCallback() {
        val row = entry(id = 1L, canVideoCall = true)
        setContent(entries = listOf(row.copy(item = row.item.copy(isVideoCall = false))))

        composeTestRule.onNodeWithTag(testTag = callButtonTag(id = 1L)).performClick()

        composeTestRule.runOnIdle {
            assertEquals(
                listOf(Event.CallClicked(entryId = entryId(1L), number = NUMBER)),
                emittedEvents
            )
        }
    }

    @Test
    fun callButton_whenTheEntryCannotBeCalled_isNotRendered() {
        setContent(entries = listOf(entry(id = 1L, canCallBack = false)))

        composeTestRule.onAllNodesWithTag(testTag = callButtonTag(id = 1L))
            .assertCountEquals(expectedSize = 0)
    }

    @Test
    fun row_whenRendered_startsEightDpFromTheListEdge() {
        setContent(entries = listOf(entry(id = 1L)))

        composeTestRule.onNodeWithTag(testTag = rowTag(id = 1L))
            .assertLeftPositionInRootIsEqualTo(expectedLeft = LIST_PADDING)
    }

    private fun setContent(
        entries: List<RecentsListItemUiModel.Entry>,
        withHeader: Boolean = false,
    ) {
        val items = buildList {
            if (withHeader) {
                add(RecentsListItemUiModel.DayHeader(key = "Today", label = "Today"))
            }
            addAll(entries)
        }

        composeTestRule.setContent {
            DialerTheme {
                RecentsItems(
                    items = items.toImmutableList(),
                    listState = rememberLazyListState(),
                    onItemEvent = { event -> emittedEvents.add(event) },
                )
            }
        }
    }

    private fun entry(
        id: Long,
        canCallBack: Boolean = true,
        canVideoCall: Boolean = false,
    ): RecentsListItemUiModel.Entry {
        return RecentsListItemUiModel.Entry(
            item = previewRecentsItem(
                entryId = entryId(id),
                primaryText = "Caller $id",
                number = NUMBER,
                canCallBack = canCallBack,
                canVideoCall = canVideoCall,
            ),
        )
    }

    private fun entryId(value: Long): CallLogEntryId {
        return CallLogEntryId(value = value)
    }

    private fun rowTag(id: Long): String {
        return recentsItemTestTag(entryId = entryId(id))
    }

    private fun callButtonTag(id: Long): String {
        return recentsItemCallButtonTestTag(entryId = entryId(id))
    }

    private companion object {
        private const val NUMBER = "+15550001"
        private val LIST_PADDING = 8.dp
    }
}
