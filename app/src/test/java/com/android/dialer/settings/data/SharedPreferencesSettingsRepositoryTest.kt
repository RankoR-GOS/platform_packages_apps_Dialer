package com.android.dialer.settings.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import androidx.core.content.edit
import app.cash.turbine.test
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.DisplayOrder
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.SortOrder
import com.android.dialer.settings.data.SharedPreferencesSettingsRepository.Companion.KEY_AUDIO_SOURCE
import com.android.dialer.settings.data.SharedPreferencesSettingsRepository.Companion.KEY_OUTPUT_FORMAT_V1
import com.android.dialer.settings.data.SharedPreferencesSettingsRepository.Companion.KEY_OUTPUT_FORMAT_V2
import com.android.dialer.settings.data.SharedPreferencesSettingsRepository.Companion.KEY_USE_V2
import com.android.dialer.settings.model.CallRecorderVersion
import com.android.dialer.settings.model.CallRecordingAudioSource
import com.android.dialer.settings.model.CallRecordingOutputFormat
import com.android.dialer.settings.model.CallRecordingSettings
import com.android.dialer.settings.model.StoredSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SharedPreferencesSettingsRepositoryTest {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var contactDisplayPreferences: SharedPreferencesContactDisplayPreferences

    @Before
    fun setUp() {
        sharedPreferences = RuntimeEnvironment.getApplication()
            .getSharedPreferences("settings_repository_test", Context.MODE_PRIVATE)
        sharedPreferences.edit(commit = true) { clear() }
        contactDisplayPreferences = SharedPreferencesContactDisplayPreferences(sharedPreferences)
    }

    @Test
    fun settings_emptyPreferences_emitsDefaults() = runTest {
        val repository = createRepository()

        assertEquals(StoredSettings(), repository.settings.first())
    }

    @Test
    fun settings_readsValuesStoredByLegacyScreen() = runTest {
        sharedPreferences.edit(commit = true) {
            putBoolean(KEY_USE_V2, true)
            putString(KEY_OUTPUT_FORMAT_V1, "1")
            putString(KEY_OUTPUT_FORMAT_V2, "2")
            putString(KEY_AUDIO_SOURCE, "1")
        }
        contactDisplayPreferences.sortOrder = SortOrder.BY_ALTERNATIVE
        contactDisplayPreferences.displayOrder = DisplayOrder.ALTERNATIVE

        val settings = createRepository().settings.first()

        assertEquals(
            StoredSettings(
                sortOrder = SortOrder.BY_ALTERNATIVE,
                displayOrder = DisplayOrder.ALTERNATIVE,
                callRecording = CallRecordingSettings(
                    isV2Enabled = true,
                    outputFormatV1 = CallRecordingOutputFormat.AMR_WB,
                    outputFormatV2 = CallRecordingOutputFormat.WAV,
                    audioSource = CallRecordingAudioSource.MIC,
                ),
            ),
            settings,
        )
    }

    @Test
    fun settings_unknownOrUnsupportedValues_fallBackToDefaults() = runTest {
        sharedPreferences.edit(commit = true) {
            putString(KEY_OUTPUT_FORMAT_V1, "2")
            putString(KEY_OUTPUT_FORMAT_V2, "42")
            putString(KEY_AUDIO_SOURCE, "garbage")
        }

        val callRecording = createRepository().settings.first().callRecording

        assertEquals(CallRecordingOutputFormat.DEFAULT, callRecording.outputFormatV1)
        assertEquals(CallRecordingOutputFormat.DEFAULT, callRecording.outputFormatV2)
        assertEquals(CallRecordingAudioSource.DEFAULT, callRecording.audioSource)
    }

    @Test
    fun setters_writeLegacyKeysAndRepresentations() = runTest {
        val repository = createRepository()

        repository.setCallRecordingV2Enabled(true)
        repository.setCallRecordingOutputFormat(
            CallRecorderVersion.V1,
            CallRecordingOutputFormat.AMR_WB
        )
        repository.setCallRecordingOutputFormat(
            CallRecorderVersion.V2,
            CallRecordingOutputFormat.WAV
        )
        repository.setCallRecordingAudioSource(CallRecordingAudioSource.MIC)

        assertTrue(sharedPreferences.getBoolean(KEY_USE_V2, false))
        assertEquals("1", sharedPreferences.getString(KEY_OUTPUT_FORMAT_V1, null))
        assertEquals("2", sharedPreferences.getString(KEY_OUTPUT_FORMAT_V2, null))
        assertEquals("1", sharedPreferences.getString(KEY_AUDIO_SOURCE, null))
    }

    @Test
    fun setters_delegateNameOrderToContactDisplayPreferences() = runTest {
        val repository = createRepository()

        repository.setSortOrder(SortOrder.BY_ALTERNATIVE)
        repository.setDisplayOrder(DisplayOrder.ALTERNATIVE)

        assertEquals(SortOrder.BY_ALTERNATIVE, contactDisplayPreferences.sortOrder)
        assertEquals(DisplayOrder.ALTERNATIVE, contactDisplayPreferences.displayOrder)
    }

    @Test
    fun settings_reemitsAfterWrite() = runTest {
        val repository = createRepository()

        repository.settings.test {
            assertEquals(StoredSettings(), awaitItem())

            repository.setCallRecordingAudioSource(CallRecordingAudioSource.MIC)
            shadowOf(Looper.getMainLooper()).idle()

            assertEquals(CallRecordingAudioSource.MIC, awaitItem().callRecording.audioSource)
        }
    }

    private fun TestScope.createRepository() = SharedPreferencesSettingsRepository(
        sharedPreferences = sharedPreferences,
        contactDisplayPreferences = contactDisplayPreferences,
        ioDispatcher = UnconfinedTestDispatcher(testScheduler),
    )

    private class SharedPreferencesContactDisplayPreferences(
        private val sharedPreferences: SharedPreferences,
    ) : ContactDisplayPreferences {

        override fun getDisplayOrder(): DisplayOrder =
            DisplayOrder.valueOf(
                sharedPreferences.getString(DISPLAY_ORDER_KEY, null) ?: DisplayOrder.PRIMARY.name
            )

        override fun setDisplayOrder(displayOrder: DisplayOrder) {
            sharedPreferences.edit { putString(DISPLAY_ORDER_KEY, displayOrder.name) }
        }

        override fun getSortOrder(): SortOrder =
            SortOrder.valueOf(
                sharedPreferences.getString(SORT_ORDER_KEY, null) ?: SortOrder.BY_PRIMARY.name
            )

        override fun setSortOrder(sortOrder: SortOrder) {
            sharedPreferences.edit { putString(SORT_ORDER_KEY, sortOrder.name) }
        }

        private companion object {
            const val DISPLAY_ORDER_KEY = "display_order"
            const val SORT_ORDER_KEY = "sort_order"
        }
    }
}
