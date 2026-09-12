package com.android.dialer.ui.recents.component

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.ui.core.DialerTheme
import com.android.dialer.ui.recents.common.RECENTS_ASSISTED_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_HD_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_RTT_TEST_TAG
import com.android.dialer.ui.recents.common.previewRecentsItem
import com.android.dialer.ui.recents.common.recentsItemAccountTestTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class RecentsItemMetadataTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun row_withLargeFontAndLongAccountLabel_ellipsizesTheLabelAndKeepsFeatureMarkersVisible() {
        setContent(fontScale = 2f)
        val layouts = mutableListOf<TextLayoutResult>()

        composeTestRule
            .onNodeWithTag(
                testTag = recentsItemAccountTestTag(entryId = ENTRY_ID),
                useUnmergedTree = true,
            )
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }

        assertEquals(1, layouts.single().lineCount)
        assertTrue(layouts.single().isLineEllipsized(0))
        listOf(
            RECENTS_HD_TEST_TAG,
            RECENTS_RTT_TEST_TAG,
            RECENTS_ASSISTED_TEST_TAG,
        ).forEach { tag ->
            composeTestRule.onNodeWithTag(testTag = tag, useUnmergedTree = true).assertIsDisplayed()
        }
    }

    private fun setContent(fontScale: Float) {
        composeTestRule.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                DialerTheme {
                    Box(modifier = Modifier.width(320.dp)) {
                        RecentsItemRow(
                            item = previewRecentsItem(
                                entryId = ENTRY_ID,
                                primaryText = "Test caller",
                            ).copy(
                                accountLabel = LONG_ACCOUNT_LABEL,
                                isHdCall = true,
                                isRttCall = true,
                                isAssistedDialing = true,
                            ),
                            onClick = {},
                            onCallClick = {},
                        )
                    }
                }
            }
        }
    }

    private companion object {
        val ENTRY_ID = CallLogEntryId(value = 7L)
        const val LONG_ACCOUNT_LABEL = "Work subscription with a long name via +12025550186"
    }
}
