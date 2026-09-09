package com.android.dialer.ui.recents.component

import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.font.FontWeight
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.testutil.RobolectricComposeActivityRule
import com.android.dialer.testutil.hasClickLabel
import com.android.dialer.testutil.hasCustomAction
import com.android.dialer.testutil.hasCustomActionCount
import com.android.dialer.ui.recents.common.previewRecentsItem
import com.android.dialer.ui.recents.common.recentsItemAvatarTestTag
import com.android.dialer.ui.recents.common.recentsItemCallButtonTestTag
import com.android.dialer.ui.recents.common.recentsItemTestTag
import com.android.dialer.ui.recents.model.RecentsItemUiModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsItemRowTest : BaseRecentsItemRowTest() {

    @get:Rule(order = 0)
    val componentActivityRule = RobolectricComposeActivityRule()

    @Test
    fun row_whenRendered_isTaggedByEntryId() {
        setContent(item = item())

        composeTestRule.onNodeWithTag(testTag = ROW_TAG).assertIsDisplayed()
    }

    @Test
    fun row_whenRendered_carriesTheMergedContentDescription() {
        setContent(item = item())

        composeTestRule.onNodeWithTag(testTag = ROW_TAG)
            .assertContentDescriptionEquals(ROW_DESCRIPTION)
    }

    @Test
    fun row_whenRendered_labelsItsClickWithTheExpandMenuAction() {
        setContent(item = item())

        composeTestRule.onNodeWithTag(testTag = ROW_TAG)
            .assert(matcher = hasClickLabel(label = CLICK_LABEL))
    }

    @Test
    fun row_click_routesToTheClickCallback() {
        var clicks = 0

        setContent(item = item(), onClick = { clicks += 1 })

        composeTestRule.onNodeWithTag(testTag = ROW_TAG).performClick()

        composeTestRule.runOnIdle {
            assertEquals(1, clicks)
        }
    }

    @Test
    fun callButton_whenTheNumberCanBeCalled_isDisplayedWithItsDescription() {
        setContent(item = item(), onCallClick = {})

        composeTestRule.onNodeWithTag(testTag = CALL_BUTTON_TAG)
            .assertIsDisplayed()
            .assertContentDescriptionEquals(CALL_LABEL)
    }

    @Test
    fun callButton_whenTheNumberCannotBeCalled_isNotRendered() {
        setContent(item = item(canCallBack = false), onCallClick = {})

        composeTestRule.onAllNodesWithTag(testTag = CALL_BUTTON_TAG)
            .assertCountEquals(expectedSize = 0)
    }

    @Test
    fun callButton_click_routesToTheCallCallback() {
        var callClicks = 0

        setContent(item = item(), onCallClick = { callClicks += 1 })

        composeTestRule.onNodeWithTag(testTag = CALL_BUTTON_TAG).performClick()

        composeTestRule.runOnIdle {
            assertEquals(1, callClicks)
        }
    }

    @Test
    fun callButton_whenTheCallWasVideo_routesToTheVideoCallCallback() {
        var callClicks = 0
        var videoCallClicks = 0

        setContent(
            item = item(canVideoCall = true),
            onCallClick = { callClicks += 1 },
            onVideoCallClick = { videoCallClicks += 1 },
        )

        composeTestRule.onNodeWithTag(testTag = CALL_BUTTON_TAG).performClick()

        composeTestRule.runOnIdle {
            assertEquals(0, callClicks)
            assertEquals(1, videoCallClicks)
        }
    }

    @Test
    fun row_withACallAction_exposesItAsACustomAction() {
        setContent(item = item(), onCallClick = {})

        composeTestRule.onNodeWithTag(testTag = ROW_TAG)
            .assert(matcher = hasCustomActionCount(count = 1))
            .assert(matcher = hasCustomAction(label = CALL_LABEL))
    }

    @Test
    fun customAction_whenPerformed_routesToTheCallCallback() {
        var callClicks = 0

        setContent(item = item(), onCallClick = { callClicks += 1 })

        composeTestRule.runOnIdle {
            composeTestRule.onNodeWithTag(testTag = ROW_TAG)
                .fetchSemanticsNode()
                .config[SemanticsActions.CustomActions]
                .single()
                .action()
        }

        composeTestRule.runOnIdle {
            assertEquals(1, callClicks)
        }
    }

    @Test
    fun row_withoutACallAction_exposesNoCustomAction() {
        setContent(item = item(canCallBack = false), onCallClick = {})

        composeTestRule.onNodeWithTag(testTag = ROW_TAG)
            .assert(matcher = hasCustomActionCount(count = 0))
    }

    @Test
    fun avatar_whenRendered_isPresentAndInert() {
        setContent(item = item())

        composeTestRule.onNodeWithTag(testTag = AVATAR_TAG, useUnmergedTree = true)
            .assertIsDisplayed()
            .assert(matcher = hasClickAction().not())
    }

    @Test
    fun recentsItemFontWeight_withAnUnreadMissedCall_isMedium() {
        assertEquals(FontWeight.Medium, recentsItemFontWeight(isUnreadMissedCall = true))
    }

    @Test
    fun recentsItemFontWeight_withAReadCall_isNormal() {
        assertEquals(FontWeight.Normal, recentsItemFontWeight(isUnreadMissedCall = false))
    }

    private fun item(
        canCallBack: Boolean = true,
        canVideoCall: Boolean = false,
    ): RecentsItemUiModel {
        return previewRecentsItem(
            entryId = ENTRY_ID,
            primaryText = "Caller 7",
            contentDescription = ROW_DESCRIPTION,
            clickActionLabel = CLICK_LABEL,
            callActionLabel = CALL_LABEL.takeIf { canCallBack },
            canCallBack = canCallBack,
            canVideoCall = canVideoCall,
        )
    }

    private companion object {
        private val ENTRY_ID = CallLogEntryId(value = 7L)
        private val ROW_TAG = recentsItemTestTag(entryId = ENTRY_ID)
        private val CALL_BUTTON_TAG = recentsItemCallButtonTestTag(entryId = ENTRY_ID)
        private val AVATAR_TAG = recentsItemAvatarTestTag(entryId = ENTRY_ID)
        private const val ROW_DESCRIPTION = "1 answered call from Caller 7; 10:24"
        private const val CLICK_LABEL = "expand menu"
        private const val CALL_LABEL = "Call Caller 7"
    }
}
