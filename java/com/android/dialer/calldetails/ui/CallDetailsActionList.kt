package com.android.dialer.calldetails.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.dialer.R
import com.android.dialer.calldetails.CALL_DETAILS_COPY_ACTION_TAG
import com.android.dialer.calldetails.CALL_DETAILS_DELETE_MENU_ITEM_TAG
import com.android.dialer.calldetails.CALL_DETAILS_EDIT_NUMBER_ACTION_TAG
import com.android.dialer.theme.DialerPreviewTheme

@Composable
internal fun CallDetailsActionList(
    onCopyNumberClick: () -> Unit,
    onEditNumberClick: () -> Unit,
    onDeleteCallLogClick: () -> Unit,
    modifier: Modifier = Modifier,
    canCopy: Boolean = true,
    canEdit: Boolean = true,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (canCopy) {
                ActionListItem(
                    title = stringResource(R.string.call_details_copy_number),
                    icon = Icons.Filled.ContentCopy,
                    testTag = CALL_DETAILS_COPY_ACTION_TAG,
                    onClick = onCopyNumberClick,
                )
            }

            if (canEdit) {
                ActionListItem(
                    title = stringResource(R.string.call_details_edit_number),
                    icon = Icons.Filled.Edit,
                    testTag = CALL_DETAILS_EDIT_NUMBER_ACTION_TAG,
                    onClick = onEditNumberClick,
                )
            }

            ActionListItem(
                title = stringResource(R.string.delete_from_call_log),
                icon = Icons.Filled.Delete,
                testTag = CALL_DETAILS_DELETE_MENU_ITEM_TAG,
                onClick = onDeleteCallLogClick,
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun ActionListItem(
    title: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = tint,
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp),
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 8.dp),
    )
}

@PreviewLightDark
@Composable
private fun CallDetailsActionListPreview() {
    DialerPreviewTheme {
        CallDetailsActionList(
            onCopyNumberClick = {},
            onEditNumberClick = {},
            onDeleteCallLogClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
