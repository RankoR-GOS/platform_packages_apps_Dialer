package com.android.dialer.data.recents.contact

import android.content.ContentResolver
import android.database.MatrixCursor
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract.PhoneLookup
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class ContactLookupImplTest {

    private val contentResolver = mockk<ContentResolver>()
    private val lookup = ContactLookupImpl(contentResolver = contentResolver)

    @Test
    fun invoke_withAMatchingContact_returnsItsNamePhotoAndLookupUri() {
        val capturedUris = mutableListOf<Uri>()
        every { contentResolver.query(capture(capturedUris), any(), null, null, null) } returns
            phoneLookupCursor(contactId = 42L, name = "Ada Lovelace", photoUri = PHOTO, key = "k42")

        val result = lookup(NUMBER)

        assertEquals("Ada Lovelace", result?.name)
        assertEquals(PHOTO, result?.photoUri)
        assertEquals("content://com.android.contacts/contacts/lookup/k42/42", result?.lookupUri)
        assertEquals(
            Uri.withAppendedPath(PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI, Uri.encode(NUMBER)),
            capturedUris.single(),
        )
    }

    @Test
    fun invoke_whenTheDisplayNameIsOnlyTheNumber_returnsTheContactWithoutAName() {
        every { contentResolver.query(any(), any(), null, null, null) } returns
            phoneLookupCursor(contactId = 7L, name = "+1 876-555-0201", photoUri = null, key = "k7")

        val result = lookup(NUMBER)

        assertNull(result?.name)
        assertEquals("content://com.android.contacts/contacts/lookup/k7/7", result?.lookupUri)
    }

    @Test
    fun invoke_withNoMatchingContact_returnsNull() {
        every { contentResolver.query(any(), any(), null, null, null) } returns
            MatrixCursor(ContactLookupImpl.PHONE_LOOKUP_PROJECTION)

        assertNull(lookup(NUMBER))
    }

    @Test
    fun invoke_whenTheProviderRefusesTheLookup_returnsNull() {
        every { contentResolver.query(any(), any(), null, null, null) } throws
            SecurityException("no contacts permission")

        assertNull(lookup(NUMBER))
    }

    @Test
    fun invoke_withABlankNumber_doesNotQuery() {
        assertNull(lookup(" "))

        verify(exactly = 0) { contentResolver.query(any(), any(), null, null, null) }
    }

    private fun phoneLookupCursor(
        contactId: Long,
        name: String,
        photoUri: String?,
        key: String,
    ): MatrixCursor {
        return MatrixCursor(ContactLookupImpl.PHONE_LOOKUP_PROJECTION).apply {
            addRow(arrayOf<Any?>(contactId, name, photoUri, key))
        }
    }

    private companion object {
        const val NUMBER = "+18765550201"
        const val PHOTO = "content://com.android.contacts/contacts/42/photo"
    }
}
