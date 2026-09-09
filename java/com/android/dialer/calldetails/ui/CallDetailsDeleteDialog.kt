package com.android.dialer.calldetails.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.dialer.R
import com.android.dialer.calldetails.CALL_DETAILS_DELETE_CANCEL_BUTTON_TAG
import com.android.dialer.calldetails.CALL_DETAILS_DELETE_CONFIRM_BUTTON_TAG
import com.android.dialer.calldetails.CALL_DETAILS_DELETE_DIALOG_TEST_TAG
import com.android.dialer.calldetails.model.CallDetailsDeleteDialogState
import com.android.dialer.theme.DialerPreviewTheme

@Composable
internal fun CallDetailsDeleteDialog(
    state: CallDetailsDeleteDialogState?,
    onConfirm: (state: CallDetailsDeleteDialogState) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.delete_call_log_title))
        },
        text = {
            Text(text = stringResource(R.string.delete_call_log_message))
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(state) },
                modifier = Modifier.testTag(CALL_DETAILS_DELETE_CONFIRM_BUTTON_TAG),
            ) {
                Text(
                    text = stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(CALL_DETAILS_DELETE_CANCEL_BUTTON_TAG),
            ) {
                Text(text = stringResource(android.R.string.cancel))
            }
        },
        modifier = modifier.testTag(CALL_DETAILS_DELETE_DIALOG_TEST_TAG),
    )
}

@PreviewLightDark
@Composable
private fun CallDetailsDeleteDialogPreview() {
    DialerPreviewTheme {
        CallDetailsDeleteDialog(
            state = CallDetailsDeleteDialogState.DeleteAll,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
