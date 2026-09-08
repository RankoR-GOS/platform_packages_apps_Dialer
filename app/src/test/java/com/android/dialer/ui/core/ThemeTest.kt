package com.android.dialer.ui.core

import android.content.Context
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.v2.createComposeRule
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
internal class ThemeTest {

    @get:Rule(order = 0)
    val componentActivityRule = RobolectricComposeActivityRule()

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private lateinit var themedContext: Context
    private lateinit var themedColorScheme: ColorScheme
    private lateinit var themedShapes: Shapes

    @Test
    fun dialerTheme_whenDarkThemeIsFalse_usesTheDynamicLightColorScheme() {
        composeRule.setContent {
            DialerTheme(darkTheme = false) { CaptureTheme() }
        }

        assertEquals(
            roleColors(scheme = dynamicLightColorScheme(context = themedContext)),
            roleColors(scheme = themedColorScheme),
        )
    }

    @Test
    fun dialerTheme_whenDarkThemeIsTrue_usesTheDynamicDarkColorScheme() {
        composeRule.setContent {
            DialerTheme(darkTheme = true) { CaptureTheme() }
        }

        assertEquals(
            roleColors(scheme = dynamicDarkColorScheme(context = themedContext)),
            roleColors(scheme = themedColorScheme),
        )
        assertNotEquals(
            roleColors(scheme = dynamicLightColorScheme(context = themedContext)),
            roleColors(scheme = themedColorScheme),
        )
    }

    @Test
    fun dialerTheme_whenDarkThemeIsNotGiven_inDayMode_usesTheDynamicLightColorScheme() {
        composeRule.setContent {
            DialerTheme { CaptureTheme() }
        }

        assertEquals(
            roleColors(scheme = dynamicLightColorScheme(context = themedContext)),
            roleColors(scheme = themedColorScheme),
        )
    }

    @Test
    @Config(qualifiers = "night")
    fun dialerTheme_whenDarkThemeIsNotGiven_inNightMode_usesTheDynamicDarkColorScheme() {
        composeRule.setContent {
            DialerTheme { CaptureTheme() }
        }

        assertEquals(
            roleColors(scheme = dynamicDarkColorScheme(context = themedContext)),
            roleColors(scheme = themedColorScheme),
        )
    }

    @Test
    fun dialerTheme_shapes_carryTheSharedCornerScale() {
        composeRule.setContent {
            DialerTheme(darkTheme = false) { CaptureTheme() }
        }

        assertEquals(
            Shapes(
                extraSmall = RoundedCornerShape(size = 12.dp),
                small = RoundedCornerShape(size = 16.dp),
                medium = RoundedCornerShape(size = 20.dp),
                large = RoundedCornerShape(size = 28.dp),
                extraLarge = RoundedCornerShape(size = 36.dp),
            ),
            themedShapes,
        )
    }

    @Composable
    private fun CaptureTheme() {
        themedContext = LocalContext.current
        themedColorScheme = MaterialTheme.colorScheme
        themedShapes = MaterialTheme.shapes
    }

    private fun roleColors(scheme: ColorScheme): List<Color> {
        return listOf(
            scheme.primary,
            scheme.onPrimary,
            scheme.primaryContainer,
            scheme.onPrimaryContainer,
            scheme.background,
            scheme.onBackground,
            scheme.surface,
            scheme.onSurface,
            scheme.surfaceVariant,
            scheme.onSurfaceVariant,
            scheme.surfaceContainer,
            scheme.surfaceContainerLow,
            scheme.error,
            scheme.onError,
            scheme.outline,
            scheme.outlineVariant,
            scheme.inverseSurface,
            scheme.scrim,
        )
    }
}
