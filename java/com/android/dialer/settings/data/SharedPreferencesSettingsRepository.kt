package com.android.dialer.settings.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.DisplayOrder
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.SortOrder
import com.android.dialer.coroutines.IoDispatcher
import com.android.dialer.settings.di.DefaultSharedPreferences
import com.android.dialer.settings.model.CallRecorderVersion
import com.android.dialer.settings.model.CallRecordingAudioSource
import com.android.dialer.settings.model.CallRecordingOutputFormat
import com.android.dialer.settings.model.CallRecordingOutputFormat.Companion.V1_FORMATS
import com.android.dialer.settings.model.CallRecordingOutputFormat.Companion.V2_FORMATS
import com.android.dialer.settings.model.CallRecordingSettings
import com.android.dialer.settings.model.StoredSettings
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class SharedPreferencesSettingsRepository @Inject constructor(
    @DefaultSharedPreferences private val sharedPreferences: SharedPreferences,
    private val contactDisplayPreferences: ContactDisplayPreferences,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SettingsRepository {

    override val settings: Flow<StoredSettings> =
        callbackFlow {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                trySend(Unit)
            }
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
            trySend(Unit)
            awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
        }.conflate()
            .map { readSettings() }
            .flowOn(ioDispatcher)

    override fun setSortOrder(sortOrder: SortOrder) {
        contactDisplayPreferences.sortOrder = sortOrder
    }

    override fun setDisplayOrder(displayOrder: DisplayOrder) {
        contactDisplayPreferences.displayOrder = displayOrder
    }

    override fun setCallRecordingV2Enabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_USE_V2, enabled) }
    }

    override fun setCallRecordingOutputFormat(
        version: CallRecorderVersion,
        outputFormat: CallRecordingOutputFormat,
    ) {
        val key = when (version) {
            CallRecorderVersion.V1 -> KEY_OUTPUT_FORMAT_V1
            CallRecorderVersion.V2 -> KEY_OUTPUT_FORMAT_V2
        }
        sharedPreferences.edit { putString(key, outputFormat.value) }
    }

    override fun setCallRecordingAudioSource(audioSource: CallRecordingAudioSource) {
        sharedPreferences.edit { putString(KEY_AUDIO_SOURCE, audioSource.value) }
    }

    private fun readSettings(): StoredSettings = StoredSettings(
        sortOrder = contactDisplayPreferences.sortOrder,
        displayOrder = contactDisplayPreferences.displayOrder,
        callRecording = CallRecordingSettings(
            isV2Enabled = sharedPreferences.getBoolean(KEY_USE_V2, false),
            outputFormatV1 = readOutputFormat(KEY_OUTPUT_FORMAT_V1, V1_FORMATS),
            outputFormatV2 = readOutputFormat(KEY_OUTPUT_FORMAT_V2, V2_FORMATS),
            audioSource = readAudioSource(),
        ),
    )

    private fun readAudioSource(): CallRecordingAudioSource =
        CallRecordingAudioSource.fromValue(sharedPreferences.getString(KEY_AUDIO_SOURCE, null))
            ?: CallRecordingAudioSource.DEFAULT

    private fun readOutputFormat(
        key: String,
        supported: List<CallRecordingOutputFormat>,
    ): CallRecordingOutputFormat =
        CallRecordingOutputFormat.fromValue(sharedPreferences.getString(key, null))
            ?.takeIf { it in supported }
            ?: CallRecordingOutputFormat.DEFAULT

    companion object {
        const val KEY_USE_V2 = "call_recording_use_v2"
        const val KEY_OUTPUT_FORMAT_V1 = "call_recording_output_format"
        const val KEY_OUTPUT_FORMAT_V2 = "call_recording_output_format_v2"
        const val KEY_AUDIO_SOURCE = "call_recording_audio_source"
    }
}
