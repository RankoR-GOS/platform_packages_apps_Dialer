package com.android.dialer.calldetails

import android.content.ComponentName
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.dialer.calldetails.ui.CallDetailsEntryRow
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
class CallDetailsEntryRowTest {

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
    fun entryRowDisplaysStatusIconAndTexts() {
        val entry = syntheticCallDetailsEntriesList().first()

        composeRule.setContent {
            CallDetailsTestTheme {
                CallDetailsEntryRow(entry = entry)
            }
        }

        val rowTag = callDetailsEntryTag(entry.callId)
        val iconTag = callDetailsEntryTypeIconTag(entry.callId)
        val typeTag = callDetailsEntryTypeTextTag(entry.callId)
        val dateTag = callDetailsEntryDateTextTag(entry.callId)

        composeRule.onNodeWithTag(rowTag).assertIsDisplayed()
        composeRule.onNodeWithTag(iconTag, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag(typeTag, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag(dateTag, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun entryRowDisplaysAccountLabelWhenProvided() {
        val entry = syntheticCallDetailsEntry(
            callId = 201L,
            accountLabel = "SIM 1",
        )

        composeRule.setContent {
            CallDetailsTestTheme {
                CallDetailsEntryRow(entry = entry)
            }
        }

        val accountTag = callDetailsEntryAccountTag(entry.callId)
        composeRule.onNodeWithTag(accountTag, useUnmergedTree = true).assertIsDisplayed()
    }
}
