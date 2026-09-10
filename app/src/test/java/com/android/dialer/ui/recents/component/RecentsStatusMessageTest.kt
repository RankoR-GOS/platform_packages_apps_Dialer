package com.android.dialer.ui.recents.component

import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.dialer.testutil.RobolectricComposeActivityRule
import com.android.dialer.ui.core.DialerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsStatusMessageTest {

    @get:Rule(order = 0)
    val componentActivityRule = RobolectricComposeActivityRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Test
    fun statusMessage_withoutAnActionLabel_rendersNoButton() {
        setContent(actionLabel = null)

        composeTestRule.onAllNodesWithTag(testTag = ACTION_TEST_TAG)
            .assertCountEquals(expectedSize = 0)
    }

    @Test
    fun statusMessage_whenRendered_isScrollableForLargeFontScales() {
        setContent(actionLabel = ACTION_LABEL)

        composeTestRule.onNodeWithTag(testTag = ROOT_TEST_TAG)
            .assert(matcher = hasScrollAction())
    }

    private fun setContent(actionLabel: String?, onActionClick: () -> Unit = {}) {
        composeTestRule.setContent {
            DialerTheme {
                RecentsStatusMessage(
                    message = MESSAGE,
                    icon = Icons.Outlined.History,
                    modifier = Modifier.testTag(tag = ROOT_TEST_TAG),
                    actionLabel = actionLabel,
                    actionTestTag = ACTION_TEST_TAG,
                    onActionClick = onActionClick,
                )
            }
        }
    }

    private companion object {
        private const val ROOT_TEST_TAG = "status_message_under_test"
        private const val ACTION_TEST_TAG = "status_message_action_under_test"
        private const val MESSAGE = "Your call history is empty"
        private const val ACTION_LABEL = "Turn on"
    }
}
