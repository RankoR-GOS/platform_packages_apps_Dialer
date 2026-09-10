package com.android.dialer.data.recents.contact

import android.content.ContentResolver
import android.database.Cursor
import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteDiskIOException
import android.database.sqlite.SQLiteFullException
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.PhoneLookup
import android.telephony.PhoneNumberUtils
import com.android.dialer.common.LogUtil
import com.android.dialer.phonenumberutil.PhoneNumberHelper
import javax.inject.Inject

internal sealed interface ContactLookupResult {

    data class Found(
        val name: String?,
        val photoUri: String?,
        val lookupUri: String?,
        val numberType: Int,
        val numberLabel: String?,
        val alternativeName: String? = null,
        val carrierPresence: Int = 0,
    ) : ContactLookupResult

    data object None : ContactLookupResult

    data object Unavailable : ContactLookupResult
}

internal fun interface ContactLookup {
    operator fun invoke(number: String): ContactLookupResult
}

internal class ContactLookupImpl @Inject constructor(
    private val contentResolver: ContentResolver,
) : ContactLookup {

    override fun invoke(number: String): ContactLookupResult {
        if (number.isBlank()) {
            return ContactLookupResult.None
        }

        return try {
            val result = query(number = number)
            val username = when {
                PhoneNumberHelper.isUriNumber(
                    number
                ) -> PhoneNumberHelper.getUsernameFromUriNumber(number)
                else -> ""
            }

            when {
                result == ContactLookupResult.None && PhoneNumberHelper.isUriNumber(number) &&
                    PhoneNumberUtils.isGlobalPhoneNumber(username) -> query(number = username)
                else -> result
            }
        } catch (_: SecurityException) {
            unavailable(reason = "contacts permission revoked")
        } catch (_: IllegalArgumentException) {
            unavailable(reason = "provider rejected the lookup")
        } catch (_: SQLiteDiskIOException) {
            unavailable(reason = "contacts disk read failed")
        } catch (_: SQLiteFullException) {
            unavailable(reason = "contacts disk full")
        } catch (_: SQLiteDatabaseCorruptException) {
            unavailable(reason = "contacts database corrupt")
        }
    }

    private fun query(number: String): ContactLookupResult {
        val uri = PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI.buildUpon()
            .appendPath(number)
            .appendQueryParameter(
                PhoneLookup.QUERY_PARAMETER_SIP_ADDRESS,
                PhoneNumberHelper.isUriNumber(number).toString(),
            )
            .build()

        return contentResolver.query(uri, PHONE_LOOKUP_PROJECTION, null, null, null)
            ?.use { cursor -> cursor.firstContact(number = number) }
            ?: ContactLookupResult.Unavailable
    }

    private fun unavailable(reason: String): ContactLookupResult {
        LogUtil.e(TAG, "ContactLookupImpl.invoke: $reason")

        return ContactLookupResult.Unavailable
    }

    private fun Cursor.firstContact(number: String): ContactLookupResult {
        if (!moveToFirst()) {
            return ContactLookupResult.None
        }

        val lookupKey = getString(LOOKUP_KEY_INDEX)?.takeIf { it.isNotBlank() }
        val lookupUri = lookupKey?.let { key ->
            Contacts.getLookupUri(getLong(CONTACT_ID_INDEX), key)?.toString()
        }

        return ContactLookupResult.Found(
            name = getString(DISPLAY_NAME_INDEX)?.takeIf { name -> name.isContactName(number) },
            photoUri = getString(PHOTO_URI_INDEX)?.takeIf { it.isNotBlank() },
            lookupUri = lookupUri,
            numberType = getInt(TYPE_INDEX),
            numberLabel = getString(LABEL_INDEX)?.takeIf { it.isNotBlank() },
        ).withAdditionalInfo(contactId = getLong(CONTACT_ID_INDEX), number = number)
    }

    private fun ContactLookupResult.Found.withAdditionalInfo(
        contactId: Long,
        number: String,
    ): ContactLookupResult.Found {
        if (Contacts.isEnterpriseContactId(contactId)) {
            return this
        }

        val alternativeName = lookupUri?.let { uri ->
            contentResolver.query(
                Uri.parse(uri),
                arrayOf(Contacts.DISPLAY_NAME_ALTERNATIVE),
                null,
                null,
                null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0)?.takeIf { it.isNotBlank() } else null
            }
        }
        val presence = when {
            PhoneNumberHelper.isUriNumber(number) -> 0
            else -> contentResolver.query(
                Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, Uri.encode(number)),
                arrayOf(Phone.CARRIER_PRESENCE),
                "${Phone.CONTACT_ID} = ?",
                arrayOf(contactId.toString()),
                null,
            )?.use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 } ?: 0
        }

        return copy(alternativeName = alternativeName, carrierPresence = presence)
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
        private const val TYPE_INDEX = 4
        private const val LABEL_INDEX = 5

        internal val PHONE_LOOKUP_PROJECTION = arrayOf(
            PhoneLookup.CONTACT_ID,
            PhoneLookup.DISPLAY_NAME,
            PhoneLookup.PHOTO_URI,
            PhoneLookup.LOOKUP_KEY,
            PhoneLookup.TYPE,
            PhoneLookup.LABEL,
        )
    }
}
