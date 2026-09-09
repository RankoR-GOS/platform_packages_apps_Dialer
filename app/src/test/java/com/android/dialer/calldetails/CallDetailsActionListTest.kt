package com.android.dialer.calldetails

import android.content.ComponentName
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.dialer.calldetails.model.CallDetailsDeleteDialogState
import com.android.dialer.calldetails.ui.CallDetailsActionList
import com.android.dialer.calldetails.ui.CallDetailsDeleteDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
class CallDetailsActionListTest {

    @get:Rule(order = 0)
    val componentActivityRule = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                val application = RuntimeEnvironment.getApplication()
                shadowOf(application.packageManager).addActivityIfNotPresent(
                    ComponentName(application, TestCallDetailsActivity::class.java),
                )
                base.evaluate()
            }
        }
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<TestCallDetailsActivity>()

    @Test
    fun actionListDisplaysActionsAndTriggersCallbacks() {
        var copyClicked = false
        var editClicked = false
        var deleteClicked = false

        composeRule.setContent {
            CallDetailsTestTheme {
                CallDetailsActionList(
                    onCopyNumberClick = { copyClicked = true },
                    onEditNumberClick = { editClicked = true },
                    onDeleteCallLogClick = { deleteClicked = true },
                )
            }
        }

        composeRule
            .onNodeWithTag(CALL_DETAILS_COPY_ACTION_TAG)
            .assertIsDisplayed()
            .performClick()
        assertTrue(copyClicked)

        composeRule
            .onNodeWithTag(CALL_DETAILS_EDIT_NUMBER_ACTION_TAG)
            .assertIsDisplayed()
            .performClick()
        assertTrue(editClicked)

        composeRule
            .onNodeWithTag(CALL_DETAILS_DELETE_MENU_ITEM_TAG)
            .assertIsDisplayed()
            .performClick()
        assertTrue(deleteClicked)
    }

    @Test
    fun deleteDialogConfirmsAndDismisses() {
        var confirmedState: CallDetailsDeleteDialogState? = null

        composeRule.setContent {
            CallDetailsTestTheme {
                CallDetailsDeleteDialog(
                    state = CallDetailsDeleteDialogState.DeleteAll,
                    onConfirm = { confirmedState = it },
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithTag(CALL_DETAILS_DELETE_DIALOG_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(CALL_DETAILS_DELETE_CONFIRM_BUTTON_TAG).performClick()
        assertEquals(CallDetailsDeleteDialogState.DeleteAll, confirmedState)
    }
}
