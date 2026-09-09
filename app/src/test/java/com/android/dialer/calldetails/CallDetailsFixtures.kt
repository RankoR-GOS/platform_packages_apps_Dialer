package com.android.dialer.calldetails

import android.provider.CallLog.Calls
import com.android.dialer.calldetails.model.CallDetailsEntryUiModel
import com.android.dialer.calldetails.model.CallDetailsHeaderUiModel
import com.android.dialer.calldetails.model.CallDetailsUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal const val SYNTHETIC_CONTACT_NAME = "Ada Lovelace"
internal const val SYNTHETIC_PHONE_NUMBER = "+1 555 0100"
internal const val SYNTHETIC_NORMALIZED_NUMBER = "+15550100"
internal const val SYNTHETIC_ENTRY_ID_INCOMING = 101L
internal const val SYNTHETIC_ENTRY_ID_OUTGOING = 102L
internal const val SYNTHETIC_POST_DIAL_DIGITS = ";1234"
internal const val SYNTHETIC_ENTRY_ID_MISSED = 103L

internal fun syntheticCallDetailsHeader(
    primaryText: String = SYNTHETIC_CONTACT_NAME,
    secondaryText: String? = SYNTHETIC_PHONE_NUMBER,
    number: String = SYNTHETIC_NORMALIZED_NUMBER,
    postDialDigits: String = "",
    photoUri: String? = null,
    contactLookupKey: String? = "lookup-42",
    contactUri: String? = null,
    contactType: Int = 1,
    isSpam: Boolean = false,
    isBlocked: Boolean = false,
    canReportCallerId: Boolean = true,
    canSupportAssistedDialing: Boolean = false,
    accountLabel: String? = null,
): CallDetailsHeaderUiModel =
    CallDetailsHeaderUiModel(
        primaryText = primaryText,
        secondaryText = secondaryText,
        number = number,
        postDialDigits = postDialDigits,
        photoUri = photoUri,
        contactLookupKey = contactLookupKey,
        contactUri = contactUri,
        contactType = contactType,
        isSpam = isSpam,
        isBlocked = isBlocked,
        canReportCallerId = canReportCallerId,
        canSupportAssistedDialing = canSupportAssistedDialing,
        accountLabel = accountLabel,
    )

internal fun sampleHeaderModel(
    name: String = SYNTHETIC_CONTACT_NAME,
    number: String = SYNTHETIC_PHONE_NUMBER,
    normalizedNumber: String = SYNTHETIC_NORMALIZED_NUMBER,
    postDialDigits: String = "",
): CallDetailsHeaderUiModel = syntheticCallDetailsHeader(
    primaryText = name,
    secondaryText = number,
    number = normalizedNumber,
    postDialDigits = postDialDigits,
)

internal fun syntheticCallDetailsEntry(
    callId: Long = SYNTHETIC_ENTRY_ID_INCOMING,
    callType: Int = Calls.INCOMING_TYPE,
    timestamp: Long = 1725700000000L,
    formattedDate: String = "Today, 2:15 PM",
    durationSeconds: Long = 142L,
    formattedDuration: String = "2 min 22 sec",
    dataUsage: Long = 0L,
    isVideoCall: Boolean = false,
    isRtt: Boolean = false,
    postCallNote: String? = null,
    accountLabel: String? = null,
): CallDetailsEntryUiModel =
    CallDetailsEntryUiModel(
        callId = callId,
        callType = callType,
        timestamp = timestamp,
        formattedDate = formattedDate,
        durationSeconds = durationSeconds,
        formattedDuration = formattedDuration,
        dataUsage = dataUsage,
        isVideoCall = isVideoCall,
        isRtt = isRtt,
        postCallNote = postCallNote,
        accountLabel = accountLabel,
    )

internal fun syntheticCallDetailsEntriesList(): ImmutableList<CallDetailsEntryUiModel> =
    persistentListOf(
        syntheticCallDetailsEntry(
            callId = SYNTHETIC_ENTRY_ID_INCOMING,
            callType = Calls.INCOMING_TYPE,
            formattedDate = "Today, 2:15 PM",
            durationSeconds = 142L,
            formattedDuration = "2 min 22 sec",
        ),
        syntheticCallDetailsEntry(
            callId = SYNTHETIC_ENTRY_ID_OUTGOING,
            callType = Calls.OUTGOING_TYPE,
            formattedDate = "Yesterday, 6:30 PM",
            durationSeconds = 45L,
            formattedDuration = "45 sec",
        ),
        syntheticCallDetailsEntry(
            callId = SYNTHETIC_ENTRY_ID_MISSED,
            callType = Calls.MISSED_TYPE,
            formattedDate = "Sunday, 11:05 AM",
            durationSeconds = 0L,
            formattedDuration = "0 sec",
        ),
    )

internal fun syntheticCallDetailsContentUiState(
    header: CallDetailsHeaderUiModel = syntheticCallDetailsHeader(),
    entries: ImmutableList<CallDetailsEntryUiModel> = syntheticCallDetailsEntriesList(),
): CallDetailsUiState.Content =
    CallDetailsUiState.Content(
        header = header,
        entries = entries,
        deleteDialogState = null,
    )
