package com.android.dialer.data.contacts.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class ContactsSnapshot(
    val contacts: ImmutableList<Contact> = persistentListOf(),
    val index: ContactsIndex = ContactsIndex.EMPTY,
) {
    companion object {
        val EMPTY = ContactsSnapshot()
    }
}
