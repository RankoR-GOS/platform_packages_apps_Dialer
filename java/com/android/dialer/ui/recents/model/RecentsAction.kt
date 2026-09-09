package com.android.dialer.ui.recents.model

import com.android.dialer.data.recents.model.CallLogEntryId

internal sealed interface RecentsAction {

    data object ScreenResumed : RecentsAction

    data object GrantPermissionClicked : RecentsAction

    data object CallLogPermissionGranted : RecentsAction

    data class CallBackClicked(
        val number: String,
    ) : RecentsAction

    data class VideoCallClicked(
        val number: String,
    ) : RecentsAction

    data class MessageClicked(
        val number: String,
    ) : RecentsAction

    data class AddContactClicked(
        val number: String,
    ) : RecentsAction

    data class CopyNumberClicked(
        val number: String,
    ) : RecentsAction

    data class DeleteConfirmed(
        val entryId: CallLogEntryId,
    ) : RecentsAction

    data object ClearHistoryConfirmed : RecentsAction
}
