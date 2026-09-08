package com.android.dialer.data.recents.model

internal sealed interface CallType {

    sealed interface Incoming : CallType

    sealed interface Unanswered : Incoming

    data object Answered : Incoming

    data object Outgoing : CallType

    data object Missed : Unanswered

    data object Rejected : Unanswered

    data object Blocked : Unanswered

    data object Voicemail : Unanswered

    data class Unknown(
        val rawType: Int,
    ) : CallType
}
