package com.android.dialer.ui.contacts.screen.model

import androidx.compose.ui.geometry.Rect

internal sealed interface ContactsAction {

    data class ContactClicked(
        val contactId: Long,
        val anchorBounds: Rect,
    ) : ContactsAction

    data object AddContactClicked : ContactsAction

    data object GrantPermissionClicked : ContactsAction

    data class FilterChanged(
        val filter: String,
    ) : ContactsAction
}
