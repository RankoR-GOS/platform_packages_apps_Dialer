package com.android.dialer.ui.core

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DialerShapes = Shapes(
    extraSmall = RoundedCornerShape(size = 12.dp),
    small = RoundedCornerShape(size = 16.dp),
    medium = RoundedCornerShape(size = 20.dp),
    large = RoundedCornerShape(size = 28.dp),
    extraLarge = RoundedCornerShape(size = 36.dp),
)

/**
 * [darkTheme] defaults to the system setting, which is what a preview, a test, or a future
 * single-Activity host wants. It is a parameter rather than a hardcoded lookup because during the
 * migration a Compose screen is hosted inside legacy chrome, and the two must agree: the legacy
 * `Theme.getTheme()` currently returns `LIGHT` unconditionally, so the bridge that hosts this
 * theme inside a Fragment passes that value explicitly instead of taking the default. Once the
 * legacy TODO for a theme preference is resolved, both sides follow the same source.
 */
@Composable
fun DialerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        darkTheme -> dynamicDarkColorScheme(context = context)
        else -> dynamicLightColorScheme(context = context)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = DialerShapes,
        content = content,
    )
}
