package com.android.dialer.ui.contacts.screen.model

import androidx.compose.ui.geometry.Rect
import kotlinx.collections.immutable.ImmutableList

internal sealed interface ContactsScreenEffect {

    data class ShowContactCard(
        val contactId: Long,
        val lookupKey: String,
        val anchorBounds: Rect,
    ) : ContactsScreenEffect

    data object LaunchAddContact : ContactsScreenEffect

    data class RequestPermissions(
        val permissions: ImmutableList<String>,
    ) : ContactsScreenEffect
}
