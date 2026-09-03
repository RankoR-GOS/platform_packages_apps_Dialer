package com.android.dialer.ui.contacts.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.dialer.R
import com.android.dialer.ui.contacts.common.CONTACTS_ADD_CONTACT_ROW_TEST_TAG
import com.android.dialer.ui.contacts.common.ContactAvatarSize
import com.android.dialer.ui.contacts.common.ContactRowContentSpacing
import com.android.dialer.ui.contacts.common.ContactRowHeight
import com.android.dialer.ui.contacts.common.ContactRowHorizontalPadding
import com.android.dialer.ui.contacts.common.ContactSectionGutterWidth
import com.android.dialer.ui.core.DialerPreviewTheme

@Composable
internal fun AddContactRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ContactRowHeight)
            .clickable(onClick = onClick)
            .testTag(CONTACTS_ADD_CONTACT_ROW_TEST_TAG),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Empty gutter, so the icon lines up with the avatars below it.
        Spacer(modifier = Modifier.width(ContactSectionGutterWidth).fillMaxHeight())

        Box(
            modifier = Modifier
                .size(ContactAvatarSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PersonAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Spacer(modifier = Modifier.width(ContactRowContentSpacing))

        Text(
            text = stringResource(id = R.string.all_contacts_empty_add_contact_action),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(end = ContactRowHorizontalPadding),
        )
    }
}

@PreviewLightDark
@Composable
private fun AddContactRowPreview() {
    DialerPreviewTheme {
        AddContactRow(onClick = {})
    }
}
