package com.android.dialer.settings

import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.DisplayOrder
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.SortOrder
import com.android.dialer.settings.data.SettingsEnvironment
import com.android.dialer.settings.data.SettingsRepository
import com.android.dialer.settings.model.CallRecorderVersion
import com.android.dialer.settings.model.CallRecordingAudioSource
import com.android.dialer.settings.model.CallRecordingOutputFormat
import com.android.dialer.settings.model.StoredSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class FakeSettingsRepository(
    initial: StoredSettings = StoredSettings(),
) : SettingsRepository {

    private val state = MutableStateFlow(initial)
    override val settings: StateFlow<StoredSettings> = state

    override fun setSortOrder(sortOrder: SortOrder) {
        state.update { it.copy(sortOrder = sortOrder) }
    }

    override fun setDisplayOrder(displayOrder: DisplayOrder) {
        state.update { it.copy(displayOrder = displayOrder) }
    }

    override fun setCallRecordingV2Enabled(enabled: Boolean) {
        state.update { it.copy(callRecording = it.callRecording.copy(isV2Enabled = enabled)) }
    }

    override fun setCallRecordingOutputFormat(
        version: CallRecorderVersion,
        outputFormat: CallRecordingOutputFormat,
    ) {
        state.update {
            val callRecording = when (version) {
                CallRecorderVersion.V1 -> it.callRecording.copy(outputFormatV1 = outputFormat)
                CallRecorderVersion.V2 -> it.callRecording.copy(outputFormatV2 = outputFormat)
            }
            it.copy(callRecording = callRecording)
        }
    }

    override fun setCallRecordingAudioSource(audioSource: CallRecordingAudioSource) {
        state.update { it.copy(callRecording = it.callRecording.copy(audioSource = audioSource)) }
    }
}

class FakeSettingsEnvironment(
    override val isNameOrderChangeable: Boolean = true,
    override val isPrimaryUser: Boolean = true,
    override val phoneCount: Int = 1,
    override val isCallAccessibilitySupported: Boolean = true,
    override val versionName: String = "23.0",
    var isSystemSettingsWritable: Boolean = true,
) : SettingsEnvironment {

    override fun canWriteSystemSettings(): Boolean = isSystemSettingsWritable
}
