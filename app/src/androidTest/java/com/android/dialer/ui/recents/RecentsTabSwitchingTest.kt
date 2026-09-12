package com.android.dialer.ui.recents

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.dialer.R
import com.android.dialer.main.impl.MainActivity
import com.android.dialer.ui.recents.common.RECENTS_SCREEN_TEST_TAG
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class RecentsTabSwitchingTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun recentsScreen_whenItsTabIsLeftAndReturnedTo_isDisplayedAgain() {
        selectTab(tabId = R.id.call_log_tab)
        recentsScreen().assertIsDisplayed()

        selectTab(tabId = R.id.contacts_tab)
        recentsScreen().assertIsNotDisplayed()

        selectTab(tabId = R.id.call_log_tab)
        recentsScreen().assertIsDisplayed()
    }

    @Test
    fun tabSwitching_awayAndBack_keepsExactlyOneFragmentUnderTheRecentsTag() {
        selectTab(tabId = R.id.call_log_tab)
        selectTab(tabId = R.id.speed_dial_tab)
        selectTab(tabId = R.id.call_log_tab)

        composeRule.runOnUiThread {
            val activity = composeRule.activity
            val supportFragments = activity.supportFragmentManager.fragments
                .filter { fragment -> fragment.tag == RECENTS_TAG }

            assertEquals(1, supportFragments.size)
            assertEquals(RecentsHostFragment::class.java, supportFragments.single().javaClass)
            @Suppress("DEPRECATION")
            assertNull(activity.fragmentManager.findFragmentByTag(RECENTS_TAG))
        }
    }

    private fun selectTab(tabId: Int) {
        onView(withId(tabId)).perform(click())
        composeRule.waitForIdle()
    }

    private fun recentsScreen() = composeRule.onNodeWithTag(testTag = RECENTS_SCREEN_TEST_TAG)

    private companion object {
        const val RECENTS_TAG = "recents"
    }
}
