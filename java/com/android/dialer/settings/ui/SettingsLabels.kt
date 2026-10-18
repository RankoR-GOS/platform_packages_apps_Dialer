package com.android.dialer.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.android.dialer.R
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.DisplayOrder
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.SortOrder
import com.android.dialer.settings.model.CallRecordingAudioSource
import com.android.dialer.settings.model.CallRecordingOutputFormat

@Composable
internal fun sortOrderLabel(sortOrder: SortOrder): String = stringResource(
    when (sortOrder) {
        SortOrder.BY_PRIMARY -> R.string.display_options_sort_by_given_name
        SortOrder.BY_ALTERNATIVE -> R.string.display_options_sort_by_family_name
    },
)

@Composable
internal fun displayOrderLabel(displayOrder: DisplayOrder): String = stringResource(
    when (displayOrder) {
        DisplayOrder.PRIMARY -> R.string.display_options_view_given_name_first
        DisplayOrder.ALTERNATIVE -> R.string.display_options_view_family_name_first
    },
)

@Composable
internal fun outputFormatLabel(outputFormat: CallRecordingOutputFormat): String = stringResource(
    when (outputFormat) {
        CallRecordingOutputFormat.AAC_MPEG4 -> R.string.call_recording_output_format_aac
        CallRecordingOutputFormat.AMR_WB -> R.string.call_recording_output_format_amr_wb
        CallRecordingOutputFormat.WAV -> R.string.call_recording_output_format_wav
    },
)

@Composable
internal fun audioSourceLabel(audioSource: CallRecordingAudioSource): String = stringResource(
    when (audioSource) {
        CallRecordingAudioSource.MIC -> R.string.call_recording_audio_source_mic
        CallRecordingAudioSource.VOICE_CALL -> R.string.call_recording_audio_source_voice_call
    },
)
