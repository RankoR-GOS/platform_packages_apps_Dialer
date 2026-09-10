package com.android.dialer.ui.recents.model

import com.android.dialer.data.recents.model.CallLogEntryId
import kotlinx.collections.immutable.ImmutableList

internal sealed interface RecentsAction {

    sealed interface LifecycleAction : RecentsAction

    sealed interface StatusAction : RecentsAction

    sealed interface NumberAction : RecentsAction {
        val number: String
    }

    sealed interface EntryAction : RecentsAction

    data object ScreenResumed : LifecycleAction

    data object CallLogPermissionGranted : LifecycleAction

    data object GrantPermissionClicked : StatusAction

    data object MakeCallClicked : StatusAction

    data class CallBackClicked(
        override val number: String,
    ) : NumberAction

    data class VideoCallClicked(
        override val number: String,
    ) : NumberAction

    data class MessageClicked(
        override val number: String,
    ) : NumberAction

    data class CreateContactClicked(
        override val number: String,
    ) : NumberAction

    data class AddContactClicked(
        override val number: String,
    ) : NumberAction

    data class CopyNumberClicked(
        override val number: String,
    ) : NumberAction

    data class EditNumberBeforeCallClicked(
        override val number: String,
    ) : NumberAction

    data class EntryViewed(
        val entryIds: ImmutableList<CallLogEntryId>,
    ) : EntryAction

    data class DeleteConfirmed(
        val entryIds: ImmutableList<CallLogEntryId>,
    ) : EntryAction
}
