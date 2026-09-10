package com.android.dialer.ui.recents.component

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsEqualTo
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.ui.core.DialerTheme
import com.android.dialer.ui.recents.common.RECENTS_CALL_TYPE_ICON_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_GROUP_COUNT_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_ACTION_ICON_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_AVATAR_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_TITLE_TEST_TAG
import com.android.dialer.ui.recents.common.previewActionLabels
import com.android.dialer.ui.recents.common.previewRecentsItem
import com.android.dialer.ui.recents.common.recentsItemAccountTestTag
import com.android.dialer.ui.recents.common.recentsItemPrimaryTextTestTag
import com.android.dialer.ui.recents.common.recentsItemSecondaryTextTestTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class RecentsAlignmentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun row_withWrappedText_alignsTheMarkerAndCountToItsFirstLine() {
        assertRowAlignment(fontScale = 1f, direction = LayoutDirection.Ltr)
    }

    @Test
    fun row_withLargeWrappedText_keepsTheSameTextAndMarkerAnchors() {
        assertRowAlignment(fontScale = 2f, direction = LayoutDirection.Ltr)
    }

    @Test
    fun row_withRtlContent_mirrorsTheTextAnchorsAndKeepsTheFirstBaseline() {
        assertRowAlignment(fontScale = 1f, direction = LayoutDirection.Rtl)
    }

    @Test
    fun sheet_withLtrContent_alignsHeaderAndActionColumns() {
        assertSheetAlignment(fontScale = 1f, direction = LayoutDirection.Ltr)
    }

    @Test
    fun sheet_withRtlContent_mirrorsHeaderAndActionColumns() {
        assertSheetAlignment(fontScale = 1f, direction = LayoutDirection.Rtl)
    }

    @Test
    fun sheet_withLargeText_keepsHeaderAndActionColumnsAligned() {
        assertSheetAlignment(fontScale = 2f, direction = LayoutDirection.Ltr)
    }

    private fun assertRowAlignment(fontScale: Float, direction: LayoutDirection) {
        setContent(fontScale, direction, isSheet = false)
        val secondary = node(recentsItemSecondaryTextTestTag(ENTRY_ID))
        val textBounds = secondary.getUnclippedBoundsInRoot()
        val layout = secondary.layout()
        val marker = node(RECENTS_CALL_TYPE_ICON_TEST_TAG).getUnclippedBoundsInRoot()
        val markerCenter = (marker.top + marker.bottom) / 2
        val count = node(RECENTS_GROUP_COUNT_TEST_TAG)
        val firstLineCenter =
            textBounds.top + pixelsToDp((layout.getLineTop(0) + layout.getLineBottom(0)) / 2)
        val countBaseline =
            count.getUnclippedBoundsInRoot().top + pixelsToDp(count.layout().firstBaseline)
        val textBaseline = textBounds.top + pixelsToDp(layout.firstBaseline)
        val primary = node(
            recentsItemPrimaryTextTestTag(ENTRY_ID)
        ).getUnclippedBoundsInRoot()
        val account = node(recentsItemAccountTestTag(ENTRY_ID)).getUnclippedBoundsInRoot()

        Log.i(
            TAG,
            "row $fontScale $direction marker=$markerCenter line=$firstLineCenter " +
                "countBaseline=$countBaseline textBaseline=$textBaseline " +
                "primary=$primary account=$account"
        )
        assertEquals(2, layout.lineCount)
        markerCenter.assertIsEqualTo(
            firstLineCenter,
            "marker center",
            tolerance()
        )
        countBaseline.assertIsEqualTo(textBaseline, "first baseline", tolerance())
        if (direction == LayoutDirection.Ltr) {
            account.left.assertIsEqualTo(primary.left, "text start", tolerance())
            pixelsToDp(layout.getLineLeft(1)).assertIsEqualTo(
                pixelsToDp(layout.getLineLeft(0)),
                "wrapped line start",
                tolerance()
            )
        } else {
            account.right.assertIsEqualTo(primary.right, "RTL text start", tolerance())
        }
    }

    private fun assertSheetAlignment(fontScale: Float, direction: LayoutDirection) {
        setContent(fontScale, direction, isSheet = true)
        val title = node(RECENTS_SHEET_TITLE_TEST_TAG).getUnclippedBoundsInRoot()
        val avatar = node(RECENTS_SHEET_AVATAR_TEST_TAG).getUnclippedBoundsInRoot()
        val icons = composeTestRule.onAllNodesWithTag(RECENTS_SHEET_ACTION_ICON_TEST_TAG, true)
        val iconCount = icons.fetchSemanticsNodes().size
        assertTrue("fixture must exercise multiple action rows", iconCount >= 5)
        for (index in 0 until iconCount) {
            val icon = icons[index].getUnclippedBoundsInRoot()
            val label = icons[index].onParent().onChildren()
                .filter(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text)).onFirst()
                .getUnclippedBoundsInRoot()
            assertTrue("action label must be below the header", label.top >= title.bottom)
            val avatarCenter = (avatar.left + avatar.right) / 2
            val iconCenter = (icon.left + icon.right) / 2
            Log.i(
                TAG,
                "sheet $fontScale $direction action=$index title=$title label=$label " +
                    "avatarCenter=$avatarCenter iconCenter=$iconCenter"
            )
            val titleStart = if (direction == LayoutDirection.Ltr) title.left else title.right
            val labelStart = if (direction == LayoutDirection.Ltr) label.left else label.right
            labelStart.assertIsEqualTo(titleStart, "action $index text column", tolerance())
            iconCenter.assertIsEqualTo(avatarCenter, "action $index leading center", tolerance())
        }
    }

    private fun setContent(fontScale: Float, direction: LayoutDirection, isSheet: Boolean) {
        composeTestRule.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(
                LocalDensity provides Density(density, fontScale),
                LocalLayoutDirection provides direction,
            ) {
                DialerTheme {
                    val item = previewRecentsItem(
                        entryId = ENTRY_ID,
                        primaryText = "Migration Parity",
                        secondaryText = "reviewer42@example.invalid • 10 minutes ago",
                        groupedCallCountLabel = "(12)",
                    ).copy(accountLabel = "Work subscription via +12025550187")
                    Box(Modifier.width(360.dp)) {
                        if (isSheet) {
                            RecentsActionsSheetContent(item, previewActionLabels(), {})
                        } else {
                            RecentsItemRow(item, {}, onCallClick = {})
                        }
                    }
                }
            }
        }
    }

    private fun pixelsToDp(pixels: Float): Dp {
        return with(composeTestRule.density) { pixels.toDp() }
    }

    private fun tolerance(): Dp {
        return pixelsToDp(1f)
    }

    private fun node(tag: String): SemanticsNodeInteraction {
        return composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)
    }

    private fun SemanticsNodeInteraction.layout(): TextLayoutResult {
        val layouts = mutableListOf<TextLayoutResult>()
        performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        return layouts.single()
    }

    private companion object {
        const val TAG = "RecentsAlignment"
        val ENTRY_ID = CallLogEntryId(7L)
    }
}
