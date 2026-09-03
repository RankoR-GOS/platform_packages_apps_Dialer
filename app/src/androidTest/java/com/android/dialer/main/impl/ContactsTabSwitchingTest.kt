package com.android.dialer.main.impl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.android.dialer.ui.contacts.common.CONTACTS_LIST_TEST_TAG
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContactsTabSwitchingTest {

    @get:Rule(order = 0)
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.READ_CONTACTS)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun selectTab(label: String) {
        onView(withText(label)).perform(click())
        composeRule.waitForIdle()
    }

    private fun contactsList() = composeRule.onNodeWithTag(CONTACTS_LIST_TEST_TAG)

    @Test
    fun contactsSurvivesLeavingAndReturningToItsTab() {
        selectTab("Contacts")
        contactsList().assertIsDisplayed()

        selectTab("Recents")
        contactsList().assertIsNotDisplayed()

        selectTab("Contacts")
        contactsList().assertIsDisplayed()
    }

    @Test
    fun contactsSurvivesRepeatedTabSwitching() {
        repeat(times = 3) {
            selectTab("Contacts")
            contactsList().assertIsDisplayed()

            selectTab("Favorites")
            contactsList().assertIsNotDisplayed()
        }

        selectTab("Contacts")
        contactsList().assertIsDisplayed()
    }
}
