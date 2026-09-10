package com.android.dialer.ui.recents.component

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.ui.core.DialerTheme
import com.android.dialer.ui.recents.common.previewRecentsItem
import com.android.dialer.ui.recents.common.recentsItemSecondaryTextTestTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class RecentsItemRowWrapTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun secondaryText_whenItDoesNotFitOneLine_wrapsToASecondLineThenEllipsizes() {
        setContent(secondaryText = LONG_SECONDARY_TEXT)

        val layout = secondaryTextLayout()

        assertEquals(2, layout.lineCount)
        assertTrue(layout.isLineEllipsized(lineIndex = 1))
    }

    @Test
    fun secondaryText_whenItFitsOneLine_staysOnOneLine() {
        setContent(secondaryText = SHORT_SECONDARY_TEXT)

        assertEquals(1, secondaryTextLayout().lineCount)
    }

    private fun setContent(secondaryText: String) {
        composeTestRule.setContent {
            DialerTheme {
                RecentsItemRow(
                    item = previewRecentsItem(
                        entryId = ENTRY_ID,
                        primaryText = "Caller 7",
                        secondaryText = secondaryText,
                    ),
                    onClick = {},
                )
            }
        }
    }

    private fun secondaryTextLayout(): TextLayoutResult {
        val layouts = mutableListOf<TextLayoutResult>()

        composeTestRule.onNodeWithTag(testTag = SECONDARY_TEXT_TAG, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { fetch ->
                fetch(layouts)
            }

        return layouts.single()
    }

    private companion object {
        private val ENTRY_ID = CallLogEntryId(value = 7L)
        private val SECONDARY_TEXT_TAG = recentsItemSecondaryTextTestTag(entryId = ENTRY_ID)
        private const val SHORT_SECONDARY_TEXT = "Kingston, Jamaica • 10:24"
        private val LONG_SECONDARY_TEXT = List(size = 12) { "Kingston, Jamaica" }
            .joinToString(separator = " • ")
    }
}
