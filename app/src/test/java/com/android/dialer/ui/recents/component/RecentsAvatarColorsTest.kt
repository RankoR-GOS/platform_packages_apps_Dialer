package com.android.dialer.ui.recents.component

import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class RecentsAvatarColorsTest {

    @Test
    fun recentsAvatarColors_withTheSameSeed_returnsTheSameColors() {
        assertEquals(
            recentsAvatarColors(seed = SEED, isDarkTheme = false),
            recentsAvatarColors(seed = SEED, isDarkTheme = false),
        )
    }

    @Test
    fun recentsAvatarColors_withDifferentSeeds_returnsDifferentBackgrounds() {
        assertNotEquals(
            recentsAvatarColors(seed = SEED, isDarkTheme = false).background,
            recentsAvatarColors(seed = OTHER_SEED, isDarkTheme = false).background,
        )
    }

    @Test
    fun recentsAvatarColors_inTheLightTheme_putsDarkContentOnALightBackground() {
        val colors = recentsAvatarColors(seed = SEED, isDarkTheme = false)

        assertTrue(colors.background.luminance() > colors.content.luminance())
    }

    @Test
    fun recentsAvatarColors_inTheDarkTheme_putsLightContentOnADarkBackground() {
        val colors = recentsAvatarColors(seed = SEED, isDarkTheme = true)

        assertTrue(colors.content.luminance() > colors.background.luminance())
    }

    @Test
    fun avatarColorSeed_stripsWhitespaceFromTheNumber() {
        assertEquals("+15550001", avatarColorSeed(number = " +1 555 0001 "))
    }

    @Test
    fun avatarColorSeed_withABlankNumber_isNull() {
        assertNull(avatarColorSeed(number = "  "))
    }

    private companion object {
        private const val SEED = "+15550001"
        private const val OTHER_SEED = "+15550002"
    }
}
