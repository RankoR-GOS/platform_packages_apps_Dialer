package com.android.dialer.settings.ui

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ContactPhone
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.dialer.R
import com.android.dialer.settings.model.CallAccountsEntry
import com.android.dialer.settings.model.CallRecordingUiState
import com.android.dialer.settings.model.DisplayOptionsUiState
import com.android.dialer.settings.model.SettingsAction as Action
import com.android.dialer.settings.model.SettingsUiState

internal fun LazyListScope.displayOptionsSection(
    displayOptions: DisplayOptionsUiState,
    onAction: (Action) -> Unit,
) = settingsSection(key = "display_options", titleRes = R.string.display_options_title) {
    row(key = "sort_order") { shapes ->
        SettingsItem(
            title = stringResource(R.string.display_options_sort_list_by),
            summary = sortOrderLabel(displayOptions.sortOrder),
            icon = Icons.Outlined.SortByAlpha,
            onClick = { onAction(Action.SortOrderClicked) },
            shapes = shapes,
            modifier = Modifier.testTag(SETTINGS_SORT_ORDER_ITEM_TEST_TAG),
        )
    }
    row(key = "display_order") { shapes ->
        SettingsItem(
            title = stringResource(R.string.display_options_view_names_as),
            summary = displayOrderLabel(displayOptions.displayOrder),
            icon = Icons.Outlined.Badge,
            onClick = { onAction(Action.DisplayOrderClicked) },
            shapes = shapes,
            modifier = Modifier.testTag(SETTINGS_DISPLAY_ORDER_ITEM_TEST_TAG),
        )
    }
}

internal fun LazyListScope.generalSection(
    uiState: SettingsUiState,
    onAction: (Action) -> Unit,
) = settingsSection(
    key = "general",
    titleRes = null,
) {
    row(key = "sound_and_vibration") { shapes ->
        SettingsItem(
            title = stringResource(R.string.sounds_and_vibration_title),
            icon = Icons.AutoMirrored.Outlined.VolumeUp,
            onClick = { onAction(Action.SoundAndVibrationClicked) },
            shapes = shapes,
            modifier = Modifier.testTag(SETTINGS_SOUND_AND_VIBRATION_ITEM_TEST_TAG),
        )
    }
    row(key = "quick_responses") { shapes ->
        SettingsItem(
            title = stringResource(R.string.respond_via_sms_setting_title),
            icon = Icons.AutoMirrored.Outlined.Message,
            onClick = { onAction(Action.QuickResponsesClicked) },
            shapes = shapes,
            modifier = Modifier.testTag(SETTINGS_QUICK_RESPONSES_ITEM_TEST_TAG),
        )
    }
    row(key = "call_accounts") { shapes ->
        SettingsItem(
            title = stringResource(
                when (uiState.callAccountsEntry) {
                    CallAccountsEntry.CALL_SETTINGS -> R.string.call_settings_label
                    CallAccountsEntry.CALLING_ACCOUNTS -> R.string.phone_account_settings_label
                },
            ),
            icon = Icons.Outlined.ContactPhone,
            onClick = { onAction(Action.CallAccountsClicked) },
            shapes = shapes,
            modifier = Modifier.testTag(SETTINGS_CALL_ACCOUNTS_ITEM_TEST_TAG),
        )
    }
    if (uiState.isAccessibilityVisible) {
        row(key = "accessibility") { shapes ->
            SettingsItem(
                title = stringResource(R.string.accessibility_settings_title),
                icon = Icons.Outlined.AccessibilityNew,
                onClick = { onAction(Action.AccessibilityClicked) },
                shapes = shapes,
                modifier = Modifier.testTag(SETTINGS_ACCESSIBILITY_ITEM_TEST_TAG),
            )
        }
    }
}

