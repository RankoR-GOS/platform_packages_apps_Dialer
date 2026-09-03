package com.android.dialer.ui.contacts.component

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.dialer.R
import com.android.dialer.ui.contacts.common.CONTACTS_ADD_CONTACT_ROW_TEST_TAG
import com.android.dialer.ui.core.DialerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddContactRowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var clicks = 0

    private fun render() {
        composeRule.setContent {
            DialerTheme(darkTheme = false) {
                AddContactRow(onClick = { clicks++ })
            }
        }
    }

    @Test
    fun rowIsTagged() {
        render()

        composeRule.onNodeWithTag(CONTACTS_ADD_CONTACT_ROW_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun rowShowsTheCreateContactLabel() {
        render()

        val label = composeRule.activity.getString(R.string.all_contacts_empty_add_contact_action)

        composeRule
            .onNodeWithTag(CONTACTS_ADD_CONTACT_ROW_TEST_TAG)
            .assertTextContains(label)
    }

    @Test
    fun rowReportsClicks() {
        render()

        composeRule.onNodeWithTag(CONTACTS_ADD_CONTACT_ROW_TEST_TAG).performClick()

        assertEquals(1, clicks)
    }
}
