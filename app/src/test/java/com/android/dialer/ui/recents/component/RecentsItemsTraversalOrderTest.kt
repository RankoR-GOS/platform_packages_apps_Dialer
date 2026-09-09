package com.android.dialer.ui.recents.component

import android.os.Build
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.testutil.RobolectricComposeActivityRule
import com.android.dialer.ui.core.DialerTheme
import com.android.dialer.ui.recents.common.RECENTS_DAY_HEADER_TEST_TAG
import com.android.dialer.ui.recents.common.previewRecentsItem
import com.android.dialer.ui.recents.common.recentsItemTestTag
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
internal class RecentsItemsTraversalOrderTest {

    @get:Rule(order = 0)
    val componentActivityRule = RobolectricComposeActivityRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Test
    fun rows_areTraversedInListOrderForAFlatList() {
        val items = listOf(entry(id = 3L), entry(id = 1L), entry(id = 2L))

        setContent(items = items)

        assertEquals(tagsOf(items), traversedTags(items))
    }

    @Test
    fun rows_underADayHeader_areTraversedAfterThatHeader() {
        val items = listOf(
            header(key = "Today"),
            entry(id = 2L),
            header(key = "Older"),
            entry(id = 1L),
        )

        setContent(items = items)

        assertEquals(tagsOf(items), traversedTags(items))
    }

    @Test
    fun dayHeader_isAnnouncedAsAHeading() {
        setContent(items = listOf(header(key = "Today"), entry(id = 1L)))

        composeTestRule.onAllNodesWithTag(testTag = RECENTS_DAY_HEADER_TEST_TAG)
            .onFirst()
            .assert(matcher = isHeading())
    }

    private fun setContent(items: List<RecentsListItemUiModel>) {
        composeTestRule.setContent {
            DialerTheme {
                RecentsItems(
                    items = items.toImmutableList(),
                    listState = rememberLazyListState(),
                    onItemEvent = {},
                )
            }
        }
    }

    private fun traversedTags(items: List<RecentsListItemUiModel>): List<String> {
        val expectedTags = tagsOf(items).toSet()
        val isListItem = SemanticsMatcher(description = "is a recents list item") { node ->
            node.config.getOrNull(SemanticsProperties.TestTag) in expectedTags
        }

        return composeTestRule.onAllNodes(matcher = isListItem)
            .fetchSemanticsNodes()
            .map { node -> node.config[SemanticsProperties.TestTag] }
    }

    private fun tagsOf(items: List<RecentsListItemUiModel>): List<String> {
        return items.map { item ->
            when (item) {
                is RecentsListItemUiModel.DayHeader -> RECENTS_DAY_HEADER_TEST_TAG
                is RecentsListItemUiModel.Entry -> recentsItemTestTag(entryId = item.item.entryId)
            }
        }
    }

    private fun header(key: String): RecentsListItemUiModel.DayHeader {
        return RecentsListItemUiModel.DayHeader(key = key, label = key)
    }

    private fun entry(id: Long): RecentsListItemUiModel.Entry {
        return RecentsListItemUiModel.Entry(
            item = previewRecentsItem(
                entryId = CallLogEntryId(value = id),
                primaryText = "Caller $id",
            ),
        )
    }
}
