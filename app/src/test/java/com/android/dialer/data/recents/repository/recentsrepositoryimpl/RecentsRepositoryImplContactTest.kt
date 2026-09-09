package com.android.dialer.data.recents.repository.recentsrepositoryimpl

import android.os.Build
import android.provider.CallLog
import android.provider.ContactsContract
import app.cash.turbine.test
import com.android.dialer.data.recents.contact.ContactLookupResult
import com.android.dialer.data.recents.model.CallLogFilter
import com.android.dialer.testutil.callLogRow
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsRepositoryImplContactTest : BaseRecentsRepositoryImplTest() {

    @Test
    fun observeSnapshot_withoutACachedName_paintsTheRowThenFillsItFromTheLookup() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubCallLogQuery(rows = listOf(unnamedRow(id = 1L)))
            stubObserverRegistration()
            every { contactLookup(NUMBER) } returns ADA

            createRepository(isContactsGranted = true)
                .observeSnapshot(filter = CallLogFilter.All)
                .test {
                    assertNull(awaitItem().entries.single().cachedName)

                    val enriched = awaitItem().entries.single()

                    assertEquals("Ada Lovelace", enriched.cachedName)
                    assertEquals(ADA.photoUri, enriched.photoUri)
                    assertEquals(ADA.lookupUri, enriched.lookupUri)
                    cancelAndIgnoreRemainingEvents()
                }
        }
    }

    @Test
    fun observeSnapshot_whenTheLookupDisagreesWithTheCachedName_prefersTheLookup() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubCallLogQuery(
                rows = listOf(callLogRow(id = 1L, number = NUMBER, cachedName = "Old")),
            )
            stubObserverRegistration()
            every { contactLookup(NUMBER) } returns ADA

            createRepository(isContactsGranted = true)
                .observeSnapshot(filter = CallLogFilter.All)
                .test {
                    assertEquals("Old", awaitItem().entries.single().cachedName)
                    assertEquals("Ada Lovelace", awaitItem().entries.single().cachedName)
                    cancelAndIgnoreRemainingEvents()
                }
        }
    }

    @Test
    fun observeSnapshot_looksEachNumberUpOnceAcrossSnapshots() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubCallLogQuery(rows = listOf(unnamedRow(id = 2L), unnamedRow(id = 1L)))
            stubObserverRegistration()
            every { contactLookup(NUMBER) } returns ADA
            val repository = createRepository(isContactsGranted = true)

            repository.observeSnapshot(filter = CallLogFilter.All).test {
                awaitItem()
                awaitItem()

                repository.refresh()

                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            verify(exactly = 1) { contactLookup(NUMBER) }
        }
    }

    @Test
    fun observeSnapshot_whenAContactChanges_dropsTheCacheAndLooksUpAgain() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubCallLogQuery(rows = listOf(unnamedRow(id = 1L)))
            val observers = stubObserverRegistrations()
            every { contactLookup(NUMBER) } returns ADA

            createRepository(isContactsGranted = true)
                .observeSnapshot(filter = CallLogFilter.All)
                .test {
                    awaitItem()
                    awaitItem()

                    observers.getValue(ContactsContract.Contacts.CONTENT_URI).onChange(false)

                    awaitItem()
                    awaitItem()
                    cancelAndIgnoreRemainingEvents()
                }

            verify(exactly = 2) { contactLookup(NUMBER) }
        }
    }

    @Test
    fun observeSnapshot_withoutTheContactsPermission_neitherLooksUpNorObservesContacts() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubCallLogQuery(rows = listOf(unnamedRow(id = 1L)))
            stubObserverRegistration()

            createRepository(isContactsGranted = false)
                .observeSnapshot(filter = CallLogFilter.All)
                .test {
                    assertNull(awaitItem().entries.single().cachedName)
                    advanceUntilIdle()
                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }

            verify(exactly = 0) { contactLookup(any()) }
            verify(exactly = 0) {
                contentResolver.registerContentObserver(
                    ContactsContract.Contacts.CONTENT_URI,
                    true,
                    any(),
                )
            }
            verify(exactly = 1) {
                contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, any())
            }
        }
    }

    private fun unnamedRow(id: Long) = callLogRow(id = id, number = NUMBER, cachedName = null)

    private companion object {
        const val NUMBER = "+18765550201"
        val ADA = ContactLookupResult(
            name = "Ada Lovelace",
            photoUri = "content://com.android.contacts/contacts/42/photo",
            lookupUri = "content://com.android.contacts/contacts/lookup/k42/42",
        )
    }
}
