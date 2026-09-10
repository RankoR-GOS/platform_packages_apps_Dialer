package com.android.dialer.data.recents.model

import android.provider.CallLog
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class CallLogEntry(
    val entryId: CallLogEntryId,
    val number: String,
    val formattedNumber: String?,
    val countryIso: String?,
    val numberPresentation: Int,
    val geocodedLocation: String?,
    val cachedName: String?,
    val photoUri: String?,
    val lookupUri: String?,
    val numberType: Int,
    val numberLabel: String?,
    val timestampMillis: Long,
    val durationSeconds: Long,
    val features: Int,
    val callType: CallType,
    val isRead: Boolean,
    val accountComponentName: String? = null,
    val accountId: String? = null,
    val postDialDigits: String = "",
    val viaNumber: String = "",
    val groupedEntryIds: ImmutableList<CallLogEntryId> = persistentListOf(entryId),
) {

    val groupedCallCount: Int
        get() = groupedEntryIds.size

    val isVideoCall: Boolean
        get() = features and CallLog.Calls.FEATURES_VIDEO != 0
}
