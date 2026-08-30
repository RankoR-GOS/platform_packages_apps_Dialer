package com.android.dialer.data.contacts.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * [titles] holds the section labels ("A", "B", "#")
 * [counts] how many rows fall under each.
 */
internal data class ContactsIndex(
    val titles: ImmutableList<String> = persistentListOf(),
    val counts: ImmutableList<Int> = persistentListOf(),
) {
    companion object {
        val EMPTY = ContactsIndex()
    }
}
