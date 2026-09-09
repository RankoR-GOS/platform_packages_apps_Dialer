package com.android.dialer.calldetails.model

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class CallDetailsHeaderUiModel(
    val primaryText: String,
    val secondaryText: String? = null,
    val number: String,
    val postDialDigits: String = "",
    val photoUri: String? = null,
    val photoBitmap: Bitmap? = null,
    val contactLookupKey: String? = null,
    val contactUri: String? = null,
    val contactType: Int = 1,
    val isSpam: Boolean = false,
    val isBlocked: Boolean = false,
    val canReportCallerId: Boolean = false,
    val canSupportAssistedDialing: Boolean = false,
    val accountLabel: String? = null,
)

@Immutable
internal data class CallDetailsEntryUiModel(
    val callId: Long,
    val callType: Int,
    val timestamp: Long,
    val formattedDate: String,
    val durationSeconds: Long,
    val formattedDuration: String,
    val dataUsage: Long = 0L,
    val isVideoCall: Boolean = false,
    val isRtt: Boolean = false,
    val postCallNote: String? = null,
    val accountLabel: String? = null,
)

@Immutable
internal sealed interface CallDetailsDeleteDialogState {
    data class DeleteEntry(
        val callId: Long,
    ) : CallDetailsDeleteDialogState

    data object DeleteAll : CallDetailsDeleteDialogState
}

@Immutable
internal sealed interface CallDetailsUiState {
    data object Loading : CallDetailsUiState
    data object Unavailable : CallDetailsUiState
    data class Content(
        val header: CallDetailsHeaderUiModel,
        val entries: ImmutableList<CallDetailsEntryUiModel>,
        val deleteDialogState: CallDetailsDeleteDialogState? = null,
        val canReportCallerId: Boolean = false,
        val canSupportAssistedDialing: Boolean = false,
    ) : CallDetailsUiState
}

internal data class CallDetailsData(
    val header: CallDetailsHeaderData,
    val entries: List<CallDetailsEntryData>,
    val canReportCallerId: Boolean = false,
    val canSupportAssistedDialing: Boolean = false,
)

internal data class CallDetailsHeaderData(
    val primaryText: String,
    val secondaryText: String? = null,
    val number: String,
    val postDialDigits: String = "",
    val photoUri: String? = null,
    val contactLookupKey: String? = null,
    val contactUri: String? = null,
    val contactType: Int = 1,
    val isSpam: Boolean = false,
    val isBlocked: Boolean = false,
    val accountLabel: String? = null,
)

internal data class CallDetailsEntryData(
    val callId: Long,
    val callType: Int,
    val timestamp: Long,
    val durationSeconds: Long,
    val dataUsage: Long = 0L,
    val isVideoCall: Boolean = false,
    val isRtt: Boolean = false,
    val postCallNote: String? = null,
    val accountLabel: String? = null,
)
