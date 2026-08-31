package com.android.dialer.ui.contacts.screen.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal sealed interface ContactsUiState {

    data object Loading : ContactsUiState

    data object PermissionRequired : ContactsUiState

    data object Empty : ContactsUiState

    data class Loaded(
        val rows: ImmutableList<ContactRowUiModel>,
        val showsAddContactRow: Boolean,
    ) : ContactsUiState
}

@Immutable
internal data class ContactRowUiModel(
    val id: Long,
    val lookupKey: String,
    val displayName: String,
    val photoId: Long,
    val photoUri: String?,
    val sectionLabel: String,
    val isSectionStart: Boolean,
)
