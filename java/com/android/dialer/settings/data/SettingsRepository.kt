package com.android.dialer.settings.data

import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.DisplayOrder
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.SortOrder
import com.android.dialer.settings.model.CallRecorderVersion
import com.android.dialer.settings.model.CallRecordingAudioSource
import com.android.dialer.settings.model.CallRecordingOutputFormat
import com.android.dialer.settings.model.StoredSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    val settings: Flow<StoredSettings>

    fun setSortOrder(sortOrder: SortOrder)

    fun setDisplayOrder(displayOrder: DisplayOrder)

    fun setCallRecordingV2Enabled(enabled: Boolean)

    fun setCallRecordingOutputFormat(
        version: CallRecorderVersion,
        outputFormat: CallRecordingOutputFormat,
    )

    fun setCallRecordingAudioSource(audioSource: CallRecordingAudioSource)
}
