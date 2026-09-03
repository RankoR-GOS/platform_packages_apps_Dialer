package com.android.dialer.ui.contacts.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.dialer.ui.contacts.common.ContactRowContentSpacing
import com.android.dialer.ui.contacts.common.ContactRowHeight
import com.android.dialer.ui.contacts.common.ContactRowHorizontalPadding
import com.android.dialer.ui.contacts.common.ContactSectionGutterWidth
import com.android.dialer.ui.contacts.common.contactAvatarTestTag
import com.android.dialer.ui.contacts.common.contactRowTestTag
import com.android.dialer.ui.contacts.common.contactSectionHeaderTestTag
import com.android.dialer.ui.contacts.screen.model.ContactRowUiModel
import com.android.dialer.ui.core.DialerPreviewTheme

@Composable
internal fun ContactRow(
    row: ContactRowUiModel,
    onClick: (Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    // The quick contact card animates out of the avatar, so that is what gets measured.
    var avatarBounds by remember(row.id) { mutableStateOf(Rect.Zero) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ContactRowHeight)
            .clickable { onClick(avatarBounds) }
            .testTag(contactRowTestTag(contactId = row.id)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ContactSectionGutter(row = row)

        ContactAvatar(
            displayName = row.displayName,
            photoUri = row.photoUri,
            colorSeed = row.lookupKey.ifBlank { row.displayName },
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    avatarBounds = coordinates.boundsInWindow()
                }
                .testTag(contactAvatarTestTag(contactId = row.id)),
        )

        Spacer(modifier = Modifier.width(ContactRowContentSpacing))

        Text(
            text = row.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = ContactRowHorizontalPadding),
        )
    }
}

@Composable
private fun ContactSectionGutter(row: ContactRowUiModel) {
    Box(
        modifier = Modifier
            .width(ContactSectionGutterWidth)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        if (row.isSectionStart) {
            Text(
                text = row.sectionLabel,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag(
                    contactSectionHeaderTestTag(label = row.sectionLabel),
                ),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ContactRowPreview() {
    DialerPreviewTheme {
        Column {
            ContactRow(
                row = ContactRowUiModel(
                    id = 1L,
                    lookupKey = "lookup-1",
                    displayName = "Ada Lovelace",
                    photoId = 0L,
                    photoUri = null,
                    sectionLabel = "A",
                    isSectionStart = true,
                ),
                onClick = {},
            )
            ContactRow(
                row = ContactRowUiModel(
                    id = 2L,
                    lookupKey = "lookup-2",
                    displayName = "Alan Turing",
                    photoId = 0L,
                    photoUri = null,
                    sectionLabel = "A",
                    isSectionStart = false,
                ),
                onClick = {},
            )
        }
    }
}
