package com.android.dialer.ui.recents.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.abs
import kotlin.math.roundToInt

private const val DARK_THEME_LUMINANCE_THRESHOLD = 0.5f
private const val FULL_HUE_CIRCLE_DEGREES = 360f
private const val HUE_SEGMENT_DEGREES = 60f
private const val GOLDEN_ANGLE_DEGREES = 137.508f
private const val BYTE_COLOR_MAX_VALUE = 255
private const val FNV_OFFSET_BASIS = -0x7ee3623b
private const val FNV_PRIME = 0x01000193

private const val LIGHT_THEME_BACKGROUND_SATURATION = 0.58f
private const val LIGHT_THEME_BACKGROUND_LIGHTNESS = 0.82f
private const val LIGHT_THEME_CONTENT_SATURATION = 0.82f
private const val LIGHT_THEME_CONTENT_LIGHTNESS = 0.22f

private const val DARK_THEME_BACKGROUND_SATURATION = 0.48f
private const val DARK_THEME_BACKGROUND_LIGHTNESS = 0.30f
private const val DARK_THEME_CONTENT_SATURATION = 0.70f
private const val DARK_THEME_CONTENT_LIGHTNESS = 0.88f

internal data class RecentsAvatarColors(
    val background: Color,
    val content: Color,
)

internal fun avatarColorSeed(number: String): String? {
    return number.filterNot { it.isWhitespace() }.takeIf { it.isNotEmpty() }
}

@Composable
internal fun resolvedAvatarColors(seed: String?): RecentsAvatarColors {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkTheme = colorScheme.background.luminance() < DARK_THEME_LUMINANCE_THRESHOLD

    return when (seed) {
        null -> RecentsAvatarColors(
            background = colorScheme.primaryContainer,
            content = colorScheme.onPrimaryContainer,
        )

        else -> remember(seed, isDarkTheme) {
            recentsAvatarColors(seed = seed, isDarkTheme = isDarkTheme)
        }
    }
}

internal fun recentsAvatarColors(seed: String, isDarkTheme: Boolean): RecentsAvatarColors {
    val hue = avatarHue(seed = seed)

    return when {
        isDarkTheme -> RecentsAvatarColors(
            background = hslColor(
                hue = hue,
                saturation = DARK_THEME_BACKGROUND_SATURATION,
                lightness = DARK_THEME_BACKGROUND_LIGHTNESS,
            ),
            content = hslColor(
                hue = hue,
                saturation = DARK_THEME_CONTENT_SATURATION,
                lightness = DARK_THEME_CONTENT_LIGHTNESS,
            ),
        )

        else -> RecentsAvatarColors(
            background = hslColor(
                hue = hue,
                saturation = LIGHT_THEME_BACKGROUND_SATURATION,
                lightness = LIGHT_THEME_BACKGROUND_LIGHTNESS,
            ),
            content = hslColor(
                hue = hue,
                saturation = LIGHT_THEME_CONTENT_SATURATION,
                lightness = LIGHT_THEME_CONTENT_LIGHTNESS,
            ),
        )
    }
}

private fun avatarHue(seed: String): Float {
    val positiveHash = seed.stableHashCode() and Int.MAX_VALUE

    return (positiveHash * GOLDEN_ANGLE_DEGREES) % FULL_HUE_CIRCLE_DEGREES
}

private fun String.stableHashCode(): Int {
    var hash = FNV_OFFSET_BASIS

    forEach { character ->
        hash = hash xor character.code
        hash *= FNV_PRIME
    }

    return hash
}

private fun hslColor(hue: Float, saturation: Float, lightness: Float): Color {
    val chroma = (1f - abs(2f * lightness - 1f)) * saturation
    val huePrime = hue / HUE_SEGMENT_DEGREES
    val secondLargestComponent = chroma * (1f - abs(huePrime % 2f - 1f))
    val lightnessMatch = lightness - chroma / 2f

    val sextantComponents = listOf(
        Triple(chroma, secondLargestComponent, 0f),
        Triple(secondLargestComponent, chroma, 0f),
        Triple(0f, chroma, secondLargestComponent),
        Triple(0f, secondLargestComponent, chroma),
        Triple(secondLargestComponent, 0f, chroma),
        Triple(chroma, 0f, secondLargestComponent),
    )
    val sextant = huePrime.toInt().coerceIn(0, sextantComponents.lastIndex)
    val (redPrime, greenPrime, bluePrime) = sextantComponents[sextant]

    return Color(
        red = (redPrime + lightnessMatch).toByteColorComponent(),
        green = (greenPrime + lightnessMatch).toByteColorComponent(),
        blue = (bluePrime + lightnessMatch).toByteColorComponent(),
    )
}

private fun Float.toByteColorComponent(): Int {
    return (coerceIn(minimumValue = 0f, maximumValue = 1f) * BYTE_COLOR_MAX_VALUE).roundToInt()
}
