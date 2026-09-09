package com.android.dialer.calldetails.ui

import android.provider.CallLog.Calls
import com.android.dialer.calldetails.model.CallDetailsEntryUiModel
import com.android.dialer.calldetails.model.CallDetailsHeaderUiModel
import kotlinx.collections.immutable.persistentListOf

internal object CallDetailsPreviewData {
    val sampleHeader = CallDetailsHeaderUiModel(
        primaryText = "Ada Lovelace",
        secondaryText = "+1 555 0100",
        number = "+15550100",
        postDialDigits = "",
        photoUri = null,
        contactLookupKey = "lookup-42",
        contactUri = null,
        contactType = 1,
        isSpam = false,
        isBlocked = false,
        canReportCallerId = true,
        canSupportAssistedDialing = false,
    )

    val sampleEntries = persistentListOf(
        CallDetailsEntryUiModel(
            callId = 1L,
            callType = Calls.INCOMING_TYPE,
            timestamp = 1725700000000L,
            formattedDate = "Today, 2:15 PM",
            durationSeconds = 142L,
            formattedDuration = "2 min 22 sec",
            dataUsage = 0L,
            isVideoCall = false,
            isRtt = false,
            postCallNote = null,
        ),
        CallDetailsEntryUiModel(
            callId = 2L,
            callType = Calls.OUTGOING_TYPE,
            timestamp = 1725613600000L,
            formattedDate = "Yesterday, 6:30 PM",
            durationSeconds = 45L,
            formattedDuration = "45 sec",
            dataUsage = 0L,
            isVideoCall = false,
            isRtt = false,
            postCallNote = null,
        ),
        CallDetailsEntryUiModel(
            callId = 3L,
            callType = Calls.MISSED_TYPE,
            timestamp = 1725527200000L,
            formattedDate = "Sunday, 11:05 AM",
            durationSeconds = 0L,
            formattedDuration = "0 sec",
            dataUsage = 0L,
            isVideoCall = false,
            isRtt = false,
            postCallNote = null,
        ),
    )
}
