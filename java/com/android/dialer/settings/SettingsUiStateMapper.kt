package com.android.dialer.settings

import com.android.dialer.settings.model.CallRecordingOutputFormat
import com.android.dialer.settings.model.CallRecordingSettings
import com.android.dialer.settings.model.CallRecordingUiState
import com.android.dialer.settings.model.DisplayOptionsUiState
import com.android.dialer.settings.model.SettingsDialog
import com.android.dialer.settings.model.SettingsUiState
import com.android.dialer.settings.model.StoredSettings

internal fun mapSettingsUiState(
    stored: StoredSettings,
    static: SettingsStaticState,
    openDialog: SettingsDialog?,
): SettingsUiState = SettingsUiState(
    isLoading = false,
    displayOptions = DisplayOptionsUiState(
        sortOrder = stored.sortOrder,
        displayOrder = stored.displayOrder,
    ).takeIf { static.isNameOrderChangeable },
    callAccountsEntry = static.callAccountsEntry,
    isAccessibilityVisible = static.isAccessibilityVisible,
    callRecording = stored.callRecording.toUiState(),
    versionName = static.versionName,
    openDialog = openDialog?.takeIf { it.isAvailable(static) },
)

private fun CallRecordingSettings.toUiState(): CallRecordingUiState = CallRecordingUiState(
    isV2Enabled = isV2Enabled,
    outputFormat = if (isV2Enabled) outputFormatV2 else outputFormatV1,
    availableOutputFormats = if (isV2Enabled) {
        CallRecordingOutputFormat.V2_FORMATS
    } else {
        CallRecordingOutputFormat.V1_FORMATS
    },
    audioSource = audioSource,
)

private fun SettingsDialog.isAvailable(static: SettingsStaticState): Boolean = when (this) {
    SettingsDialog.SORT_ORDER, SettingsDialog.DISPLAY_ORDER -> static.isNameOrderChangeable
    SettingsDialog.OUTPUT_FORMAT, SettingsDialog.AUDIO_SOURCE -> true
}
