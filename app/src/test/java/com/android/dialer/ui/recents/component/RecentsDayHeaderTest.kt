package com.android.dialer.ui.recents.component

import android.os.Build
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.dialer.testutil.RobolectricComposeActivityRule
import com.android.dialer.ui.core.DialerTheme
import com.android.dialer.ui.recents.common.RECENTS_DAY_HEADER_TEST_TAG
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsDayHeaderTest {

    @get:Rule(order = 0)
    val componentActivityRule = RobolectricComposeActivityRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Test
    fun header_whenRendered_isAnnouncedAsAHeading() {
        setContent()

        composeTestRule.onNodeWithTag(testTag = RECENTS_DAY_HEADER_TEST_TAG)
            .assert(matcher = isHeading())
    }

    private fun setContent() {
        composeTestRule.setContent {
            DialerTheme {
                RecentsDayHeader(label = LABEL)
            }
        }
    }

    private companion object {
        private const val LABEL = "Today"
    }
}
