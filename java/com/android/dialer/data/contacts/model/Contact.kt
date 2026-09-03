package com.android.dialer.data.contacts.model

internal data class Contact(
    val id: Long,
    val lookupKey: String,
    val displayName: String,
    val photoId: Long,
    val photoUri: String?,
)
