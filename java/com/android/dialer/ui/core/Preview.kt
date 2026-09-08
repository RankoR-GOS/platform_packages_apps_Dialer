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
