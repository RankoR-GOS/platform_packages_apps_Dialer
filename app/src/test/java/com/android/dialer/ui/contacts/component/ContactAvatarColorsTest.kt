package com.android.dialer.ui.contacts.component

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactAvatarColorsTest {

    @Test
    fun sameSeedAlwaysProducesTheSameColours() {
        val first = contactAvatarColors(colorSeed = "lookup-1", isDarkTheme = false)
        val second = contactAvatarColors(colorSeed = "lookup-1", isDarkTheme = false)

        assertEquals(first, second)
    }

    @Test
    fun differentSeedsProduceDifferentColours() {
        val ada = contactAvatarColors(colorSeed = "lookup-ada", isDarkTheme = false)
        val grace = contactAvatarColors(colorSeed = "lookup-grace", isDarkTheme = false)

        assertNotEquals(ada.background, grace.background)
    }

    @Test
    fun seedsDifferingByOneCharacterStillSeparate() {
        val first = contactAvatarColors(colorSeed = "lookup-1", isDarkTheme = false)
        val second = contactAvatarColors(colorSeed = "lookup-2", isDarkTheme = false)

        assertNotEquals(first.background, second.background)
    }

    @Test
    fun darkThemeDarkensTheBackgroundAndLightensTheContent() {
        val light = contactAvatarColors(colorSeed = "lookup-1", isDarkTheme = false)
        val dark = contactAvatarColors(colorSeed = "lookup-1", isDarkTheme = true)

        assertTrue(dark.background.brightness() < light.background.brightness())
        assertTrue(dark.content.brightness() > light.content.brightness())
    }

    // The letter has to stay readable on its tile, in both themes and whatever the name is.
    @Test
    fun contentContrastsWithItsBackgroundForEverySeed() {
        val seeds = listOf("a", "lookup-42", "Ада", "李", "+38761123456", "")

        listOf(false, true).forEach { isDarkTheme ->
            seeds.forEach { seed ->
                val colors = contactAvatarColors(colorSeed = seed, isDarkTheme = isDarkTheme)
                val difference = abs(colors.background.brightness() - colors.content.brightness())

                assertTrue(
                    "seed=$seed dark=$isDarkTheme difference=$difference",
                    difference > MINIMUM_BRIGHTNESS_DIFFERENCE,
                )
            }
        }
    }

    private fun Color.brightness(): Float =
        RED_WEIGHT * red + GREEN_WEIGHT * green + BLUE_WEIGHT * blue

    private companion object {
        private const val MINIMUM_BRIGHTNESS_DIFFERENCE = 0.3f
        private const val RED_WEIGHT = 0.2126f
        private const val GREEN_WEIGHT = 0.7152f
        private const val BLUE_WEIGHT = 0.0722f
    }
}
