package com.android.dialer.ui.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val PreviewPadding = 16.dp

/**
 * Wraps preview content in [DialerTheme] plus a themed [Surface], so a `@PreviewLightDark`
 * renders against the right background instead of a transparent one.
 */
@Composable
internal fun DialerPreviewTheme(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    DialerTheme {
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            content = content,
        )
    }
}

/** [DialerPreviewTheme] with breathing room around a single component. */
@Composable
internal fun DialerPreviewBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    DialerPreviewTheme(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = PreviewPadding),
        ) {
            content()
        }
    }
}
