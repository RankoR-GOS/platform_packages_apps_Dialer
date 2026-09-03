package com.android.dialer.data.contacts.repository

import android.os.Build
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import app.cash.turbine.test
import com.android.dialer.data.contacts.model.Contact
import com.android.dialer.data.contacts.model.ContactNameOrder
import com.android.dialer.data.contacts.model.ContactsQuery
import com.android.dialer.data.contacts.repository.FakeContactsProvider.Companion.cursorOf
import com.android.dialer.data.contacts.repository.FakeContactsProvider.Companion.row
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
class ContactsRepositoryTest {

    private class FakeNameOrderSource : ContactNameOrderSource {
        var display = ContactNameOrder.PRIMARY
        var sort = ContactNameOrder.PRIMARY

        override fun displayOrder(): ContactNameOrder = display

        override fun sortOrder(): ContactNameOrder = sort
    }

    private val provider = FakeContactsProvider()
    private val nameOrderSource = FakeNameOrderSource()
    private val contentResolver
        get() = RuntimeEnvironment.getApplication().contentResolver

    private lateinit var repository: ContactsRepository

    @Before
    fun setUp() {
        ShadowContentResolver.registerProviderInternal(ContactsContract.AUTHORITY, provider)
        repository = ContactsRepositoryImpl(
            contentResolver = contentResolver,
            nameOrderSource = nameOrderSource,
            ioDispatcher = UnconfinedTestDispatcher(),
        )
    }

    private suspend fun recordedQuery(
        query: ContactsQuery = ContactsQuery(),
    ): FakeContactsProvider.QueryArgs {
        repository.observeContacts(query = query).first()

        return provider.queries.single()
    }

    // --- what gets asked of the provider -------------------------------------------------

    @Test
    fun asksForThePlainContentUriWhenThereIsNoFilter() = runTest {
        val args = recordedQuery(ContactsQuery(filter = ""))

        assertEquals(Contacts.CONTENT_URI.path, args.uri.path)
        assertEquals(
            "true",
            args.uri.getQueryParameter(Contacts.EXTRA_ADDRESS_BOOK_INDEX),
        )
    }

    @Test
    fun asksForTheFilterUriWhenThereIsAFilter() = runTest {
        val args = recordedQuery(ContactsQuery(filter = "ada"))

        assertTrue(
            "expected a filter uri, got ${args.uri}",
            args.uri.toString().startsWith(Contacts.CONTENT_FILTER_URI.toString()),
        )
        assertEquals("ada", args.uri.pathSegments.last())
        assertEquals("true", args.uri.getQueryParameter(Contacts.EXTRA_ADDRESS_BOOK_INDEX))
    }

    @Test
    fun projectsAndSortsByPrimaryNameByDefault() = runTest {
        val args = recordedQuery()

        assertEquals(Contacts.DISPLAY_NAME_PRIMARY, args.projection[1])
        assertEquals("${Contacts.SORT_KEY_PRIMARY} ASC", args.sortOrder)
        assertEquals("${Contacts.DISPLAY_NAME_PRIMARY} IS NOT NULL", args.selection)
    }

    @Test
    fun projectsAndSortsByAlternativeNameWhenPreferred() = runTest {
        nameOrderSource.display = ContactNameOrder.ALTERNATIVE
        nameOrderSource.sort = ContactNameOrder.ALTERNATIVE

        val args = recordedQuery()

        assertEquals(Contacts.DISPLAY_NAME_ALTERNATIVE, args.projection[1])
        assertEquals("${Contacts.SORT_KEY_ALTERNATIVE} ASC", args.sortOrder)
        assertEquals("${Contacts.DISPLAY_NAME_ALTERNATIVE} IS NOT NULL", args.selection)
    }

    @Test
    fun letsDisplayOrderAndSortOrderDiffer() = runTest {
        nameOrderSource.display = ContactNameOrder.PRIMARY
        nameOrderSource.sort = ContactNameOrder.ALTERNATIVE

        val args = recordedQuery()

        assertEquals(Contacts.DISPLAY_NAME_PRIMARY, args.projection[1])
        assertEquals("${Contacts.SORT_KEY_ALTERNATIVE} ASC", args.sortOrder)
    }

    @Test
    fun asksOnlyForContactsWithPhoneNumbersWhenRequired() = runTest {
        val args = recordedQuery(ContactsQuery(requirePhoneNumber = true))

        assertEquals(
            "${Contacts.DISPLAY_NAME_PRIMARY} IS NOT NULL AND ${Contacts.HAS_PHONE_NUMBER}=1",
            args.selection,
        )
    }

