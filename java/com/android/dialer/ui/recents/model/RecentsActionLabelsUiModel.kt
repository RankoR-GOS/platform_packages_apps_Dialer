package com.android.dialer.ui.recents.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class RecentsActionLabelsUiModel(
    val call: String = "",
    val videoCall: String = "",
    val message: String = "",
    val addContact: String = "",
    val block: String = "",
    val copyNumber: String = "",
    val callDetails: String = "",
    val delete: String = "",
)
