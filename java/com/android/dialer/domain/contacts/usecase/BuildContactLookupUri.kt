package com.android.dialer.domain.contacts.usecase

import android.content.ContentUris
import android.net.Uri
import android.provider.ContactsContract.Contacts
import com.android.dialer.data.contacts.model.Contact
import javax.inject.Inject

internal interface BuildContactLookupUri {
    operator fun invoke(contact: Contact): Uri
}

internal class BuildContactLookupUriImpl @Inject constructor() : BuildContactLookupUri {

    override fun invoke(contact: Contact): Uri =
        contact.lookupKey
            .takeIf(String::isNotBlank)
            ?.let { lookupKey -> Contacts.getLookupUri(contact.id, lookupKey) }
            ?: ContentUris.withAppendedId(Contacts.CONTENT_URI, contact.id)
}
