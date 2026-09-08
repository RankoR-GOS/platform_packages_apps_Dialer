package com.android.dialer.ui.core

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.android.dialer.testutil.RobolectricComposeActivityRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class PreviewTest {

    @get:Rule(order = 0)
    val componentActivityRule = RobolectricComposeActivityRule()

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private lateinit var shapes: Shapes

    private var contentColor: Color = Color.Unspecified
    private var onBackgroundColor: Color = Color.Unspecified

    @Test
    fun dialerPreviewTheme_appliesTheThemeAndTheBackgroundContentColour() {
        composeRule.setContent {
            DialerPreviewTheme { CaptureTheme() }
        }

        assertNotEquals(Shapes(), shapes)
        assertNotEquals(Color.Unspecified, contentColor)
        assertEquals(onBackgroundColor, contentColor)
    }

    @Test
    fun dialerPreviewTheme_doesNotInsetItsContent() {
        composeRule.setContent {
            DialerPreviewTheme { TaggedContent() }
        }

        composeRule.onNodeWithTag(testTag = CONTENT_TEST_TAG)
            .assertLeftPositionInRootIsEqualTo(expectedLeft = 0.dp)
            .assertTopPositionInRootIsEqualTo(expectedTop = 0.dp)
    }

    @Test
    fun dialerPreviewBox_appliesTheThemeAndTheBackgroundContentColour() {
        composeRule.setContent {
            DialerPreviewBox { CaptureTheme() }
        }

        assertNotEquals(Shapes(), shapes)
        assertNotEquals(Color.Unspecified, contentColor)
        assertEquals(onBackgroundColor, contentColor)
    }

    @Test
    fun dialerPreviewBox_insetsItsContentByThePreviewPadding() {
        composeRule.setContent {
            DialerPreviewBox { TaggedContent() }
        }

        composeRule.onNodeWithTag(testTag = CONTENT_TEST_TAG)
            .assertLeftPositionInRootIsEqualTo(expectedLeft = EXPECTED_PREVIEW_PADDING)
            .assertTopPositionInRootIsEqualTo(expectedTop = EXPECTED_PREVIEW_PADDING)
    }

    @Composable
    private fun CaptureTheme() {
        shapes = MaterialTheme.shapes
        contentColor = LocalContentColor.current
        onBackgroundColor = MaterialTheme.colorScheme.onBackground
    }

    @Composable
    private fun TaggedContent() {
        Box(
            modifier = Modifier
                .testTag(tag = CONTENT_TEST_TAG)
                .size(size = CONTENT_SIZE),
        )
    }

    private companion object {
        private const val CONTENT_TEST_TAG = "preview_wrapper_content"
        private val CONTENT_SIZE = 24.dp
        private val EXPECTED_PREVIEW_PADDING = 16.dp
    }
}
