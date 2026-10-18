package com.android.dialer.settings.model

import androidx.compose.runtime.Immutable
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.DisplayOrder
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.SortOrder
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class SettingsUiState(
    val isLoading: Boolean = true,
    val displayOptions: DisplayOptionsUiState? = null,
    val callAccountsEntry: CallAccountsEntry = CallAccountsEntry.CALL_SETTINGS,
    val isAccessibilityVisible: Boolean = false,
    val callRecording: CallRecordingUiState = CallRecordingUiState(),
    val versionName: String = "",
    val openDialog: SettingsDialog? = null,
)

@Immutable
data class DisplayOptionsUiState(
    val sortOrder: SortOrder,
    val displayOrder: DisplayOrder,
)

@Immutable
data class CallRecordingUiState(
    val isV2Enabled: Boolean = false,
    val outputFormat: CallRecordingOutputFormat = CallRecordingOutputFormat.DEFAULT,
    val availableOutputFormats: ImmutableList<CallRecordingOutputFormat> =
        CallRecordingOutputFormat.V1_FORMATS,
    val audioSource: CallRecordingAudioSource = CallRecordingAudioSource.DEFAULT,
)

enum class CallAccountsEntry {
    CALL_SETTINGS,
    CALLING_ACCOUNTS,
}

enum class SettingsDialog {
    SORT_ORDER,
    DISPLAY_ORDER,
    OUTPUT_FORMAT,
    AUDIO_SOURCE,
}
