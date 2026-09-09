package com.android.dialer.calldetails.ui

import com.android.dialer.calldetails.model.CallDetailsDeleteDialogState
import com.android.dialer.calldetails.model.CallDetailsUiState
import kotlinx.coroutines.flow.StateFlow

/**
 * UI interaction and state model contract for the Call Details screen.
 *
 * Defines the observable UI state and user event callbacks, serving as a boundary between the
 * presentation layer ([CallDetailsViewModel]) and the stateless composables ([CallDetailsScreen]).
 */
internal interface CallDetailsScreenModel {
    val uiState: StateFlow<CallDetailsUiState>

    fun onPlaceVoiceCall(
        phoneNumber: String,
        postDialDigits: String = "",
    )

    fun onPlaceVideoCall(
        phoneNumber: String,
    )

    fun onSendSms(
        phoneNumber: String,
    )

    fun onCopyNumber(
        phoneNumber: String,
    )

    fun onEditNumber(
        phoneNumber: String,
    )

    fun onDeleteCall(
        callId: Long,
    )

    fun onDeleteAllEntries()

    fun onConfirmDelete(
        state: CallDetailsDeleteDialogState,
    )

    fun onDismissDeleteDialog()

    fun onOpenContact()
}
