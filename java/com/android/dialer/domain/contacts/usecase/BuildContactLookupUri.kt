package com.android.dialer.domain.contacts.usecase

import android.content.ContentUris
import android.net.Uri
import android.provider.ContactsContract.Contacts
import javax.inject.Inject

internal interface BuildContactLookupUri {
    operator fun invoke(contactId: Long, lookupKey: String): Uri
}

internal class BuildContactLookupUriImpl @Inject constructor() : BuildContactLookupUri {

    override fun invoke(contactId: Long, lookupKey: String): Uri =
        lookupKey
            .takeIf(String::isNotBlank)
            ?.let { key -> Contacts.getLookupUri(contactId, key) }
            ?: ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId)
}
