package com.android.dialer.ui.recents.screen

import android.os.Build
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.dialer.testutil.RobolectricComposeActivityRule
import com.android.dialer.ui.core.DialerTheme
import com.android.dialer.ui.recents.common.RECENTS_EMPTY_STATE_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_LIST_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_PERMISSION_ACTION_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_PERMISSION_STATE_TEST_TAG
import com.android.dialer.ui.recents.model.RecentsContentUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsContentTest {

    @get:Rule(order = 0)
    val componentActivityRule = RobolectricComposeActivityRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private var grantPermissionClicks = 0

    @Test
    fun content_whenLoading_rendersNothing() {
        setContent(content = RecentsContentUiState.Loading)

        listOf(
            RECENTS_LIST_TEST_TAG,
            RECENTS_EMPTY_STATE_TEST_TAG,
            RECENTS_PERMISSION_STATE_TEST_TAG,
        ).forEach { tag ->
            composeTestRule.onAllNodesWithTag(testTag = tag).assertCountEquals(expectedSize = 0)
        }
    }

    @Test
    fun permissionState_actionClick_asksForThePermission() {
        setContent(
            content = RecentsContentUiState.PermissionRequired(
                message = "Turn on the permission",
                actionLabel = "Turn on",
            ),
        )

        composeTestRule.onNodeWithTag(testTag = RECENTS_PERMISSION_ACTION_TEST_TAG).performClick()

        composeTestRule.runOnIdle {
            assertEquals(1, grantPermissionClicks)
        }
    }

    private fun setContent(content: RecentsContentUiState) {
        composeTestRule.setContent {
            DialerTheme {
                RecentsContent(
                    content = content,
                    listState = rememberLazyListState(),
                    onItemEvent = {},
                    onGrantPermissionClick = { grantPermissionClicks++ },
                )
            }
        }
    }
}
