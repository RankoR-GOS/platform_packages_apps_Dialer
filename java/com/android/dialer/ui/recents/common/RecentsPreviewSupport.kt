package com.android.dialer.ui.recents.common

import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.ui.recents.model.RecentsAvatarUiModel
import com.android.dialer.ui.recents.model.RecentsCallTypeIcon
import com.android.dialer.ui.recents.model.RecentsItemUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal fun previewRecentsItem(
    entryId: CallLogEntryId,
    primaryText: String,
    secondaryText: String = "Kingston, Jamaica • 10:24",
    contentDescription: String = "1 answered call from $primaryText; 10:24",
    clickActionLabel: String = "expand menu",
    callActionLabel: String? = "Call $primaryText",
    avatar: RecentsAvatarUiModel = RecentsAvatarUiModel(
        photoUri = null,
        letter = primaryText.firstOrNull()?.takeIf { it.isLetter() }?.uppercaseChar(),
        lookupUri = null,
    ),
    callTypeIcon: RecentsCallTypeIcon = RecentsCallTypeIcon.Incoming,
    groupedCallCountLabel: String? = null,
    number: String = "+1555000${entryId.value}",
    isUnreadMissedCall: Boolean = false,
    canCallBack: Boolean = true,
    canVideoCall: Boolean = false,
): RecentsItemUiModel {
    return RecentsItemUiModel(
        entryId = entryId,
        primaryText = primaryText,
        secondaryText = secondaryText,
        contentDescription = contentDescription,
        clickActionLabel = clickActionLabel,
        callActionLabel = callActionLabel,
        avatar = avatar,
        callTypeIcon = callTypeIcon,
        groupedCallCountLabel = groupedCallCountLabel,
        number = number,
        isUnreadMissedCall = isUnreadMissedCall,
        canCallBack = canCallBack,
        canVideoCall = canVideoCall,
    )
}

internal fun previewRecentsItems(): ImmutableList<RecentsItemUiModel> {
    return persistentListOf(
        previewRecentsItem(
            entryId = CallLogEntryId(value = 1L),
            primaryText = "Ada Lovelace",
            secondaryText = "2 min ago",
            contentDescription = "3 missed calls from Ada Lovelace; 2 minutes ago",
            callTypeIcon = RecentsCallTypeIcon.Missed,
            groupedCallCountLabel = "(3)",
            isUnreadMissedCall = true,
        ),
        previewRecentsItem(
            entryId = CallLogEntryId(value = 2L),
            primaryText = "Grace Hopper",
            secondaryText = "Video call • 10:24",
            contentDescription = "1 outgoing call to Grace Hopper; Video call, 10:24",
            callActionLabel = "Video call Grace Hopper.",
            callTypeIcon = RecentsCallTypeIcon.Outgoing,
            canVideoCall = true,
        ),
        previewRecentsItem(
            entryId = CallLogEntryId(value = 3L),
            primaryText = "+1 555-0003",
            contentDescription = "1 answered call from +1 555-0003; Kingston, Jamaica, 10:24",
            callActionLabel = "Call +1 555-0003",
        ),
        previewRecentsItem(
            entryId = CallLogEntryId(value = 4L),
            primaryText = "Private number",
            secondaryText = "Yesterday",
            contentDescription = "1 missed call from Private number; Yesterday",
            callActionLabel = null,
            callTypeIcon = RecentsCallTypeIcon.Missed,
            canCallBack = false,
        ),
    )
}
