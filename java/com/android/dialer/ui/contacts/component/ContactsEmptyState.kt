package com.android.dialer.ui.contacts.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.dialer.ui.contacts.common.CONTACTS_EMPTY_STATE_ACTION_TEST_TAG
import com.android.dialer.ui.contacts.common.CONTACTS_EMPTY_STATE_TEST_TAG
import com.android.dialer.ui.core.DialerPreviewTheme

private val EmptyStateIconSize = 96.dp
private val EmptyStateSpacing = 16.dp
private val EmptyStateHorizontalPadding = 32.dp

@Composable
internal fun ContactsEmptyState(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Contacts,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = EmptyStateHorizontalPadding)
            .testTag(CONTACTS_EMPTY_STATE_TEST_TAG),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(EmptyStateIconSize),
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = EmptyStateSpacing),
        )

        TextButton(
            onClick = onAction,
            modifier = Modifier
                .padding(top = EmptyStateSpacing)
                .testTag(CONTACTS_EMPTY_STATE_ACTION_TEST_TAG),
        ) {
            Text(text = actionLabel)
        }
    }
}

@PreviewLightDark
@Composable
private fun ContactsEmptyStatePreview() {
    DialerPreviewTheme {
        ContactsEmptyState(
            message = "You don't have any contacts yet",
            actionLabel = "Create new contact",
            onAction = {},
        )
    }
}
