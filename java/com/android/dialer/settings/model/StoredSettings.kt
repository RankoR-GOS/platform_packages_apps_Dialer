package com.android.dialer.settings.model

import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.DisplayOrder
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.SortOrder

data class StoredSettings(
    val sortOrder: SortOrder = SortOrder.BY_PRIMARY,
    val displayOrder: DisplayOrder = DisplayOrder.PRIMARY,
    val callRecording: CallRecordingSettings = CallRecordingSettings(),
)

data class CallRecordingSettings(
    val isV2Enabled: Boolean = false,
    val outputFormatV1: CallRecordingOutputFormat = CallRecordingOutputFormat.DEFAULT,
    val outputFormatV2: CallRecordingOutputFormat = CallRecordingOutputFormat.DEFAULT,
    val audioSource: CallRecordingAudioSource = CallRecordingAudioSource.DEFAULT,
)
