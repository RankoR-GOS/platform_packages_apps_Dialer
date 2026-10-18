package com.android.dialer.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.dialer.settings.data.SettingsEnvironment
import com.android.dialer.settings.data.SettingsRepository
import com.android.dialer.settings.model.CallAccountsEntry
import com.android.dialer.settings.model.CallRecorderVersion
import com.android.dialer.settings.model.CallRecordingOutputFormat
import com.android.dialer.settings.model.SettingsAction as Action
import com.android.dialer.settings.model.SettingsDialog
import com.android.dialer.settings.model.SettingsEffect as Effect
import com.android.dialer.settings.model.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val environment: SettingsEnvironment,
) : ViewModel() {

    private val staticState = SettingsStaticState(
        isNameOrderChangeable = environment.isNameOrderChangeable,
        // "Call Settings" (full settings) is shown if the current user is primary user and there
        // is only one SIM. Otherwise, "Calling accounts" is shown.
        callAccountsEntry = when {
            environment.isPrimaryUser && environment.phoneCount <= 1 -> {
                CallAccountsEntry.CALL_SETTINGS
            }
            else -> CallAccountsEntry.CALLING_ACCOUNTS
        },
        isAccessibilityVisible =
            environment.isPrimaryUser && environment.isCallAccessibilitySupported,
        versionName = environment.versionName,
    )

    private val openDialog = MutableStateFlow<SettingsDialog?>(null)

    val uiState: StateFlow<SettingsUiState> =
        combine(repository.settings, openDialog) { stored, dialog ->
            mapSettingsUiState(stored = stored, static = staticState, openDialog = dialog)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SettingsUiState(),
        )

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effects: Flow<Effect> = _effects.receiveAsFlow()

    fun onAction(action: Action) {
        when (action) {
            is Action.DialogAction -> onDialogAction(action)
            is Action.ValueAction -> onValueAction(action)
            is Action.NavigationAction -> _effects.trySend(effectFor(action))
        }
    }

    private fun onDialogAction(action: Action.DialogAction) {
        openDialog.update {
            when (action) {
                Action.SortOrderClicked -> SettingsDialog.SORT_ORDER
                Action.DisplayOrderClicked -> SettingsDialog.DISPLAY_ORDER
                Action.OutputFormatClicked -> SettingsDialog.OUTPUT_FORMAT
                Action.AudioSourceClicked -> SettingsDialog.AUDIO_SOURCE
                Action.DialogDismissed -> null
            }
        }
    }

    private fun onValueAction(action: Action.ValueAction) {
        when (action) {
            is Action.SortOrderSelected -> repository.setSortOrder(action.sortOrder)
            is Action.DisplayOrderSelected -> repository.setDisplayOrder(action.displayOrder)
            is Action.OutputFormatSelected -> onOutputFormatSelected(action.outputFormat)
            is Action.AudioSourceSelected -> {
                repository.setCallRecordingAudioSource(action.audioSource)
            }
            is Action.CallRecordingV2Changed -> {
                repository.setCallRecordingV2Enabled(action.enabled)
            }
        }
        openDialog.update { null }
    }

    private fun onOutputFormatSelected(outputFormat: CallRecordingOutputFormat) {
        val callRecording = uiState.value.callRecording
        if (outputFormat !in callRecording.availableOutputFormats) return
        val version = when {
            callRecording.isV2Enabled -> CallRecorderVersion.V2
            else -> CallRecorderVersion.V1
        }
        repository.setCallRecordingOutputFormat(version = version, outputFormat = outputFormat)
    }

    private fun effectFor(action: Action.NavigationAction): Effect = when (action) {
        Action.SoundAndVibrationClicked -> Effect.OpenSoundSettings(
            showCannotWriteSettingsWarning = !environment.canWriteSystemSettings(),
        )
        Action.QuickResponsesClicked -> Effect.OpenQuickResponses
        Action.CallAccountsClicked -> when (staticState.callAccountsEntry) {
            CallAccountsEntry.CALL_SETTINGS -> Effect.OpenCallSettings
            CallAccountsEntry.CALLING_ACCOUNTS -> Effect.OpenCallingAccounts
        }
        Action.AccessibilityClicked -> Effect.OpenCallAccessibility
        Action.LicensesClicked -> Effect.OpenLicenses
        Action.PrivacyPolicyClicked -> Effect.OpenPrivacyPolicy
        Action.TermsOfServiceClicked -> Effect.OpenTermsOfService
    }
}
