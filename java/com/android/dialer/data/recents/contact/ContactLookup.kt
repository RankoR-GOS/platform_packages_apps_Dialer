package com.android.dialer.data.recents.contact

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.PhoneLookup
import android.telephony.PhoneNumberUtils
import com.android.dialer.common.LogUtil
import javax.inject.Inject

internal data class ContactLookupResult(
    val name: String?,
    val photoUri: String?,
    val lookupUri: String?,
)

internal fun interface ContactLookup {
    operator fun invoke(number: String): ContactLookupResult?
}

internal class ContactLookupImpl @Inject constructor(
    private val contentResolver: ContentResolver,
) : ContactLookup {

    override fun invoke(number: String): ContactLookupResult? {
        if (number.isBlank()) {
            return null
        }

        val uri = Uri.withAppendedPath(
            PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI,
            Uri.encode(number),
        )

        return try {
            contentResolver.query(uri, PHONE_LOOKUP_PROJECTION, null, null, null)
                ?.use { cursor -> cursor.firstContactOrNull(number = number) }
        } catch (_: SecurityException) {
            LogUtil.e(TAG, "ContactLookupImpl.invoke: contacts permission revoked")
            null
        } catch (_: IllegalArgumentException) {
            LogUtil.e(TAG, "ContactLookupImpl.invoke: provider rejected the lookup")
            null
        }
    }

    private fun Cursor.firstContactOrNull(number: String): ContactLookupResult? {
        if (!moveToFirst()) {
            return null
        }

        val lookupKey = getString(LOOKUP_KEY_INDEX)?.takeIf { it.isNotBlank() }
        val lookupUri = lookupKey?.let { key ->
            Contacts.getLookupUri(getLong(CONTACT_ID_INDEX), key)?.toString()
        }

        return ContactLookupResult(
            name = getString(DISPLAY_NAME_INDEX)?.takeIf { name -> name.isContactName(number) },
            photoUri = getString(PHOTO_URI_INDEX)?.takeIf { it.isNotBlank() },
            lookupUri = lookupUri,
        )
    }

    private fun String.isContactName(number: String): Boolean {
        return isNotBlank() && !PhoneNumberUtils.compare(this, number)
    }

    internal companion object {
        private const val TAG = "ContactLookupImpl"
        private const val CONTACT_ID_INDEX = 0
        private const val DISPLAY_NAME_INDEX = 1
        private const val PHOTO_URI_INDEX = 2
        private const val LOOKUP_KEY_INDEX = 3

        internal val PHONE_LOOKUP_PROJECTION = arrayOf(
            PhoneLookup.CONTACT_ID,
            PhoneLookup.DISPLAY_NAME,
            PhoneLookup.PHOTO_URI,
            PhoneLookup.LOOKUP_KEY,
        )
    }
}
