package com.android.dialer.keypad.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Voicemail
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.dialer.keypad.model.KeypadAction
import com.android.dialer.keypad.model.KeypadError
import com.android.dialer.theme.compose.DialerPreviewTheme

@Composable
internal fun KeypadErrorDialog(
    error: KeypadError,
    strings: KeypadStrings,
    onAction: (KeypadAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = errorDialogContent(error = error, strings = strings)
    val text: (@Composable () -> Unit)? = content.message?.let { message ->
        @Composable { Text(text = message) }
    }
    val dismissButton: (@Composable () -> Unit)? = content.dismissLabel?.let { label ->
        @Composable {
            TextButton(
                onClick = { onAction(KeypadAction.ErrorDismissed) },
                modifier = Modifier.testTag(KEYPAD_ERROR_DIALOG_DISMISS_TEST_TAG),
            ) {
                Text(text = label)
            }
        }
    }
    AlertDialog(
        onDismissRequest = { onAction(KeypadAction.ErrorDismissed) },
        confirmButton = {
            TextButton(
                onClick = { onAction(content.confirmAction) },
                modifier = Modifier.testTag(KEYPAD_ERROR_DIALOG_CONFIRM_TEST_TAG),
            ) {
                Text(text = content.confirmLabel)
            }
        },
        modifier = modifier.testTag(KEYPAD_ERROR_DIALOG_TEST_TAG),
        dismissButton = dismissButton,
        icon = { Icon(imageVector = content.icon, contentDescription = null) },
        title = { Text(text = content.title) },
        text = text,
    )
}

/** What one error shows, and the action its confirm button sends. */
private data class ErrorDialogContent(
    val icon: ImageVector,
    val title: String,
    val message: String?,
    val confirmLabel: String,
    val confirmAction: KeypadAction,
    val dismissLabel: String?,
)

private fun errorDialogContent(error: KeypadError, strings: KeypadStrings): ErrorDialogContent {
    return when (error) {
        KeypadError.VOICEMAIL_AIRPLANE_MODE -> ErrorDialogContent(
            icon = Icons.Rounded.AirplanemodeActive,
            title = strings.voicemailUnavailableTitle,
            message = strings.voicemailAirplaneModeError,
            confirmLabel = strings.airplaneModeSettings,
            confirmAction = KeypadAction.AirplaneModeSettingsClicked,
            dismissLabel = strings.cancel,
        )
        KeypadError.VOICEMAIL_NOT_READY -> ErrorDialogContent(
            icon = Icons.Rounded.Voicemail,
            title = strings.voicemailUnavailableTitle,
            // Still points at Menu > Settings, which the button now skips: kept because it is
            // translated, where a new message would show in English until translators catch up.
            message = strings.voicemailNotReadyError,
            confirmLabel = strings.voicemailSettings,
            confirmAction = KeypadAction.VoicemailSettingsClicked,
            dismissLabel = strings.cancel,
        )
        KeypadError.PROHIBITED_NUMBER -> ErrorDialogContent(
            icon = Icons.Rounded.Block,
            title = strings.prohibitedNumberError,
            message = null,
            confirmLabel = strings.ok,
            confirmAction = KeypadAction.ErrorDismissed,
            dismissLabel = null,
        )
    }
}

@PreviewLightDark
@Composable
private fun AirplaneModeErrorPreview() {
    ErrorDialogPreview(error = KeypadError.VOICEMAIL_AIRPLANE_MODE)
}

@PreviewLightDark
@Composable
private fun VoicemailNotReadyErrorPreview() {
    ErrorDialogPreview(error = KeypadError.VOICEMAIL_NOT_READY)
}

@PreviewLightDark
@Composable
private fun ProhibitedNumberErrorPreview() {
    ErrorDialogPreview(error = KeypadError.PROHIBITED_NUMBER)
}

@Composable
private fun ErrorDialogPreview(error: KeypadError) {
    DialerPreviewTheme {
        KeypadErrorDialog(
            error = error,
            strings = previewKeypadStrings(),
            onAction = {},
        )
    }
}
