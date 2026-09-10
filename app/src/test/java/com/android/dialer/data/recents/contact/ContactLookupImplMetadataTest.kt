package com.android.dialer.data.recents.contact

import android.content.ContentResolver
import android.database.MatrixCursor
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.PhoneLookup
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class ContactLookupImplMetadataTest {

    private val resolver = mockk<ContentResolver>()
    private val lookup = ContactLookupImpl(resolver)
    private val queries = mutableListOf<Uri>()

    @Test
    fun invoke_withASipContact_usesTheSipQueryAndPreservesTheWholeAddress() {
        stub(found = true)

        val result = lookup("reviewer42@example.invalid") as ContactLookupResult.Found

        assertEquals("Ada", result.name)
        assertEquals(
            "true",
            queries.first().getQueryParameter(PhoneLookup.QUERY_PARAMETER_SIP_ADDRESS)
        )
        assertEquals("reviewer42@example.invalid", queries.first().lastPathSegment)
        assertEquals(1, queries.count { it.path?.startsWith("/phone_lookup") == true })
    }

    @Test
    fun invoke_withAnEncodedSipSeparator_usesTheSipQueryWithoutLosingTheNumber() {
        stub(found = true)

        lookup("42%40example.invalid")

        assertEquals(
            "true",
            queries.first().getQueryParameter(PhoneLookup.QUERY_PARAMETER_SIP_ADDRESS)
        )
        assertEquals("42%40example.invalid", queries.first().lastPathSegment)
    }

    @Test
    fun invoke_withANumericSipUsernameAndNoSipMatch_triesThePhoneLookup() {
        stub(found = false)

        lookup("+12025550186@example.invalid")

        assertEquals(2, queries.size)
        assertEquals("+12025550186", queries.last().lastPathSegment)
        assertEquals(
            "false",
            queries.last().getQueryParameter(PhoneLookup.QUERY_PARAMETER_SIP_ADDRESS)
        )
    }

    @Test
    fun invoke_withANonNumericSipUsernameAndNoMatch_doesNotTryAPhoneLookup() {
        stub(found = false)

        assertEquals(ContactLookupResult.None, lookup("reviewer42@example.invalid"))
        assertEquals(1, queries.size)
    }

    @Test
    fun invoke_whenTheSipProviderReturnsNull_doesNotTreatFailureAsNoMatch() {
        every { resolver.query(capture(queries), any(), any<String>(), any(), null) } returns null

        assertEquals(ContactLookupResult.Unavailable, lookup("42@example.invalid"))
        assertEquals(1, queries.size)
    }

    @Test
    fun invoke_whenSupplementaryMetadataExists_keepsTheMatchedNameAndPresence() {
        stub(found = true)
        every {
            resolver.query(any(), eq(arrayOf(Contacts.DISPLAY_NAME_ALTERNATIVE)), null, null, null)
        } returns MatrixCursor(arrayOf(Contacts.DISPLAY_NAME_ALTERNATIVE)).apply {
            addRow(arrayOf("Lovelace, Ada"))
        }
        every {
            resolver.query(
                any(),
                eq(arrayOf(Phone.CARRIER_PRESENCE)),
                "${Phone.CONTACT_ID} = ?",
                eq(arrayOf("42")),
                null,
            )
        } returns MatrixCursor(arrayOf(Phone.CARRIER_PRESENCE)).apply {
            addRow(listOf(Phone.CARRIER_PRESENCE_VT_CAPABLE))
        }

        val found = lookup("+12025550186") as ContactLookupResult.Found

        assertEquals("Lovelace, Ada", found.alternativeName)
        assertEquals(Phone.CARRIER_PRESENCE_VT_CAPABLE, found.carrierPresence)
    }

    @Test
    fun invoke_withAnEnterpriseContact_doesNotQueryPersonalSupplementaryData() {
        stub(found = true, contactId = ENTERPRISE_CONTACT_ID)

        assertTrue(lookup("+12025550186") is ContactLookupResult.Found)
        assertEquals(1, queries.size)
    }

    private fun stub(found: Boolean, contactId: Long = 42L) {
        every { resolver.query(capture(queries), any(), any<String>(), any(), null) } answers {
            val uri = firstArg<Uri>()
            when {
                uri.path?.startsWith("/phone_lookup") == true -> MatrixCursor(
                    ContactLookupImpl.PHONE_LOOKUP_PROJECTION,
                ).apply {
                    if (found) {
                        addRow(
                            arrayOf<Any?>(contactId, "Ada", null, "key", Phone.TYPE_MOBILE, null)
                        )
                    }
                }
                else -> null
            }
        }
    }
    private companion object {
        const val ENTERPRISE_CONTACT_ID = 1_000_000_000L
    }
}
