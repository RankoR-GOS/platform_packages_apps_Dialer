package com.android.dialer

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ComposeUnitTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun composableRendersText() {
        composeRule.setContent { Text("Dialer") }

        composeRule.onNodeWithText("Dialer").assertIsDisplayed()
    }
}
