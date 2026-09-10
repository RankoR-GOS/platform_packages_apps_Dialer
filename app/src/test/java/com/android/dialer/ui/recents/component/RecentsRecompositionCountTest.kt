package com.android.dialer.ui.recents.component

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composer
import androidx.compose.runtime.CompositionTracer
import androidx.compose.runtime.InternalComposeTracingApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.testutil.RobolectricComposeActivityRule
import com.android.dialer.ui.core.DialerTheme
import com.android.dialer.ui.recents.common.previewRecentsItem
import com.android.dialer.ui.recents.model.RecentsItemUiModel
import com.android.dialer.ui.recents.model.RecentsListItemUiModel
import kotlinx.collections.immutable.ImmutableList
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
internal class RecentsRecompositionCountTest {

    @get:Rule(order = 0)
    val componentActivityRule = RobolectricComposeActivityRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Test
    fun row_whenAnotherRowsModelChanges_doesNotRecompose() {
        val rowA = mutableStateOf(item(entryId = 1L, primaryText = "Caller 1"))
        val rowB = mutableStateOf(item(entryId = 2L, primaryText = "Caller 2"))
        val countA = CompositionCounter()
        val countB = CompositionCounter()

        composeTestRule.setContent {
            DialerTheme {
                CountedRow(item = rowA.value, counter = countA)
                CountedRow(item = rowB.value, counter = countB)
            }
        }
        composeTestRule.waitForIdle()
        val initialA = countA.value
        val initialB = countB.value

        composeTestRule.runOnIdle {
            rowA.value = item(entryId = 1L, primaryText = "Caller 1 renamed")
        }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            assertEquals(initialA + 1, countA.value)
            assertEquals(initialB, countB.value)
        }
    }

    @Test
    fun row_whenItsModelIsReassignedToAnEqualValue_doesNotRecompose() {
        val model = mutableStateOf(item(entryId = 1L, primaryText = "Caller 1"))
        val counter = CompositionCounter()

        composeTestRule.setContent {
            DialerTheme { CountedRow(item = model.value, counter = counter) }
        }
        composeTestRule.waitForIdle()
        val initial = counter.value

        composeTestRule.runOnIdle {
            model.value = item(entryId = 1L, primaryText = "Caller 1")
        }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            assertEquals(initial, counter.value)
        }
    }

    @Test
    fun row_whenOnlyItsSecondaryTextChanges_recomposesExactlyOnce() {
        val ticking = mutableStateOf(item(entryId = 1L, secondaryText = "1 min ago"))
        val settled = mutableStateOf(item(entryId = 2L, secondaryText = "Yesterday"))
        val tickingCount = CompositionCounter()
        val settledCount = CompositionCounter()

        composeTestRule.setContent {
            DialerTheme {
                CountedRow(item = ticking.value, counter = tickingCount)
                CountedRow(item = settled.value, counter = settledCount)
            }
        }
        composeTestRule.waitForIdle()
        val initialTicking = tickingCount.value
        val initialSettled = settledCount.value

        composeTestRule.runOnIdle {
            ticking.value = item(entryId = 1L, secondaryText = "2 min ago")
        }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            assertEquals(initialTicking + 1, tickingCount.value)
            assertEquals(initialSettled, settledCount.value)
        }
    }

    @Test
    fun listContainer_whenScrolled_doesNotRecompose() {
        val counter = CompositionCounter()
        lateinit var listState: LazyListState

        composeTestRule.setContent {
            listState = rememberLazyListState()
            DialerTheme {
                Box(modifier = Modifier.height(height = VIEWPORT_HEIGHT)) {
                    CountedList(
                        items = listItems(count = LIST_SIZE),
                        listState = listState,
                        counter = counter
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        val afterInitialComposition = counter.value

        composeTestRule.runOnIdle {
            listState.requestScrollToItem(index = 0, scrollOffset = SCROLL_OFFSET_PX)
        }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            assertEquals(SCROLL_OFFSET_PX, listState.firstVisibleItemScrollOffset)
            assertEquals(afterInitialComposition, counter.value)
        }
    }

    @Test
    fun listContainer_whenScrolledAcrossManyItems_doesNotRecompose() {
        val counter = CompositionCounter()
        lateinit var listState: LazyListState

        composeTestRule.setContent {
            listState = rememberLazyListState()
            DialerTheme {
                Box(modifier = Modifier.height(height = VIEWPORT_HEIGHT)) {
                    CountedList(
                        items = listItems(count = LIST_SIZE),
                        listState = listState,
                        counter = counter
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        val afterInitialComposition = counter.value

        composeTestRule.runOnIdle {
            listState.requestScrollToItem(index = SCROLL_TARGET_INDEX)
        }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            assertEquals(SCROLL_TARGET_INDEX, listState.firstVisibleItemIndex)
            assertEquals(afterInitialComposition, counter.value)
        }
    }

    @OptIn(InternalComposeTracingApi::class)
    @Test
    fun listRows_whenScrolledWithinTheSameVisibleRows_doNotRecompose() {
        val items = listItems(count = 2)
        val rowCounter = RowCompositionCounter()
        lateinit var listState: LazyListState

        Composer.setTracer(rowCounter)
        try {
            composeTestRule.setContent {
                listState = rememberLazyListState()
                DialerTheme {
                    Box(modifier = Modifier.height(height = ROW_VIEWPORT_HEIGHT)) {
                        RecentsItems(items = items, listState = listState, onItemEvent = {})
                    }
                }
            }
            val afterInitialComposition = composeTestRule.runOnIdle {
                assertEquals(listOf(1L, 2L), listState.layoutInfo.visibleItemsInfo.map { it.key })
                assertTrue("expected production row compositions", rowCounter.value > 0)
                rowCounter.value
            }

            composeTestRule.runOnIdle {
                listState.requestScrollToItem(index = 0, scrollOffset = SCROLL_OFFSET_PX)
            }
            composeTestRule.runOnIdle {
                assertEquals(SCROLL_OFFSET_PX, listState.firstVisibleItemScrollOffset)
                assertEquals(listOf(1L, 2L), listState.layoutInfo.visibleItemsInfo.map { it.key })
                assertEquals(afterInitialComposition, rowCounter.value)
            }
        } finally {
            Composer.setTracer(null)
        }
    }

    @OptIn(InternalComposeTracingApi::class)
    private class RowCompositionCounter : CompositionTracer {
        var value: Int = 0
            private set

        override fun traceEventStart(key: Int, dirty1: Int, dirty2: Int, info: String) {
            if (info.startsWith("com.android.dialer.ui.recents.component.RecentsItemRow (")) {
                value += 1
            }
        }

        override fun traceEventEnd() = Unit

        override fun isTraceInProgress(): Boolean = true
    }

    private class CompositionCounter {
        var value: Int = 0
            private set

        fun increment() {
            value += 1
        }
    }

    private data class CountingElement(
        val counter: CompositionCounter,
        val token: Any = Any(),
    ) : ModifierNodeElement<CountingNode>() {

        override fun create(): CountingNode {
            return CountingNode(counter = counter)
        }

        override fun update(node: CountingNode) {
            node.counter.increment()
        }
    }

    private class CountingNode(
        val counter: CompositionCounter,
    ) : Modifier.Node() {

        override val shouldAutoInvalidate: Boolean = false

        override fun onAttach() {
            counter.increment()
        }
    }

    private fun Modifier.countCompositions(counter: CompositionCounter): Modifier {
        return this then CountingElement(counter = counter)
    }

    @Composable
    private fun CountedRow(item: RecentsItemUiModel, counter: CompositionCounter) {
        RecentsItemRow(
            item = item,
            onClick = {},
            modifier = Modifier.countCompositions(counter = counter),
        )
    }

    private fun item(
        entryId: Long,
        primaryText: String = "Caller $entryId",
        secondaryText: String = "Kingston, Jamaica • 10:24",
    ): RecentsItemUiModel {
        return previewRecentsItem(
            entryId = CallLogEntryId(value = entryId),
            primaryText = primaryText,
            secondaryText = secondaryText,
        )
    }

    @Composable
    private fun CountedList(
        items: ImmutableList<RecentsListItemUiModel>,
        listState: LazyListState,
        counter: CompositionCounter,
    ) {
        RecentsItems(
            items = items,
            listState = listState,
            onItemEvent = {},
            modifier = Modifier.countCompositions(counter = counter),
        )
    }

    private fun listItems(count: Int): ImmutableList<RecentsListItemUiModel> {
        return (1..count)
            .map { index -> RecentsListItemUiModel.Entry(item = item(entryId = index.toLong())) }
            .toImmutableList()
    }

    private companion object {
        private val VIEWPORT_HEIGHT = 96.dp
        private val ROW_VIEWPORT_HEIGHT = 160.dp
        private const val LIST_SIZE = 60
        private const val SCROLL_OFFSET_PX = 24
        private const val SCROLL_TARGET_INDEX = 20
    }
}
