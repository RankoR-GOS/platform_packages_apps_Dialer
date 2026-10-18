package com.android.dialer.settings.model

import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.DisplayOrder
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.SortOrder

sealed interface SettingsAction {

    sealed interface DialogAction : SettingsAction

    sealed interface ValueAction : SettingsAction

    sealed interface NavigationAction : SettingsAction

    data object SortOrderClicked : DialogAction
    data object DisplayOrderClicked : DialogAction
    data object OutputFormatClicked : DialogAction
    data object AudioSourceClicked : DialogAction
    data object DialogDismissed : DialogAction

    data class SortOrderSelected(
        val sortOrder: SortOrder,
    ) : ValueAction

    data class DisplayOrderSelected(
        val displayOrder: DisplayOrder,
    ) : ValueAction

    data class CallRecordingV2Changed(
        val enabled: Boolean,
    ) : ValueAction

    data class OutputFormatSelected(
        val outputFormat: CallRecordingOutputFormat,
    ) : ValueAction

    data class AudioSourceSelected(
        val audioSource: CallRecordingAudioSource,
    ) : ValueAction

    data object SoundAndVibrationClicked : NavigationAction
    data object QuickResponsesClicked : NavigationAction
    data object CallAccountsClicked : NavigationAction
    data object AccessibilityClicked : NavigationAction
    data object LicensesClicked : NavigationAction
    data object PrivacyPolicyClicked : NavigationAction
    data object TermsOfServiceClicked : NavigationAction
}
