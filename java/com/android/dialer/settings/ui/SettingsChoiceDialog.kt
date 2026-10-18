package com.android.dialer.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.dialer.settings.model.CallRecordingAudioSource
import com.android.dialer.theme.compose.DialerTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun <T : Enum<T>> SettingsChoiceDialog(
    title: String,
    options: ImmutableList<T>,
    selected: T,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(SETTINGS_CHOICE_DIALOG_CANCEL_BUTTON_TEST_TAG),
            ) {
                Text(text = stringResource(android.R.string.cancel))
            }
        },
        title = { Text(text = title) },
        text = {
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(DialerTheme.spacing.extraSmall),
            ) {
                options.forEach { option ->
                    ChoiceRow(
                        label = optionLabel(option),
                        isSelected = option == selected,
                        onClick = { onSelect(option) },
                        modifier = Modifier.testTag(settingsChoiceOptionTestTag(option)),
                    )
                }
            }
        },
        modifier = modifier.testTag(SETTINGS_CHOICE_DIALOG_TEST_TAG),
    )
}

@Composable
private fun ChoiceRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = LocalMinimumInteractiveComponentSize.current)
            .clip(MaterialTheme.shapes.large)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = DialerTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DialerTheme.spacing.medium),
    ) {
        RadioButton(selected = isSelected, onClick = null)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@PreviewLightDark
@Composable
private fun SettingsChoiceDialogPreview() {
    DialerTheme {
        SettingsChoiceDialog(
            title = "Audio source",
            options = CallRecordingAudioSource.entries.toImmutableList(),
            selected = CallRecordingAudioSource.VOICE_CALL,
            optionLabel = { it.name },
            onSelect = {},
            onDismiss = {},
        )
    }
}
