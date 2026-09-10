package com.android.dialer.ui.recents.model

import androidx.compose.runtime.Immutable
import com.android.dialer.data.recents.model.CallLogEntryId
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class RecentsItemUiModel(
    val entryId: CallLogEntryId,
    val primaryText: String,
    val secondaryText: String,
    val displayNumber: String,
    val spokenDisplayNumber: String,
    val contentDescription: String,
    val clickActionLabel: String,
    val callActionLabel: String?,
    val avatar: RecentsAvatarUiModel,
    val callTypeIcon: RecentsCallTypeIcon,
    val groupedCallCountLabel: String?,
    val groupedEntryIds: ImmutableList<CallLogEntryId>,
    val number: String,
    val isUnreadMissedCall: Boolean,
    val canCallBack: Boolean,
    val canVideoCall: Boolean,
    val canMessage: Boolean,
    val canAddContact: Boolean,
    val canEditNumberBeforeCall: Boolean,
    val postDialDigits: String = "",
    val isVideoCall: Boolean = false,
    val accountLabel: String? = null,
    val isHdCall: Boolean = false,
    val isRttCall: Boolean = false,
    val isAssistedDialing: Boolean = false,
    val accountComponentName: String? = null,
    val accountId: String? = null,
) {

    val callbackNumber: String
        get() = number + postDialDigits

    val isPrimaryTextTheNumber: Boolean
        get() = displayNumber.isNotBlank() && primaryText == displayNumber
}

@Immutable
internal data class RecentsAvatarUiModel(
    val photoUri: String?,
    val letter: Char?,
)

internal enum class RecentsCallTypeIcon {

    Incoming,
    Outgoing,
    Missed,
    Blocked,
    Voicemail,
}
