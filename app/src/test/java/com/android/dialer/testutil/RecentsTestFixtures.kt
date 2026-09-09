package com.android.dialer.testutil

import android.provider.CallLog.Calls
import com.android.dialer.data.recents.model.CallLogEntry
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.data.recents.model.CallType

internal fun callLogEntry(
    id: Long,
    number: String = "+1555000$id",
    formattedNumber: String? = null,
    numberPresentation: Int = Calls.PRESENTATION_ALLOWED,
    geocodedLocation: String? = null,
    cachedName: String? = null,
    photoUri: String? = null,
    lookupUri: String? = null,
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
        numberPresentation = numberPresentation,
        geocodedLocation = geocodedLocation,
        cachedName = cachedName,
        photoUri = photoUri,
        lookupUri = lookupUri,
        timestampMillis = timestampMillis,
        durationSeconds = durationSeconds,
        features = features,
        callType = callType,
        isRead = isRead,
        groupedCallCount = groupedCallCount,
    )
}
