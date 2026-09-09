package com.android.dialer.data.recents.contact

import android.content.ContentResolver
import android.database.MatrixCursor
import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteDiskIOException
import android.database.sqlite.SQLiteFullException
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.PhoneLookup
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class ContactLookupImplTest {

    private val contentResolver = mockk<ContentResolver>()
    private val lookup = ContactLookupImpl(contentResolver = contentResolver)

    @Test
    fun invoke_withACustomLabel_returnsTheLabelWithTheCustomType() {
        every { contentResolver.query(any(), any(), null, null, null) } returns
            phoneLookupCursor(
                contactId = 42L,
                name = "Ada Lovelace",
                photoUri = null,
                key = "k42",
                numberType = Phone.TYPE_CUSTOM,
                numberLabel = "Studio",
            )

        val found = lookup(NUMBER) as ContactLookupResult.Found

        assertEquals(Phone.TYPE_CUSTOM, found.numberType)
        assertEquals("Studio", found.numberLabel)
    }

    @Test
    fun invoke_withAMatchingContact_returnsItsNamePhotoAndLookupUri() {
        val capturedUris = mutableListOf<Uri>()
        every { contentResolver.query(capture(capturedUris), any(), null, null, null) } returns
            phoneLookupCursor(
                contactId = 42L,
                name = "Ada Lovelace",
                photoUri = PHOTO,
                key = "k42",
                numberType = Phone.TYPE_MOBILE,
            )

        val result = lookup(NUMBER)

        assertEquals(
            ContactLookupResult.Found(
                name = "Ada Lovelace",
                photoUri = PHOTO,
                lookupUri = "content://com.android.contacts/contacts/lookup/k42/42",
                numberType = Phone.TYPE_MOBILE,
                numberLabel = null,
            ),
            result,
        )
        assertEquals(
            Uri.withAppendedPath(PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI, Uri.encode(NUMBER)),
            capturedUris.single(),
        )
    }

    @Test
    fun invoke_whenTheDisplayNameIsOnlyTheNumber_returnsTheContactWithoutAName() {
        every { contentResolver.query(any(), any(), null, null, null) } returns
            phoneLookupCursor(contactId = 7L, name = "+1 876-555-0201", photoUri = null, key = "k7")

        assertEquals(
            ContactLookupResult.Found(
                name = null,
                photoUri = null,
                lookupUri = "content://com.android.contacts/contacts/lookup/k7/7",
                numberType = Phone.TYPE_CUSTOM,
                numberLabel = null,
            ),
            lookup(NUMBER),
        )
    }

    @Test
    fun invoke_withNoMatchingContact_returnsNone() {
        every { contentResolver.query(any(), any(), null, null, null) } returns
            MatrixCursor(ContactLookupImpl.PHONE_LOOKUP_PROJECTION)

        assertEquals(ContactLookupResult.None, lookup(NUMBER))
    }

    @Test
    fun invoke_whenTheProviderRefusesTheLookup_returnsUnavailable() {
        every { contentResolver.query(any(), any(), null, null, null) } throws
            SecurityException("no contacts permission")

        assertEquals(ContactLookupResult.Unavailable, lookup(NUMBER))
    }

    @Test
    fun invoke_whenTheProviderReturnsNoCursor_returnsUnavailable() {
        every { contentResolver.query(any(), any(), null, null, null) } returns null

        assertEquals(ContactLookupResult.Unavailable, lookup(NUMBER))
    }

    @Test
    fun invoke_whenTheContactsDatabaseFails_returnsUnavailable() {
        listOf(
            SQLiteDiskIOException(),
            SQLiteFullException(),
            SQLiteDatabaseCorruptException(),
        ).forEach { failure ->
            every { contentResolver.query(any(), any(), null, null, null) } throws failure

            assertEquals(ContactLookupResult.Unavailable, lookup(NUMBER))
        }
    }

    @Test
    fun invoke_whenTheProviderRefusesOrRejectsTheLookup_logsWithoutTheNumber() {
        listOf(
            SecurityException("Permission Denial: reading uri $LOOKUP_URI from pid=1"),
            IllegalArgumentException("Invalid URI $LOOKUP_URI"),
        ).forEach { failure ->
            ShadowLog.clear()
            every { contentResolver.query(any(), any(), null, null, null) } throws failure

            lookup(NUMBER)

            val logged = ShadowLog.getLogs().joinToString { item ->
                "${item.msg} ${item.throwable?.message.orEmpty()}"
            }
            assertTrue(logged.contains("ContactLookupImpl.invoke"))
            assertFalse(logged.contains(NUMBER.removePrefix("+")))
        }
    }

    @Test
    fun invoke_withABlankNumber_answersNoContactWithoutQuerying() {
        assertEquals(ContactLookupResult.None, lookup(" "))

        verify(exactly = 0) { contentResolver.query(any(), any(), null, null, null) }
    }

    private fun phoneLookupCursor(
        contactId: Long,
        name: String,
        photoUri: String?,
        key: String,
        numberType: Int = Phone.TYPE_CUSTOM,
        numberLabel: String? = null,
    ): MatrixCursor {
        return MatrixCursor(ContactLookupImpl.PHONE_LOOKUP_PROJECTION).apply {
            addRow(arrayOf<Any?>(contactId, name, photoUri, key, numberType, numberLabel))
        }
    }

    private companion object {
        const val NUMBER = "+18765550201"
        const val PHOTO = "content://com.android.contacts/contacts/42/photo"
        const val LOOKUP_URI = "content://com.android.contacts/phone_lookup/%2B18765550201"
    }
}
