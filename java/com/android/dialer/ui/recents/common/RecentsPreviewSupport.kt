package com.android.dialer.ui.recents.common

import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.ui.recents.model.RecentsActionLabelsUiModel
import com.android.dialer.ui.recents.model.RecentsAvatarUiModel
import com.android.dialer.ui.recents.model.RecentsCallTypeIcon
import com.android.dialer.ui.recents.model.RecentsItemUiModel
import com.android.dialer.ui.recents.model.RecentsListItemUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

private const val OLDER_FROM = 4L

internal fun previewRecentsItem(
    entryId: CallLogEntryId,
    primaryText: String,
    secondaryText: String = "Kingston, Jamaica • 10:24",
    displayNumber: String = "+1 555-000${entryId.value}",
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
    canMessage: Boolean = canCallBack,
    canAddContact: Boolean = canCallBack,
): RecentsItemUiModel {
    return RecentsItemUiModel(
        entryId = entryId,
        primaryText = primaryText,
        secondaryText = secondaryText,
        displayNumber = displayNumber,
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
        canMessage = canMessage,
        canAddContact = canAddContact,
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

internal fun previewRecentsListItems(): ImmutableList<RecentsListItemUiModel> {
    val (today, older) = previewRecentsItems().partition { item -> item.entryId.value < OLDER_FROM }

    return buildList {
        add(RecentsListItemUiModel.DayHeader(key = "Today", label = "Today"))
        today.forEach { item -> add(RecentsListItemUiModel.Entry(item = item)) }
        add(RecentsListItemUiModel.DayHeader(key = "Older", label = "Older"))
        older.forEach { item -> add(RecentsListItemUiModel.Entry(item = item)) }
    }.toImmutableList()
}

internal fun previewActionLabels(): RecentsActionLabelsUiModel {
    return RecentsActionLabelsUiModel(
        call = "Voice call",
        videoCall = "Video call",
        message = "Message",
        addContact = "Add contact",
        block = "Block",
        copyNumber = "Copy number",
        callDetails = "Call details",
        delete = "Delete",
    )
}
