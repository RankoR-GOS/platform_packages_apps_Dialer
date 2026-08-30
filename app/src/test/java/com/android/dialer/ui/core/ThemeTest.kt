package com.android.dialer.ui.core

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.android.dialer.testing.robolectricComposeActivityRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
class ThemeTest {

    @get:Rule(order = 0)
    val activityRule = robolectricComposeActivityRule()

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @Test
    fun darkThemeFlagSwapsTheColorScheme() {
        val darkTheme = mutableStateOf(false)
        var scheme: ColorScheme? = null

        composeRule.setContent {
            DialerTheme(darkTheme = darkTheme.value) {
                scheme = MaterialTheme.colorScheme
            }
        }

        val light = requireNotNull(scheme)
        composeRule.runOnIdle { darkTheme.value = true }
        composeRule.waitForIdle()
        val dark = requireNotNull(scheme)

        assertTrue(
            "light background should be brighter than dark",
            light.background.luminance() > dark.background.luminance(),
        )
        assertTrue(
            "light onBackground should be darker than dark onBackground",
            light.onBackground.luminance() < dark.onBackground.luminance(),
        )
    }

    @Test
    fun themeOverridesTheMaterialDefaultShapeScale() {
        var shapes: Shapes? = null

        composeRule.setContent {
            DialerTheme(darkTheme = false) {
                shapes = MaterialTheme.shapes
            }
        }

        assertNotEquals(Shapes(), requireNotNull(shapes))
    }

    @Test
    fun themeUsesTheShapeScaleSharedWithMessaging() {
        var shapes: Shapes? = null

        composeRule.setContent {
            DialerTheme(darkTheme = false) {
                shapes = MaterialTheme.shapes
            }
        }

        assertEquals(RoundedCornerShape(size = 20.dp), requireNotNull(shapes).medium)
    }
}
