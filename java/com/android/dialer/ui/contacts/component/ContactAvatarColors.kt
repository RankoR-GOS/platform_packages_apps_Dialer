package com.android.dialer.ui.contacts.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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

private const val LIGHT_BACKGROUND_SATURATION = 0.58f
private const val LIGHT_BACKGROUND_LIGHTNESS = 0.82f
private const val LIGHT_CONTENT_SATURATION = 0.82f
private const val LIGHT_CONTENT_LIGHTNESS = 0.22f

private const val DARK_BACKGROUND_SATURATION = 0.48f
private const val DARK_BACKGROUND_LIGHTNESS = 0.30f
private const val DARK_CONTENT_SATURATION = 0.70f
private const val DARK_CONTENT_LIGHTNESS = 0.88f

@Immutable
internal data class ContactAvatarColors(
    val background: Color,
    val content: Color,
)

@Composable
internal fun rememberContactAvatarColors(colorSeed: String?): ContactAvatarColors {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkTheme = colorScheme.background.luminance() < DARK_THEME_LUMINANCE_THRESHOLD

    return when {
        colorSeed.isNullOrBlank() -> ContactAvatarColors(
            background = colorScheme.primaryContainer,
            content = colorScheme.onPrimaryContainer,
        )

        else -> remember(colorSeed, isDarkTheme) {
            contactAvatarColors(colorSeed = colorSeed, isDarkTheme = isDarkTheme)
        }
    }
}

internal fun contactAvatarColors(colorSeed: String, isDarkTheme: Boolean): ContactAvatarColors {
    val hue = contactAvatarHue(colorSeed = colorSeed)

    return when {
        isDarkTheme -> ContactAvatarColors(
            background = hslColor(hue, DARK_BACKGROUND_SATURATION, DARK_BACKGROUND_LIGHTNESS),
            content = hslColor(hue, DARK_CONTENT_SATURATION, DARK_CONTENT_LIGHTNESS),
        )

        else -> ContactAvatarColors(
            background = hslColor(hue, LIGHT_BACKGROUND_SATURATION, LIGHT_BACKGROUND_LIGHTNESS),
            content = hslColor(hue, LIGHT_CONTENT_SATURATION, LIGHT_CONTENT_LIGHTNESS),
        )
    }
}

private fun contactAvatarHue(colorSeed: String): Float {
    val positiveHash = colorSeed.stableHashCode() and Int.MAX_VALUE

    return (positiveHash * GOLDEN_ANGLE_DEGREES) % FULL_HUE_CIRCLE_DEGREES
}

// String.hashCode is stable across JVMs, but FNV-1a keeps colours identical to Messaging's.
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

private fun Float.toByteColorComponent(): Int =
    (coerceIn(minimumValue = 0f, maximumValue = 1f) * BYTE_COLOR_MAX_VALUE).roundToInt()
