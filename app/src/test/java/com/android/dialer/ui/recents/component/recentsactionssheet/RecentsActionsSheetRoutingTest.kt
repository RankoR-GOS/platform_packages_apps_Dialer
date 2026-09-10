package com.android.dialer.ui.recents.component.recentsactionssheet

import android.os.Build
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.android.dialer.ui.recents.common.RECENTS_SHEET_ADD_CONTACT_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_BLOCK_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_CALL_DETAILS_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_CALL_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_COPY_NUMBER_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_CREATE_CONTACT_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_DELETE_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_EDIT_NUMBER_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_MESSAGE_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_VIDEO_CALL_TEST_TAG
import com.android.dialer.ui.recents.model.RecentsSheetAction as Action
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsActionsSheetRoutingTest : BaseRecentsActionsSheetTest() {

    @Test
    fun createContact_click_emitsCreateContact() {
        assertClickEmits(
            tag = RECENTS_SHEET_CREATE_CONTACT_TEST_TAG,
            expected = Action.CreateContact,
        )
    }

    @Test
    fun editNumberBeforeCall_click_emitsEditNumberBeforeCall() {
        assertClickEmits(
            tag = RECENTS_SHEET_EDIT_NUMBER_TEST_TAG,
            expected = Action.EditNumberBeforeCall,
        )
    }

    @Test
    fun delete_click_emitsDelete() {
        assertClickEmits(tag = RECENTS_SHEET_DELETE_TEST_TAG, expected = Action.Delete)
    }

    @Test
    fun block_click_emitsNothing() {
        setContent(item = item())

        composeTestRule.onNodeWithTag(testTag = RECENTS_SHEET_BLOCK_TEST_TAG)
            .performScrollTo()
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(emptyList<Action>(), emittedActions)
        }
    }

    private fun assertClickEmits(tag: String, expected: Action) {
        setContent(item = item())

        composeTestRule.onNodeWithTag(testTag = tag).performScrollTo().performClick()

        composeTestRule.runOnIdle {
            assertEquals(listOf(expected), emittedActions)
        }
    }
}
