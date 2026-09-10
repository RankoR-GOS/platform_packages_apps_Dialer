package com.android.dialer.testutil

import android.provider.CallLog.Calls
import android.provider.ContactsContract.CommonDataKinds.Phone
import com.android.dialer.data.recents.model.CallLogEntry
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.data.recents.model.CallLogSnapshot
import com.android.dialer.data.recents.model.CallType
import com.android.dialer.ui.recents.model.RecentsAvatarUiModel
import com.android.dialer.ui.recents.model.RecentsCallTypeIcon
import com.android.dialer.ui.recents.model.RecentsItemUiModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal fun callLogEntry(
    id: Long,
    number: String = "+1555000$id",
    formattedNumber: String? = null,
    countryIso: String? = null,
    numberPresentation: Int = Calls.PRESENTATION_ALLOWED,
    geocodedLocation: String? = null,
    cachedName: String? = null,
    photoUri: String? = null,
    lookupUri: String? = null,
    numberType: Int = Phone.TYPE_CUSTOM,
    numberLabel: String? = null,
    timestampMillis: Long = TEST_TIMESTAMP_MILLIS + id,
    durationSeconds: Long = TEST_CALL_DURATION_SECONDS,
    features: Int = 0,
    callType: CallType = CallType.Answered,
    isRead: Boolean = true,
    groupedCallCount: Int = 1,
): CallLogEntry {
    return CallLogEntry(
        entryId = CallLogEntryId(value = id),
        number = number,
        formattedNumber = formattedNumber,
        countryIso = countryIso,
        numberPresentation = numberPresentation,
        geocodedLocation = geocodedLocation,
        cachedName = cachedName,
        photoUri = photoUri,
        lookupUri = lookupUri,
        numberType = numberType,
        numberLabel = numberLabel,
        timestampMillis = timestampMillis,
        durationSeconds = durationSeconds,
        features = features,
        callType = callType,
        isRead = isRead,
        groupedEntryIds = (0 until groupedCallCount)
            .map { offset -> CallLogEntryId(value = id + offset) }
            .toImmutableList(),
    )
}

internal fun callLogSnapshot(
    vararg entries: CallLogEntry,
    isPermissionGranted: Boolean = true,
): CallLogSnapshot {
    return CallLogSnapshot(
        entries = entries.toList().toImmutableList(),
        isPermissionGranted = isPermissionGranted,
    )
}

internal fun recentsItemUiModel(
    id: Long,
    primaryText: String = "Caller $id",
    number: String = "+1555000$id",
): RecentsItemUiModel {
    return RecentsItemUiModel(
        entryId = CallLogEntryId(value = id),
        primaryText = primaryText,
        secondaryText = "Kingston, Jamaica • 5 min ago",
        displayNumber = number,
        spokenDisplayNumber = number,
        contentDescription = "1 answered call from $primaryText",
        clickActionLabel = "expand menu",
        callActionLabel = "Call $primaryText",
        avatar = RecentsAvatarUiModel(photoUri = null, letter = 'C'),
        callTypeIcon = RecentsCallTypeIcon.Incoming,
        groupedCallCountLabel = null,
        groupedEntryIds = persistentListOf(CallLogEntryId(value = id)),
        number = number,
        isUnreadMissedCall = false,
        canCallBack = true,
        canVideoCall = false,
        canMessage = true,
        canAddContact = true,
        canEditNumberBeforeCall = true,
    )
}
