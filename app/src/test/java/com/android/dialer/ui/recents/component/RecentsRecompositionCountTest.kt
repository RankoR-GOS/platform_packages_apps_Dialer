package com.android.dialer.ui.recents.component

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.testutil.RobolectricComposeActivityRule
import com.android.dialer.ui.core.DialerTheme
import com.android.dialer.ui.recents.common.previewRecentsItem
import com.android.dialer.ui.recents.model.RecentsItemUiModel
import org.junit.Assert.assertEquals
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
}
