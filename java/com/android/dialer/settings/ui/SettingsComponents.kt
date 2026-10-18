package com.android.dialer.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.dialer.theme.compose.DialerTheme

@Composable
internal fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = DialerTheme.spacing.medium,
                end = DialerTheme.spacing.medium,
                top = DialerTheme.spacing.large,
                bottom = DialerTheme.spacing.small,
            ),
    )
}

@Composable
internal fun SettingsItem(
    title: String,
    shapes: ListItemShapes,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    summary: String? = null,
    icon: ImageVector? = null,
) {
    if (onClick != null) {
        SegmentedListItem(
            onClick = onClick,
            shapes = shapes,
            modifier = modifier.fillMaxWidth(),
            leadingContent = icon?.let { { SettingsIcon(it) } },
            supportingContent = summary?.let { { Text(text = it) } },
            content = { Text(text = title) },
        )
    } else {
        SegmentedListItem(
            shapes = shapes,
            modifier = modifier.fillMaxWidth(),
            leadingContent = icon?.let { { SettingsIcon(it) } },
            supportingContent = summary?.let { { Text(text = it) } },
            content = { Text(text = title) },
        )
    }
}

@Composable
internal fun SettingsSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
    summary: String? = null,
    icon: ImageVector? = null,
) {
    SegmentedListItem(
        onClick = { onCheckedChange(!checked) },
        shapes = shapes,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Switch
                toggleableState = ToggleableState(checked)
            },
        leadingContent = icon?.let { { SettingsIcon(it) } },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        supportingContent = summary?.let { { Text(text = it) } },
        content = { Text(text = title) },
    )
}

@Composable
private fun SettingsIcon(icon: ImageVector) {
    Icon(imageVector = icon, contentDescription = null)
}

@PreviewLightDark
@Composable
private fun SettingsComponentsPreview() {
    DialerTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Column(
                modifier = Modifier.padding(DialerTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                SettingsSectionHeader(title = "Call recording")
                SettingsSwitchItem(
                    title = "Use call recording V2",
                    summary = "Allows WAV recording option",
                    checked = true,
                    onCheckedChange = {},
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = 3),
                )
                SettingsItem(
                    title = "Audio source",
                    summary = "VOICE_CALL (4)",
                    icon = Icons.Outlined.Mic,
                    onClick = {},
                    shapes = ListItemDefaults.segmentedShapes(index = 1, count = 3),
                )
                SettingsItem(
                    title = "Call recording notice",
                    icon = Icons.Outlined.Info,
                    onClick = null,
                    shapes = ListItemDefaults.segmentedShapes(index = 2, count = 3),
                )
            }
        }
    }
}
