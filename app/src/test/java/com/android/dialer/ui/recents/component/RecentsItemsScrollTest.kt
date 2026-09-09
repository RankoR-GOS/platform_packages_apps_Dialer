package com.android.dialer.ui.recents.component

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.testutil.RobolectricComposeActivityRule
import com.android.dialer.ui.core.DialerTheme
import com.android.dialer.ui.recents.common.RECENTS_LIST_TEST_TAG
import com.android.dialer.ui.recents.common.previewRecentsItem
import com.android.dialer.ui.recents.common.recentsItemTestTag
import com.android.dialer.ui.recents.model.RecentsListItemUiModel
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsItemsScrollTest {

    @get:Rule(order = 0)
    val componentActivityRule = RobolectricComposeActivityRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var listState: LazyListState

    @Test
    fun list_whenRendered_composesOnlyTheVisibleWindow() {
        setContent(items = entries(count = ENTRY_COUNT))

        val visibleCount = composeTestRule.runOnIdle {
            listState.layoutInfo.visibleItemsInfo.size
        }

        assertTrue("expected a viewport, not the whole list", visibleCount < ENTRY_COUNT)
        composeTestRule.onNodeWithTag(testTag = RECENTS_LIST_TEST_TAG)
            .onChildren()
            .assertCountEquals(expectedSize = visibleCount)
    }

    @Test
    fun list_whenRendered_givesEveryVisibleRowTheSameContentType() {
        setContent(items = entries(count = ENTRY_COUNT))

        val contentTypes = composeTestRule.runOnIdle {
            listState.layoutInfo.visibleItemsInfo.map { info -> info.contentType }
        }

        assertTrue("expected rows on screen", contentTypes.isNotEmpty())
        assertEquals(1, contentTypes.distinct().size)
        assertTrue("contentType is not set", contentTypes.all { type -> type != null })
    }

    @Test
    fun list_withADayHeader_givesTheHeaderItsOwnContentType() {
        setContent(items = listOf(header()) + entries(count = VISIBLE_ONLY_COUNT))

        val contentTypes = composeTestRule.runOnIdle {
            listState.layoutInfo.visibleItemsInfo.map { info -> info.contentType }
        }

        assertEquals(2, contentTypes.distinct().size)
        assertEquals(1, contentTypes.count { type -> type == contentTypes.first() })
    }

    @Test
    fun list_whenScrolledToAnIndex_displaysThatRow() {
        setContent(items = entries(count = ENTRY_COUNT))

        composeTestRule.onNodeWithTag(testTag = RECENTS_LIST_TEST_TAG)
            .performScrollToIndex(index = LAST_INDEX)

        composeTestRule.onNodeWithTag(testTag = tagOf(id = ENTRY_COUNT.toLong()))
            .assertIsDisplayed()
    }

    @Test
    fun list_whenScrolledToAKey_displaysThatRow() {
        setContent(items = entries(count = ENTRY_COUNT))

        composeTestRule.onNodeWithTag(testTag = RECENTS_LIST_TEST_TAG)
            .performScrollToKey(key = TARGET_ENTRY_ID)

        composeTestRule.onNodeWithTag(testTag = tagOf(id = TARGET_ENTRY_ID)).assertIsDisplayed()
    }

    @Test
    fun list_whenScrolledToANode_displaysTheMatchedRow() {
        setContent(items = entries(count = ENTRY_COUNT))

        composeTestRule.onNodeWithTag(testTag = RECENTS_LIST_TEST_TAG)
            .performScrollToNode(matcher = hasTestTag(testTag = tagOf(id = TARGET_ENTRY_ID)))

        composeTestRule.onNodeWithTag(testTag = tagOf(id = TARGET_ENTRY_ID)).assertIsDisplayed()
    }

    @Test
    fun rows_whenRendered_areAllClickable() {
        setContent(items = entries(count = VISIBLE_ONLY_COUNT))

        composeTestRule.onAllNodesWithTag(testTag = tagOf(id = 1L))
            .assertCountEquals(expectedSize = 1)
        composeTestRule.onNodeWithTag(testTag = RECENTS_LIST_TEST_TAG)
            .onChildren()
            .assertAll(matcher = hasAnyDescendant(matcher = hasClickAction()))
    }

    @Test
    fun list_whenTheDataChangesUnderTheSameKeys_keepsTheFirstVisibleKey() {
        var items by mutableStateOf(value = entries(count = ENTRY_COUNT))
        setContent(items = { items })
        composeTestRule.onNodeWithTag(testTag = RECENTS_LIST_TEST_TAG)
            .performScrollToIndex(index = SCROLL_TARGET_INDEX)
        val anchorKey = composeTestRule.runOnIdle {
            listState.layoutInfo.visibleItemsInfo.first().key
        }

        composeTestRule.runOnIdle {
            items = items.map { entry ->
                entry.copy(item = entry.item.copy(secondaryText = "11:59"))
            }
        }

        assertEquals(
            anchorKey,
            composeTestRule.runOnIdle { listState.layoutInfo.visibleItemsInfo.first().key },
        )
    }

    private fun setContent(items: List<RecentsListItemUiModel>) {
        setContent(items = { items })
    }

    private fun setContent(items: () -> List<RecentsListItemUiModel>) {
        composeTestRule.setContent {
            listState = rememberLazyListState()
            DialerTheme {
                Box(modifier = Modifier.height(height = VIEWPORT_HEIGHT)) {
                    RecentsItems(
                        items = items().toImmutableList(),
                        listState = listState,
                        onItemEvent = {},
                    )
                }
            }
        }
    }

    private fun header(): RecentsListItemUiModel.DayHeader {
        return RecentsListItemUiModel.DayHeader(key = "Today", label = "Today")
    }

    private fun entries(count: Int): List<RecentsListItemUiModel.Entry> {
        return (1..count).map { id ->
            RecentsListItemUiModel.Entry(
                item = previewRecentsItem(
                    entryId = CallLogEntryId(value = id.toLong()),
                    primaryText = "Caller $id",
                ),
            )
        }
    }

    private fun tagOf(id: Long): String {
        return recentsItemTestTag(entryId = CallLogEntryId(value = id))
    }

    private companion object {
        private const val ENTRY_COUNT = 60
        private const val LAST_INDEX = ENTRY_COUNT - 1
        private const val SCROLL_TARGET_INDEX = 30
        private const val VISIBLE_ONLY_COUNT = 3
        private const val TARGET_ENTRY_ID = 42L
        private val VIEWPORT_HEIGHT = 400.dp
    }
}
