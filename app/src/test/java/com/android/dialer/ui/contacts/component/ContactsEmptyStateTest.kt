package com.android.dialer.ui.contacts.component

import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.dialer.testing.robolectricComposeActivityRule
import com.android.dialer.ui.contacts.common.CONTACTS_EMPTY_STATE_ACTION_TEST_TAG
import com.android.dialer.ui.contacts.common.CONTACTS_EMPTY_STATE_MESSAGE_TEST_TAG
import com.android.dialer.ui.contacts.common.CONTACTS_EMPTY_STATE_TEST_TAG
import com.android.dialer.ui.core.DialerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
class ContactsEmptyStateTest {

    @get:Rule(order = 0)
    val activityRule = robolectricComposeActivityRule()

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private var actions = 0

    private fun render(
        message: String = "You don't have any contacts yet",
        actionLabel: String = "Create new contact",
    ) {
        composeRule.setContent {
            DialerTheme(darkTheme = false) {
                ContactsEmptyState(
                    message = message,
                    actionLabel = actionLabel,
                    onAction = { actions++ },
                )
            }
        }
    }

    @Test
    fun emptyStateIsTagged() {
        render()

        composeRule.onNodeWithTag(CONTACTS_EMPTY_STATE_TEST_TAG).assertIsDisplayed()
        composeRule
            .onNodeWithTag(CONTACTS_EMPTY_STATE_ACTION_TEST_TAG, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun showsTheMessageAndActionItWasGiven() {
        val message = "To see your contacts, turn on the Contacts permission."
        render(message = message, actionLabel = "Turn on")

        composeRule
            .onNodeWithTag(CONTACTS_EMPTY_STATE_MESSAGE_TEST_TAG, useUnmergedTree = true)
            .assertTextEquals(message)
        composeRule
            .onNodeWithTag(CONTACTS_EMPTY_STATE_ACTION_TEST_TAG)
            .assertTextEquals("Turn on")
    }

    @Test
    fun actionButtonReportsClicks() {
        render()

        composeRule.onNodeWithTag(CONTACTS_EMPTY_STATE_ACTION_TEST_TAG).performClick()

        assertEquals(1, actions)
    }
}
