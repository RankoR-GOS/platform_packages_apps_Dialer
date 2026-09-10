package com.android.dialer.ui.recents.component

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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

    @Test
    fun secondaryText_withAnAddress_movesTheWholeTimestampToTheSecondLine() {
        assertTimestampWrap("reviewer42@example.invalid", 320.dp, 1f, LayoutDirection.Ltr)
    }

    @Test
    fun secondaryText_withLargeFont_keepsTheTimestampOnOneLine() {
        assertTimestampWrap("Mobile", 340.dp, 2f, LayoutDirection.Ltr)
    }

    @Test
    fun secondaryText_withRtlLayout_keepsTheTimestampOnOneLine() {
        assertTimestampWrap("reviewer42@example.invalid", 320.dp, 1f, LayoutDirection.Rtl)
    }

    private fun assertTimestampWrap(
        descriptor: String,
        width: Dp,
        fontScale: Float,
        direction: LayoutDirection,
    ) {
        val timestamp = "26\u00A0min\u00A0ago"
        val text = "$descriptor •\u00A0$timestamp"
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(LocalDensity.current.density, fontScale),
                LocalLayoutDirection provides direction,
            ) {
                DialerTheme {
                    Box(modifier = Modifier.width(width)) {
                        RecentsItemRow(
                            item = previewRecentsItem(
                                entryId = ENTRY_ID,
                                primaryText = "Migration Parity",
                                secondaryText = text,
                            ),
                            onClick = {},
                        )
                    }
                }
            }
        }

        val layout = secondaryTextLayout()
        val start = text.indexOf(timestamp)
        assertEquals(2, layout.lineCount)
        assertEquals(1, layout.getLineForOffset(start))
        assertEquals(1, layout.getLineForOffset(text.lastIndex))
        assertEquals(1, layout.getLineForOffset(text.indexOf('•')))
        assertTrue(!layout.isLineEllipsized(lineIndex = 1))
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
