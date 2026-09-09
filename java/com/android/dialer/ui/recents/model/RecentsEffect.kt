package com.android.dialer.ui.recents.model

internal sealed interface RecentsEffect {

    data object RequestCallLogPermission : RecentsEffect

    data class PlaceCall(
        val number: String,
    ) : RecentsEffect

    data class PlaceVideoCall(
        val number: String,
    ) : RecentsEffect

    data class SendMessage(
        val number: String,
    ) : RecentsEffect

    data class AddContact(
        val number: String,
    ) : RecentsEffect

    data class CopyNumber(
        val number: String,
    ) : RecentsEffect

    data object WriteFailed : RecentsEffect
}
