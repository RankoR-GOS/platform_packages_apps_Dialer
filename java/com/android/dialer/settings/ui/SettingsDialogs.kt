package com.android.dialer.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.android.dialer.R
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.DisplayOrder
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.SortOrder
import com.android.dialer.settings.model.CallRecordingAudioSource
import com.android.dialer.settings.model.SettingsAction as Action
import com.android.dialer.settings.model.SettingsDialog
import com.android.dialer.settings.model.SettingsUiState
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun SettingsDialogs(
    uiState: SettingsUiState,
    onAction: (Action) -> Unit,
) {
    val onDismiss = { onAction(Action.DialogDismissed) }
    val displayOptions = uiState.displayOptions
    val callRecording = uiState.callRecording

    when (uiState.openDialog) {
        SettingsDialog.SORT_ORDER -> if (displayOptions != null) {
            SettingsChoiceDialog(
                title = stringResource(R.string.display_options_sort_list_by),
                options = SortOrder.entries.toImmutableList(),
                selected = displayOptions.sortOrder,
                optionLabel = { sortOrderLabel(it) },
                onSelect = { onAction(Action.SortOrderSelected(it)) },
                onDismiss = onDismiss,
            )
        }

        SettingsDialog.DISPLAY_ORDER -> if (displayOptions != null) {
            SettingsChoiceDialog(
                title = stringResource(R.string.display_options_view_names_as),
                options = DisplayOrder.entries.toImmutableList(),
                selected = displayOptions.displayOrder,
                optionLabel = { displayOrderLabel(it) },
                onSelect = { onAction(Action.DisplayOrderSelected(it)) },
                onDismiss = onDismiss,
            )
        }

        SettingsDialog.OUTPUT_FORMAT -> SettingsChoiceDialog(
            title = stringResource(
                if (callRecording.isV2Enabled) {
                    R.string.call_recording_output_format_title_v2
                } else {
                    R.string.call_recording_output_format_title
                },
            ),
            options = callRecording.availableOutputFormats,
            selected = callRecording.outputFormat,
            optionLabel = { outputFormatLabel(it) },
            onSelect = { onAction(Action.OutputFormatSelected(it)) },
            onDismiss = onDismiss,
        )

        SettingsDialog.AUDIO_SOURCE -> SettingsChoiceDialog(
            title = stringResource(R.string.call_recording_audio_source_title),
            options = CallRecordingAudioSource.entries.toImmutableList(),
            selected = callRecording.audioSource,
            optionLabel = { audioSourceLabel(it) },
            onSelect = { onAction(Action.AudioSourceSelected(it)) },
            onDismiss = onDismiss,
        )

        null -> Unit
    }
}
