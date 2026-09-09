package com.android.dialer.calldetails

import android.content.ComponentName
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.dialer.calldetails.ui.CallDetailsHeader
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
class CallDetailsHeaderTest {

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
    fun headerDisplaysContactInfoAndActions() {
        val header = syntheticCallDetailsHeader()

        composeRule.setContent {
            CallDetailsTestTheme {
                CallDetailsHeader(
                    header = header,
                    onCallClick = {},
                    onVideoCallClick = {},
                    onSendSmsClick = {},
                )
            }
        }

        composeRule.onNodeWithTag(CALL_DETAILS_HEADER_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(CALL_DETAILS_AVATAR_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(CALL_DETAILS_DISPLAY_NAME_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(CALL_DETAILS_PHONE_NUMBER_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(CALL_DETAILS_VOICE_CALL_ACTION_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(CALL_DETAILS_VIDEO_CALL_ACTION_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(CALL_DETAILS_SMS_ACTION_TAG).assertIsDisplayed()
    }

    @Test
    fun headerActionClicksTriggerCallbacks() {
        var callClicked = false
        var videoClicked = false
        var smsClicked = false

        composeRule.setContent {
            CallDetailsTestTheme {
                CallDetailsHeader(
                    header = syntheticCallDetailsHeader(),
                    onCallClick = { callClicked = true },
                    onVideoCallClick = { videoClicked = true },
                    onSendSmsClick = { smsClicked = true },
                )
            }
        }

        composeRule.onNodeWithTag(CALL_DETAILS_VOICE_CALL_ACTION_TAG).performClick()
        assertTrue(callClicked)

        composeRule.onNodeWithTag(CALL_DETAILS_VIDEO_CALL_ACTION_TAG).performClick()
        assertTrue(videoClicked)

        composeRule.onNodeWithTag(CALL_DETAILS_SMS_ACTION_TAG).performClick()
        assertTrue(smsClicked)
    }

    @Test
    fun headerDisplaysAccountLabelWhenPresent() {
        val header = syntheticCallDetailsHeader(accountLabel = "SIM 1")

        composeRule.setContent {
            CallDetailsTestTheme {
                CallDetailsHeader(
                    header = header,
                    onCallClick = {},
                    onVideoCallClick = {},
                    onSendSmsClick = {},
                )
            }
        }

        composeRule.onNodeWithTag(CALL_DETAILS_ACCOUNT_LABEL_TEST_TAG)
            .assertIsDisplayed()
            .assertTextEquals("SIM 1")
    }

    @Test
    fun headerAvatarClickTriggersCallbackWhenContactUriPresent() {
        var avatarClicked = false
        val header = syntheticCallDetailsHeader(
            contactUri = "content://com.android.contacts/contacts/lookup/123/456",
        )

        composeRule.setContent {
            CallDetailsTestTheme {
                CallDetailsHeader(
                    header = header,
                    onCallClick = {},
                    onVideoCallClick = {},
                    onSendSmsClick = {},
                    onAvatarClick = { avatarClicked = true },
                )
            }
        }

        composeRule.onNodeWithTag(CALL_DETAILS_AVATAR_TEST_TAG).performClick()
        assertTrue(avatarClicked)
    }
}
