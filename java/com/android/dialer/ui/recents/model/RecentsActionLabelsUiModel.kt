package com.android.dialer.ui.recents.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class RecentsActionLabelsUiModel(
    val call: String = "",
    val videoCall: String = "",
    val message: String = "",
    val createContact: String = "",
    val addContact: String = "",
    val copyNumber: String = "",
    val editNumberBeforeCall: String = "",
    val block: String = "",
    val callDetails: String = "",
    val delete: String = "",
)
