package com.android.dialer.ui.contacts.screen.mapper

import com.android.dialer.data.contacts.model.ContactsIndex
import com.android.dialer.data.contacts.model.ContactsSnapshot
import com.android.dialer.ui.contacts.screen.model.ContactRowUiModel
import com.android.dialer.ui.contacts.screen.model.ContactsUiState
import javax.inject.Inject
import kotlinx.collections.immutable.toPersistentList

internal interface ContactsUiStateMapper {
    fun map(snapshot: ContactsSnapshot, showsAddContactRow: Boolean): ContactsUiState
}

internal class ContactsUiStateMapperImpl @Inject constructor() : ContactsUiStateMapper {

    override fun map(
        snapshot: ContactsSnapshot,
        showsAddContactRow: Boolean,
    ): ContactsUiState {
        if (snapshot.contacts.isEmpty()) {
            return ContactsUiState.Empty
        }

        val labels = sectionLabels(rowCount = snapshot.contacts.size, index = snapshot.index)

        val rows = snapshot.contacts.mapIndexed { position, contact ->
            val label = labels[position]

            ContactRowUiModel(
                id = contact.id,
                lookupKey = contact.lookupKey,
                displayName = contact.displayName,
                photoId = contact.photoId,
                photoUri = contact.photoUri,
                sectionLabel = label,
                isSectionStart = label.isNotEmpty() &&
                    (position == 0 || labels[position - 1] != label),
            )
        }

        return ContactsUiState.Loaded(
            rows = rows.toPersistentList(),
            showsAddContactRow = showsAddContactRow,
        )
    }

    private fun sectionLabels(rowCount: Int, index: ContactsIndex): List<String> {
        val labels = MutableList(rowCount) { "" }
        var position = 0

        for (section in index.titles.indices) {
            if (position >= rowCount) {
                break
            }

            val count = index.counts.getOrElse(section) { 0 }.coerceAtLeast(minimumValue = 0)
            val sectionEnd = (position + count).coerceAtMost(maximumValue = rowCount)
            val title = index.titles[section]

            while (position < sectionEnd) {
                labels[position] = title
                position++
            }
        }

        return labels
    }
}
