package com.android.dialer.domain.contacts.usecase

import android.os.Build
import android.provider.ContactsContract.Contacts
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
class BuildContactLookupUriTest {

    private val buildLookupUri = BuildContactLookupUriImpl()

    @Test
    fun prefersTheLookupUriWhenAKeyIsAvailable() {
        val uri = buildLookupUri(contactId = 42L, lookupKey = "abc")

        assertEquals(Contacts.getLookupUri(42L, "abc"), uri)
    }

    @Test
    fun fallsBackToTheIdUriWhenTheKeyIsMissing() {
        val uri = buildLookupUri(contactId = 42L, lookupKey = "")

        assertEquals("${Contacts.CONTENT_URI}/42", uri.toString())
    }

    @Test
    fun fallsBackToTheIdUriWhenTheKeyIsBlank() {
        val uri = buildLookupUri(contactId = 7L, lookupKey = "   ")

        assertEquals("${Contacts.CONTENT_URI}/7", uri.toString())
    }
}
