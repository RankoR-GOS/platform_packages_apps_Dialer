package com.android.dialer.data.recents.repository.recentsrepositoryimpl

import android.os.Build
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import app.cash.turbine.test
import com.android.dialer.data.recents.contact.ContactLookupResult
import com.android.dialer.data.recents.model.CallLogFilter
import com.android.dialer.testutil.TestCallLogRow
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
                    assertEquals(Phone.TYPE_MOBILE, enriched.numberType)
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
    fun observeSnapshot_whenAContactChangesWhileUnobserved_looksItUpOnResubscription() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubCallLogQuery(rows = listOf(unnamedRow(id = 1L)))
            stubObserverRegistration()
            every { contactLookup(NUMBER) } returns ADA
            val snapshots = createRepository(isContactsGranted = true)
                .observeSnapshot(filter = CallLogFilter.All)

            snapshots.test {
                awaitItem()
                assertEquals("Ada Lovelace", awaitItem().entries.single().cachedName)
                cancelAndIgnoreRemainingEvents()
            }

            every { contactLookup(NUMBER) } returns ADA.copy(name = "Grace Hopper")

            snapshots.test {
                advanceUntilIdle()
                assertEquals("Grace Hopper", expectMostRecentItem().entries.single().cachedName)
                cancelAndIgnoreRemainingEvents()
            }

            verify(exactly = 2) { contactLookup(NUMBER) }
        }
    }

    @Test
    fun observeSnapshot_whenAContactIsAddedWhileUnobserved_forgetsTheCachedMiss() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubCallLogQuery(rows = listOf(unnamedRow(id = 1L)))
            stubObserverRegistration()
            every { contactLookup(NUMBER) } returns ContactLookupResult.None
            val snapshots = createRepository(isContactsGranted = true)
                .observeSnapshot(filter = CallLogFilter.All)

            snapshots.test {
                assertNull(awaitItem().entries.single().cachedName)
                cancelAndIgnoreRemainingEvents()
            }

            every { contactLookup(NUMBER) } returns ADA

            snapshots.test {
                advanceUntilIdle()
                assertEquals("Ada Lovelace", expectMostRecentItem().entries.single().cachedName)
                cancelAndIgnoreRemainingEvents()
            }

            verify(exactly = 2) { contactLookup(NUMBER) }
        }
    }

    @Test
    fun observeSnapshot_whenEveryNumberIsCached_emitsOnlyTheEnrichedSnapshot() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubCallLogQuery(
                rows = listOf(unnamedRow(id = 2L), unnamedRow(id = 1L, number = OTHER)),
            )
            stubObserverRegistration()
            every { contactLookup(NUMBER) } returns ADA
            every { contactLookup(OTHER) } returns ContactLookupResult.None
            val repository = createRepository(isContactsGranted = true)

            repository.observeSnapshot(filter = CallLogFilter.All).test {
                assertNull(awaitItem().entries.first().cachedName)
                assertEquals("Ada Lovelace", awaitItem().entries.first().cachedName)

                repository.refresh()

                assertEquals("Ada Lovelace", awaitItem().entries.first().cachedName)
                advanceUntilIdle()
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun observeSnapshot_whenACachedSnapshotGainsNoNames_stillEmitsItOnce() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubCallLogQuery(rows = listOf(unnamedRow(id = 2L), unnamedRow(id = 1L)))
            stubObserverRegistration()
            every { contactLookup(NUMBER) } returns ContactLookupResult.None
            val repository = createRepository(isContactsGranted = true)

            repository.observeSnapshot(filter = CallLogFilter.All).test {
                assertEquals(2, awaitItem().entries.size)

                stubCallLogQuery(rows = listOf(unnamedRow(id = 2L)))
                repository.refresh()

                assertEquals(1, awaitItem().entries.size)
                advanceUntilIdle()
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun observeSnapshot_whenARowLeavesTheSnapshot_forgetsItsContactAndAsksAgainWhenItReturns() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubCallLogQuery(
                rows = listOf(unnamedRow(id = 2L), unnamedRow(id = 1L, number = OTHER)),
            )
            stubObserverRegistration()
            every { contactLookup(NUMBER) } returns ADA
            every { contactLookup(OTHER) } returns ContactLookupResult.None
            val repository = createRepository(isContactsGranted = true)

            repository.observeSnapshot(filter = CallLogFilter.All).test {
                awaitItem()
                awaitItem()

                stubCallLogQuery(rows = listOf(unnamedRow(id = 2L)))
                repository.refresh()
                awaitItem()

                stubCallLogQuery(
                    rows = listOf(unnamedRow(id = 2L), unnamedRow(id = 1L, number = OTHER)),
                )
                repository.refresh()
                awaitItem()
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            verify(exactly = 1) { contactLookup(NUMBER) }
            verify(exactly = 2) { contactLookup(OTHER) }
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

    @Test
    fun observeSnapshot_whenTheContactIsGone_dropsTheCachedColumns() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubCallLogQuery(
                rows = listOf(
                    callLogRow(
                        id = 1L,
                        number = NUMBER,
                        cachedName = "Old",
                        cachedPhotoUri = "content://com.android.contacts/contacts/9/photo",
                        cachedLookupUri = "content://com.android.contacts/contacts/lookup/k9/9",
                    ),
                ),
            )
            stubObserverRegistration()
            every { contactLookup(NUMBER) } returns ContactLookupResult.None

            createRepository(isContactsGranted = true)
                .observeSnapshot(filter = CallLogFilter.All)
                .test {
                    assertEquals("Old", awaitItem().entries.single().cachedName)

                    val forgotten = awaitItem().entries.single()

                    assertNull(forgotten.cachedName)
                    assertNull(forgotten.photoUri)
                    assertNull(forgotten.lookupUri)
                    assertEquals(Phone.TYPE_CUSTOM, forgotten.numberType)
                    cancelAndIgnoreRemainingEvents()
                }
        }
    }

    @Test
    fun observeSnapshot_whenTheLookupIsUnavailable_keepsTheCachedColumnsAndAsksAgain() {
        runTest(
            context = mainDispatcherRule.testDispatcher,
        ) {
            stubCallLogQuery(
                rows = listOf(callLogRow(id = 1L, number = NUMBER, cachedName = "Old")),
            )
            stubObserverRegistration()
            every { contactLookup(NUMBER) } returns ContactLookupResult.Unavailable
            val repository = createRepository(isContactsGranted = true)

            repository.observeSnapshot(filter = CallLogFilter.All).test {
                assertEquals("Old", awaitItem().entries.single().cachedName)

                repository.refresh()

                assertEquals("Old", awaitItem().entries.single().cachedName)
                advanceUntilIdle()
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }

            verify(exactly = 2) { contactLookup(NUMBER) }
        }
    }

    private fun unnamedRow(id: Long, number: String = NUMBER): TestCallLogRow {
        return callLogRow(id = id, number = number, cachedName = null)
    }

    private companion object {
        const val NUMBER = "+18765550201"
        const val OTHER = "+18765550202"
        val ADA = ContactLookupResult.Found(
            name = "Ada Lovelace",
            photoUri = "content://com.android.contacts/contacts/42/photo",
            lookupUri = "content://com.android.contacts/contacts/lookup/k42/42",
            numberType = Phone.TYPE_MOBILE,
            numberLabel = null,
        )
    }
}
