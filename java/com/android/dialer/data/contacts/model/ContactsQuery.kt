package com.android.dialer.data.contacts.model

internal data class ContactsQuery(
    val filter: String = "",
    val requirePhoneNumber: Boolean = false,
)
