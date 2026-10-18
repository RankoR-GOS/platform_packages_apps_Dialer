package com.android.dialer.settings

import app.cash.turbine.test
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.DisplayOrder
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.SortOrder
import com.android.dialer.settings.model.CallAccountsEntry
import com.android.dialer.settings.model.CallRecordingAudioSource
import com.android.dialer.settings.model.CallRecordingOutputFormat
import com.android.dialer.settings.model.CallRecordingSettings
import com.android.dialer.settings.model.SettingsAction as Action
import com.android.dialer.settings.model.SettingsDialog
import com.android.dialer.settings.model.SettingsEffect as Effect
import com.android.dialer.settings.model.SettingsUiState
import com.android.dialer.settings.model.StoredSettings
import com.android.dialer.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiState_beforeCollection_isLoading() {
        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun uiState_mapsStoredSettings() = runTest {
        val repository = FakeSettingsRepository(
            StoredSettings(
                sortOrder = SortOrder.BY_ALTERNATIVE,
                displayOrder = DisplayOrder.ALTERNATIVE,
                callRecording = CallRecordingSettings(
                    outputFormatV1 = CallRecordingOutputFormat.AMR_WB,
                    audioSource = CallRecordingAudioSource.MIC,
                ),
            ),
        )
        val viewModel = createViewModel(repository = repository)

        val state = subscribe(viewModel)

        assertFalse(state.isLoading)
        assertEquals(SortOrder.BY_ALTERNATIVE, state.displayOptions?.sortOrder)
        assertEquals(DisplayOrder.ALTERNATIVE, state.displayOptions?.displayOrder)
        assertFalse(state.callRecording.isV2Enabled)
        assertEquals(CallRecordingOutputFormat.AMR_WB, state.callRecording.outputFormat)
        assertEquals(
            CallRecordingOutputFormat.V1_FORMATS,
            state.callRecording.availableOutputFormats
        )
        assertEquals(CallRecordingAudioSource.MIC, state.callRecording.audioSource)
        assertEquals("23.0", state.versionName)
    }

    @Test
    fun uiState_v2Enabled_showsV2FormatAndAllFormats() = runTest {
        val repository = FakeSettingsRepository(
            StoredSettings(
                callRecording = CallRecordingSettings(
                    isV2Enabled = true,
                    outputFormatV1 = CallRecordingOutputFormat.AMR_WB,
                    outputFormatV2 = CallRecordingOutputFormat.WAV,
                ),
            ),
        )
        val viewModel = createViewModel(repository = repository)

        val state = subscribe(viewModel)

        assertEquals(CallRecordingOutputFormat.WAV, state.callRecording.outputFormat)
        assertEquals(
            CallRecordingOutputFormat.V2_FORMATS,
            state.callRecording.availableOutputFormats
        )
    }

    @Test
    fun uiState_nameOrderNotChangeable_hidesDisplayOptions() = runTest {
        val viewModel =
            createViewModel(environment = FakeSettingsEnvironment(isNameOrderChangeable = false))

        assertNull(subscribe(viewModel).displayOptions)
    }

    @Test
    fun uiState_primaryUserWithOneSlot_offersCallSettings() = runTest {
        val viewModel =
            createViewModel(
                environment = FakeSettingsEnvironment(isPrimaryUser = true, phoneCount = 1)
            )

        assertEquals(CallAccountsEntry.CALL_SETTINGS, subscribe(viewModel).callAccountsEntry)
    }

    @Test
    fun uiState_primaryUserWithTwoSlots_offersCallingAccounts() = runTest {
        val viewModel =
            createViewModel(
                environment = FakeSettingsEnvironment(isPrimaryUser = true, phoneCount = 2)
            )

        assertEquals(CallAccountsEntry.CALLING_ACCOUNTS, subscribe(viewModel).callAccountsEntry)
    }

    @Test
    fun uiState_secondaryUser_offersCallingAccountsAndHidesAccessibility() = runTest {
        val viewModel =
            createViewModel(
                environment = FakeSettingsEnvironment(isPrimaryUser = false, phoneCount = 1)
            )

        val state = subscribe(viewModel)

        assertEquals(CallAccountsEntry.CALLING_ACCOUNTS, state.callAccountsEntry)
        assertFalse(state.isAccessibilityVisible)
    }

    @Test
    fun uiState_accessibilityUnsupported_hidesAccessibility() = runTest {
        val viewModel =
            createViewModel(
                environment = FakeSettingsEnvironment(isCallAccessibilitySupported = false)
            )

        assertFalse(subscribe(viewModel).isAccessibilityVisible)
    }

    @Test
    fun uiState_primaryUserWithAccessibilitySupport_showsAccessibility() = runTest {
        val viewModel = createViewModel()

        assertTrue(subscribe(viewModel).isAccessibilityVisible)
    }

    @Test
    fun clickActions_openMatchingDialog() = runTest {
        val viewModel = createViewModel()
        subscribe(viewModel)

        mapOf(
            Action.SortOrderClicked to SettingsDialog.SORT_ORDER,
            Action.DisplayOrderClicked to SettingsDialog.DISPLAY_ORDER,
            Action.OutputFormatClicked to SettingsDialog.OUTPUT_FORMAT,
            Action.AudioSourceClicked to SettingsDialog.AUDIO_SOURCE,
        ).forEach { (action, dialog) ->
            viewModel.onAction(action)
            assertEquals(dialog, viewModel.uiState.value.openDialog)
        }
    }

    @Test
    fun dialogDismissed_closesDialog() = runTest {
        val viewModel = createViewModel()
        subscribe(viewModel)
        viewModel.onAction(Action.AudioSourceClicked)

        viewModel.onAction(Action.DialogDismissed)

        assertNull(viewModel.uiState.value.openDialog)
    }

    @Test
    fun nameOrderDialog_whenNotChangeable_isNotShown() = runTest {
        val viewModel =
            createViewModel(environment = FakeSettingsEnvironment(isNameOrderChangeable = false))
        subscribe(viewModel)

        viewModel.onAction(Action.SortOrderClicked)

        assertNull(viewModel.uiState.value.openDialog)
    }

    @Test
    fun sortOrderSelected_persistsAndClosesDialog() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = createViewModel(repository = repository)
        subscribe(viewModel)
        viewModel.onAction(Action.SortOrderClicked)

        viewModel.onAction(Action.SortOrderSelected(SortOrder.BY_ALTERNATIVE))

        assertEquals(SortOrder.BY_ALTERNATIVE, repository.settings.value.sortOrder)
        assertEquals(SortOrder.BY_ALTERNATIVE, viewModel.uiState.value.displayOptions?.sortOrder)
        assertNull(viewModel.uiState.value.openDialog)
    }

    @Test
    fun displayOrderSelected_persistsAndClosesDialog() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = createViewModel(repository = repository)
        subscribe(viewModel)
        viewModel.onAction(Action.DisplayOrderClicked)

        viewModel.onAction(Action.DisplayOrderSelected(DisplayOrder.ALTERNATIVE))

        assertEquals(DisplayOrder.ALTERNATIVE, repository.settings.value.displayOrder)
        assertNull(viewModel.uiState.value.openDialog)
    }

    @Test
    fun callRecordingV2Changed_persistsAndSwitchesFormatList() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = createViewModel(repository = repository)
        subscribe(viewModel)

        viewModel.onAction(Action.CallRecordingV2Changed(enabled = true))

        assertTrue(repository.settings.value.callRecording.isV2Enabled)
        assertEquals(
            CallRecordingOutputFormat.V2_FORMATS,
            viewModel.uiState.value.callRecording.availableOutputFormats,
        )
    }

    @Test
    fun outputFormatSelected_withV1_persistsV1FormatOnly() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = createViewModel(repository = repository)
        subscribe(viewModel)
        viewModel.onAction(Action.OutputFormatClicked)

        viewModel.onAction(Action.OutputFormatSelected(CallRecordingOutputFormat.AMR_WB))

        val callRecording = repository.settings.value.callRecording
        assertEquals(CallRecordingOutputFormat.AMR_WB, callRecording.outputFormatV1)
        assertEquals(CallRecordingOutputFormat.DEFAULT, callRecording.outputFormatV2)
        assertNull(viewModel.uiState.value.openDialog)
    }

    @Test
    fun outputFormatSelected_withV2_persistsV2FormatOnly() = runTest {
        val repository = FakeSettingsRepository(
            StoredSettings(callRecording = CallRecordingSettings(isV2Enabled = true)),
        )
        val viewModel = createViewModel(repository = repository)
        subscribe(viewModel)

        viewModel.onAction(Action.OutputFormatSelected(CallRecordingOutputFormat.WAV))

        val callRecording = repository.settings.value.callRecording
        assertEquals(CallRecordingOutputFormat.WAV, callRecording.outputFormatV2)
        assertEquals(CallRecordingOutputFormat.DEFAULT, callRecording.outputFormatV1)
    }

    @Test
    fun outputFormatSelected_wavWithV1_isIgnored() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = createViewModel(repository = repository)
        subscribe(viewModel)
        viewModel.onAction(Action.OutputFormatClicked)

        viewModel.onAction(Action.OutputFormatSelected(CallRecordingOutputFormat.WAV))

        assertEquals(
            CallRecordingOutputFormat.DEFAULT,
            repository.settings.value.callRecording.outputFormatV1
        )
        assertNull(viewModel.uiState.value.openDialog)
    }

    @Test
    fun audioSourceSelected_persistsAndClosesDialog() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = createViewModel(repository = repository)
        subscribe(viewModel)
        viewModel.onAction(Action.AudioSourceClicked)

        viewModel.onAction(Action.AudioSourceSelected(CallRecordingAudioSource.MIC))

        assertEquals(
            CallRecordingAudioSource.MIC,
            repository.settings.value.callRecording.audioSource
        )
        assertNull(viewModel.uiState.value.openDialog)
    }

    @Test
    fun soundAndVibrationClicked_withoutWritePermission_requestsWarning() = runTest {
        val viewModel =
            createViewModel(environment = FakeSettingsEnvironment(isSystemSettingsWritable = false))

        viewModel.effects.test {
            viewModel.onAction(Action.SoundAndVibrationClicked)

            assertEquals(
                Effect.OpenSoundSettings(showCannotWriteSettingsWarning = true),
                awaitItem()
            )
        }
    }

    @Test
    fun soundAndVibrationClicked_withWritePermission_skipsWarning() = runTest {
        val viewModel =
            createViewModel(environment = FakeSettingsEnvironment(isSystemSettingsWritable = true))

        viewModel.effects.test {
            viewModel.onAction(Action.SoundAndVibrationClicked)

            assertEquals(
                Effect.OpenSoundSettings(showCannotWriteSettingsWarning = false),
                awaitItem()
            )
        }
    }

    @Test
    fun callAccountsClicked_followsCallAccountsEntry() = runTest {
        val singleSim = createViewModel(environment = FakeSettingsEnvironment(phoneCount = 1))
        val multiSim = createViewModel(environment = FakeSettingsEnvironment(phoneCount = 2))

        singleSim.effects.test {
            singleSim.onAction(Action.CallAccountsClicked)
            assertEquals(Effect.OpenCallSettings, awaitItem())
        }
        multiSim.effects.test {
            multiSim.onAction(Action.CallAccountsClicked)
            assertEquals(Effect.OpenCallingAccounts, awaitItem())
        }
    }

    @Test
    fun navigationActions_emitMatchingEffects() = runTest {
        val viewModel = createViewModel()

        viewModel.effects.test {
            mapOf(
                Action.QuickResponsesClicked to Effect.OpenQuickResponses,
                Action.AccessibilityClicked to Effect.OpenCallAccessibility,
                Action.LicensesClicked to Effect.OpenLicenses,
                Action.PrivacyPolicyClicked to Effect.OpenPrivacyPolicy,
                Action.TermsOfServiceClicked to Effect.OpenTermsOfService,
            ).forEach { (action, effect) ->
                viewModel.onAction(action)
                assertEquals(effect, awaitItem())
            }
        }
    }

    private fun createViewModel(
        repository: FakeSettingsRepository = FakeSettingsRepository(),
        environment: FakeSettingsEnvironment = FakeSettingsEnvironment(),
    ): SettingsViewModel = SettingsViewModel(repository = repository, environment = environment)

    private fun TestScope.subscribe(viewModel: SettingsViewModel): SettingsUiState {
        backgroundScope.launch(mainDispatcherRule.testDispatcher) { viewModel.uiState.collect() }
        return viewModel.uiState.value
    }
}