internal fun LazyListScope.callRecordingSection(
    callRecording: CallRecordingUiState,
    onAction: (Action) -> Unit,
) = settingsSection(key = "call_recording", titleRes = R.string.call_recording_header_title) {
    row(key = "call_recording_v2") { shapes ->
        SettingsSwitchItem(
            title = stringResource(R.string.call_recording_use_call_recording_v2_title),
            summary = stringResource(R.string.call_recording_use_call_recording_v2_summary),
            icon = Icons.Outlined.Science,
            checked = callRecording.isV2Enabled,
            onCheckedChange = { onAction(Action.CallRecordingV2Changed(it)) },
            shapes = shapes,
            modifier = Modifier.testTag(SETTINGS_CALL_RECORDING_V2_ITEM_TEST_TAG),
        )
    }
    row(key = "output_format") { shapes ->
        SettingsItem(
            title = stringResource(outputFormatTitleRes(callRecording.isV2Enabled)),
            summary = outputFormatLabel(callRecording.outputFormat),
            icon = Icons.Outlined.AudioFile,
            onClick = { onAction(Action.OutputFormatClicked) },
            shapes = shapes,
            modifier = Modifier.testTag(SETTINGS_OUTPUT_FORMAT_ITEM_TEST_TAG),
        )
    }
    row(key = "audio_source") { shapes ->
        SettingsItem(
            title = stringResource(R.string.call_recording_audio_source_title),
            summary = audioSourceLabel(callRecording.audioSource),
            icon = Icons.Outlined.Mic,
            onClick = { onAction(Action.AudioSourceClicked) },
            shapes = shapes,
            modifier = Modifier.testTag(SETTINGS_AUDIO_SOURCE_ITEM_TEST_TAG),
        )
    }
    row(key = "recording_notice") { shapes ->
        SettingsItem(
            title = stringResource(R.string.call_recording_warning_settings_title),
            summary = stringResource(R.string.recording_warning_text),
            icon = Icons.Outlined.WarningAmber,
            onClick = null,
            shapes = shapes,
            modifier = Modifier.testTag(SETTINGS_RECORDING_NOTICE_ITEM_TEST_TAG),
        )
    }
}

internal fun LazyListScope.aboutSection(
    versionName: String,
    onAction: (Action) -> Unit,
) = settingsSection(key = "about", titleRes = R.string.about_phone_label) {
    row(key = "build_version") { shapes ->
        SettingsItem(
            title = stringResource(R.string.build_version_label),
            summary = versionName.ifEmpty { null },
            icon = Icons.Outlined.Info,
            onClick = null,
            shapes = shapes,
            modifier = Modifier.testTag(SETTINGS_BUILD_VERSION_ITEM_TEST_TAG),
        )
    }
    row(key = "licenses") { shapes ->
        SettingsItem(
            title = stringResource(R.string.licenseActivityLabel),
            summary = stringResource(R.string.open_source_license_detail_label),
            icon = Icons.Outlined.Description,
            onClick = { onAction(Action.LicensesClicked) },
            shapes = shapes,
            modifier = Modifier.testTag(SETTINGS_LICENSES_ITEM_TEST_TAG),
        )
    }
    row(key = "privacy_policy") { shapes ->
        SettingsItem(
            title = stringResource(R.string.privacy_policy_label),
            icon = Icons.Outlined.PrivacyTip,
            onClick = { onAction(Action.PrivacyPolicyClicked) },
            shapes = shapes,
            modifier = Modifier.testTag(SETTINGS_PRIVACY_POLICY_ITEM_TEST_TAG),
        )
    }
    row(key = "terms_of_service") { shapes ->
        SettingsItem(
            title = stringResource(R.string.terms_of_service_label),
            icon = Icons.Outlined.Gavel,
            onClick = { onAction(Action.TermsOfServiceClicked) },
            shapes = shapes,
            modifier = Modifier.testTag(SETTINGS_TERMS_OF_SERVICE_ITEM_TEST_TAG),
        )
    }
}

private fun outputFormatTitleRes(isV2Enabled: Boolean): Int = when {
    isV2Enabled -> R.string.call_recording_output_format_title_v2
    else -> R.string.call_recording_output_format_title
}