    @Test
    fun requestsEveryColumnThePhotoLoaderAndContactUriNeed() = runTest {
        val args = recordedQuery()

        assertEquals(
            listOf(
                Contacts._ID,
                Contacts.DISPLAY_NAME_PRIMARY,
                Contacts.PHOTO_ID,
                Contacts.PHOTO_THUMBNAIL_URI,
                Contacts.LOOKUP_KEY,
            ),
            args.projection,
        )
    }

    // --- what comes back -----------------------------------------------------------------

    @Test
    fun mapsCursorRowsToContacts() = runTest {
        provider.result = cursorOf(
            rows = listOf(
                row(id = 1L, displayName = "Ada", photoId = 7L, photoUri = "content://photo/1"),
                row(id = 2L, displayName = "Grace"),
            ),
        )

        repository.observeContacts(query = ContactsQuery()).test {
            val snapshot = awaitItem()

            assertEquals(
                listOf(
                    Contact(
                        id = 1L,
                        lookupKey = "lookup-1",
                        displayName = "Ada",
                        photoId = 7L,
                        photoUri = "content://photo/1",
                    ),
                    Contact(
                        id = 2L,
                        lookupKey = "lookup-2",
                        displayName = "Grace",
                        photoId = 0L,
                        photoUri = null,
                    ),
                ),
                snapshot.contacts,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun treatsABlankPhotoUriAsNoPhoto() = runTest {
        provider.result = cursorOf(rows = listOf(row(id = 1L, displayName = "Ada", photoUri = "")))

        repository.observeContacts(query = ContactsQuery()).test {
            assertEquals(null, awaitItem().contacts.single().photoUri)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dropsRowsWithoutAUsableDisplayName() = runTest {
        provider.result = cursorOf(
            rows = listOf(
                row(id = 1L, displayName = "Ada"),
                row(id = 2L, displayName = null),
                row(id = 3L, displayName = "   "),
                row(id = 4L, displayName = "Grace"),
            ),
        )

        repository.observeContacts(query = ContactsQuery()).test {
            assertEquals(listOf("Ada", "Grace"), awaitItem().contacts.map(Contact::displayName))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun readsTheAddressBookIndexFromCursorExtras() = runTest {
        provider.result = cursorOf(
            rows = listOf(row(id = 1L, displayName = "Ada"), row(id = 2L, displayName = "Grace")),
            titles = arrayOf("A", "G"),
            counts = intArrayOf(1, 1),
        )

        repository.observeContacts(query = ContactsQuery()).test {
            val index = awaitItem().index

            assertEquals(listOf("A", "G"), index.titles)
            assertEquals(listOf(1, 1), index.counts)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun fallsBackToAnEmptyIndexWhenTheProviderSendsNoExtras() = runTest {
        provider.result = cursorOf(rows = listOf(row(id = 1L, displayName = "Ada")))

        repository.observeContacts(query = ContactsQuery()).test {
            val index = awaitItem().index

            assertEquals(emptyList<String>(), index.titles)
            assertEquals(emptyList<Int>(), index.counts)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun truncatesTheIndexWhenTitlesAndCountsDisagree() = runTest {
        provider.result = cursorOf(
            rows = listOf(row(id = 1L, displayName = "Ada")),
            titles = arrayOf("A", "B", "C"),
            counts = intArrayOf(1),
        )

        repository.observeContacts(query = ContactsQuery()).test {
            val index = awaitItem().index

            assertEquals(listOf("A"), index.titles)
            assertEquals(listOf(1), index.counts)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun emitsAnEmptySnapshotWhenTheProviderRefusesTheQuery() = runTest {
        provider.result = null

        repository.observeContacts(query = ContactsQuery()).test {
            val snapshot = awaitItem()

            assertEquals(emptyList<Contact>(), snapshot.contacts)
            assertEquals(emptyList<String>(), snapshot.index.titles)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- staying live --------------------------------------------------------------------

    @Test
    fun reQueriesWhenTheProviderReportsAChange() = runTest {
        provider.result = cursorOf(rows = listOf(row(id = 1L, displayName = "Ada")))

        repository.observeContacts(query = ContactsQuery()).test {
            assertEquals(listOf("Ada"), awaitItem().contacts.map(Contact::displayName))

            provider.result = cursorOf(
                rows = listOf(
                    row(id = 1L, displayName = "Ada"),
                    row(id = 2L, displayName = "Grace")
                ),
            )
            contentResolver.notifyChange(Contacts.CONTENT_URI, null)

            assertEquals(listOf("Ada", "Grace"), awaitItem().contacts.map(Contact::displayName))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun unregistersTheObserverWhenCollectionStops() = runTest {
        repository.observeContacts(query = ContactsQuery()).test {
            awaitItem()
            assertTrue(
                "expected an observer while collecting",
                observerCount() > 0,
            )
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(0, observerCount())
    }

    private fun observerCount(): Int =
        shadowOf(contentResolver).getContentObservers(Contacts.CONTENT_URI).size
}
