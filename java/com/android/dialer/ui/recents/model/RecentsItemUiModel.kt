package com.android.dialer.ui.recents.model

import androidx.compose.runtime.Immutable
import com.android.dialer.data.recents.model.CallLogEntryId

@Immutable
internal data class RecentsItemUiModel(
    val entryId: CallLogEntryId,
    val primaryText: String,
    val secondaryText: String,
    val contentDescription: String,
    val avatar: RecentsAvatarUiModel,
    val callTypeIcon: RecentsCallTypeIcon,
    val groupedCallCountLabel: String?,
    val number: String,
    val isUnreadMissedCall: Boolean,
    val canCallBack: Boolean,
    val canVideoCall: Boolean,
)

@Immutable
internal data class RecentsAvatarUiModel(
    val photoUri: String?,
    val letter: Char?,
    val lookupUri: String?,
)

internal enum class RecentsCallTypeIcon {

    Incoming,
    Outgoing,
    Missed,
    Blocked,
    Voicemail,
}
