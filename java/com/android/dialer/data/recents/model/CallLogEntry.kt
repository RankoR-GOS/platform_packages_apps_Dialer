package com.android.dialer.data.recents.model

import android.provider.CallLog

internal data class CallLogEntry(
    val entryId: CallLogEntryId,
    val number: String,
    val formattedNumber: String?,
    val numberPresentation: Int,
    val geocodedLocation: String?,
    val cachedName: String?,
    val photoUri: String?,
    val lookupUri: String?,
    val timestampMillis: Long,
    val durationSeconds: Long,
    val features: Int,
    val callType: CallType,
    val isRead: Boolean,
    val groupedCallCount: Int = 1,
) {

    val isVideoCall: Boolean
        get() = features and CallLog.Calls.FEATURES_VIDEO != 0
}
