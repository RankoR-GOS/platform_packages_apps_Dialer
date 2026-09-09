package com.android.dialer.ui.common.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp

@Composable
internal fun horizontalSafeDrawingInsets(): PaddingValues {
    return WindowInsets.safeDrawing
        .only(sides = WindowInsetsSides.Horizontal)
        .asPaddingValues()
}

@Composable
internal fun safeDrawingContentPadding(
    top: Dp,
    bottom: Dp,
    horizontal: Dp,
): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    val horizontalInsets = horizontalSafeDrawingInsets()

    return PaddingValues(
        start = horizontal + horizontalInsets.calculateStartPadding(layoutDirection),
        top = top,
        end = horizontal + horizontalInsets.calculateEndPadding(layoutDirection),
        bottom = bottom,
    )
}
