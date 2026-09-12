package com.android.dialer.ui.recents.component.recentsactionssheet

import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import com.android.dialer.testutil.hasNoText
import com.android.dialer.ui.recents.common.RECENTS_SHEET_ADD_CONTACT_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_AVATAR_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_BLOCK_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_CALL_DETAILS_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_CALL_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_CONTENT_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_COPY_NUMBER_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_CREATE_CONTACT_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_DELETE_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_EDIT_NUMBER_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_MESSAGE_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_SUBTITLE_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_TITLE_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_VIDEO_CALL_TEST_TAG
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsActionsSheetVisibilityTest : BaseRecentsActionsSheetTest() {

    @Test
    fun header_showsThePrimaryTextAndTheNumber() {
        setContent(item = item())

        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_TITLE_TEST_TAG)
            .assertTextEquals(PRIMARY_TEXT)
        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_SUBTITLE_TEST_TAG)
            .assertTextEquals(DISPLAY_NUMBER)
    }

    @Test
    fun header_avatar_isPresentAndSilent() {
        setContent(item = item())

        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_AVATAR_TEST_TAG)
            .assertIsDisplayed()
            .assert(matcher = hasNoText())
            .assert(matcher = hasAnyDescendant(matcher = hasNoText().not()).not())
    }

    @Test
    fun header_whenThePrimaryTextIsTheNumber_showsNoSubtitle() {
        setContent(item = item(primaryText = DISPLAY_NUMBER))

        composeTestRule.onAllNodesWithTag(testTag = RECENTS_SHEET_SUBTITLE_TEST_TAG)
            .assertCountEquals(expectedSize = 0)
    }

    @Test
    fun header_speaksTheSubtitleNumberDigitByDigit() {
        setContent(item = item())

        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_SUBTITLE_TEST_TAG)
            .assertContentDescriptionEquals(SPOKEN_NUMBER)
    }

    @Test
    fun header_whenThePrimaryTextIsTheNumber_speaksTheTitleDigitByDigit() {
        setContent(item = item(primaryText = DISPLAY_NUMBER))

        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_TITLE_TEST_TAG)
            .assertContentDescriptionEquals(SPOKEN_NUMBER)
    }

    @Test
    fun header_whenThePrimaryTextIsAName_speaksTheTitleAsWritten() {
        val noDescription = SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription)
        setContent(item = item())

        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_TITLE_TEST_TAG)
            .assert(matcher = noDescription)
    }

    @Test
    fun header_whenThePrimaryTextIsTheNumber_laysTheTitleOutLeftToRight() {
        setContent(item = item(primaryText = DISPLAY_NUMBER), layoutDirection = LayoutDirection.Rtl)

        assertEquals(TextDirection.Ltr, textDirection(tag = RECENTS_SHEET_TITLE_TEST_TAG))
    }

    @Test
    fun header_laysTheSubtitleNumberOutLeftToRight() {
        setContent(item = item(), layoutDirection = LayoutDirection.Rtl)

        assertEquals(TextDirection.Ltr, textDirection(tag = RECENTS_SHEET_SUBTITLE_TEST_TAG))
    }

    private fun textDirection(tag: String): TextDirection {
        val layouts = mutableListOf<TextLayoutResult>()

        composeTestRule.onNodeWithTag(testTag = tag)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { fetch ->
                fetch(layouts)
            }

        return layouts.single().layoutInput.style.textDirection
    }

    @Test
    fun sheet_withACallableContact_showsEveryActionInTheLegacyOrder() {
        setContent(item = item(canVideoCall = true))

        listOf(
            RECENTS_SHEET_CALL_TEST_TAG,
            RECENTS_SHEET_VIDEO_CALL_TEST_TAG,
            RECENTS_SHEET_MESSAGE_TEST_TAG,
            RECENTS_SHEET_CREATE_CONTACT_TEST_TAG,
            RECENTS_SHEET_ADD_CONTACT_TEST_TAG,
            RECENTS_SHEET_COPY_NUMBER_TEST_TAG,
            RECENTS_SHEET_EDIT_NUMBER_TEST_TAG,
            RECENTS_SHEET_BLOCK_TEST_TAG,
            RECENTS_SHEET_CALL_DETAILS_TEST_TAG,
            RECENTS_SHEET_DELETE_TEST_TAG,
        ).zipWithNext().forEach { (above, below) ->
            val aboveTop = composeTestRule.onNodeWithTag(testTag = above)
                .fetchSemanticsNode().positionInRoot.y
            val belowTop = composeTestRule.onNodeWithTag(testTag = below)
                .fetchSemanticsNode().positionInRoot.y
            check(aboveTop < belowTop) { "$above should sit above $below" }
        }
    }

    @Test
    fun sheet_whenTheNumberCannotBeCalled_showsOnlyCallDetailsAndDelete() {
        setContent(
            item = item(canCallBack = false, canMessage = false, canAddContact = false),
        )

        listOf(
            RECENTS_SHEET_CALL_TEST_TAG,
            RECENTS_SHEET_VIDEO_CALL_TEST_TAG,
            RECENTS_SHEET_MESSAGE_TEST_TAG,
            RECENTS_SHEET_CREATE_CONTACT_TEST_TAG,
            RECENTS_SHEET_ADD_CONTACT_TEST_TAG,
            RECENTS_SHEET_COPY_NUMBER_TEST_TAG,
            RECENTS_SHEET_EDIT_NUMBER_TEST_TAG,
            RECENTS_SHEET_BLOCK_TEST_TAG,
        ).forEach { tag ->
            composeTestRule.onAllNodesWithTag(testTag = tag).assertCountEquals(expectedSize = 0)
        }
        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_CALL_DETAILS_TEST_TAG)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_DELETE_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun sheet_whenTheCallWasNotVideo_hidesTheVideoCall() {
        setContent(item = item(canVideoCall = false))

        composeTestRule.onAllNodesWithTag(testTag = RECENTS_SHEET_VIDEO_CALL_TEST_TAG)
            .assertCountEquals(expectedSize = 0)
    }

    @Test
    fun sheet_whenTheNumberIsAContact_hidesBothAddContactActions() {
        setContent(item = item(canAddContact = false))

        composeTestRule.onAllNodesWithTag(testTag = RECENTS_SHEET_CREATE_CONTACT_TEST_TAG)
            .assertCountEquals(expectedSize = 0)
        composeTestRule.onAllNodesWithTag(testTag = RECENTS_SHEET_ADD_CONTACT_TEST_TAG)
            .assertCountEquals(expectedSize = 0)
    }

    @Test
    fun sheet_whenRendered_scrollsForShortWindowsAndLargeFontScales() {
        setContent(item = item(canVideoCall = true))

        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_CONTENT_TEST_TAG)
            .assert(hasScrollAction())
    }

    @Test
    fun sheet_rendersBlockAndCallDetailsDisabled() {
        setContent(item = item())

        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_BLOCK_TEST_TAG).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_CALL_DETAILS_TEST_TAG)
            .assertIsNotEnabled()
        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_DELETE_TEST_TAG).assertIsEnabled()
    }

    @Test
    fun sheet_withAVoicemailNumber_keepsCallCopyAndDeleteAndHidesIneligibleActions() {
        setContent(
            item = item(
                canVideoCall = false,
                canMessage = false,
                canAddContact = false,
                canEditNumberBeforeCall = false,
            ).copy(canBlockNumber = false),
        )

        listOf(
            RECENTS_SHEET_CALL_TEST_TAG,
            RECENTS_SHEET_COPY_NUMBER_TEST_TAG,
            RECENTS_SHEET_DELETE_TEST_TAG,
        ).forEach { tag ->
            composeTestRule.onNodeWithTag(testTag = tag).assertExists()
        }
        listOf(
            RECENTS_SHEET_VIDEO_CALL_TEST_TAG,
            RECENTS_SHEET_MESSAGE_TEST_TAG,
            RECENTS_SHEET_ADD_CONTACT_TEST_TAG,
            RECENTS_SHEET_CREATE_CONTACT_TEST_TAG,
            RECENTS_SHEET_EDIT_NUMBER_TEST_TAG,
            RECENTS_SHEET_BLOCK_TEST_TAG,
        ).forEach { tag ->
            composeTestRule.onAllNodesWithTag(testTag = tag).assertCountEquals(expectedSize = 0)
        }
    }
}
