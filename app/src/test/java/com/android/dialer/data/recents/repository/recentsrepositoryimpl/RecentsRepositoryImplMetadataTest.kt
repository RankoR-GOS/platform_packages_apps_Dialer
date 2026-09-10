package com.android.dialer.data.recents.repository.recentsrepositoryimpl

import android.os.Build
import android.provider.CallLog.Calls
import app.cash.turbine.test
import com.android.dialer.data.recents.account.PhoneAccountSnapshot
import com.android.dialer.data.recents.contact.ContactLookupResult
import com.android.dialer.telecom.TelecomUtil
import com.android.dialer.testutil.callLogRow
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsRepositoryImplMetadataTest : BaseRecentsRepositoryImplTest() {

    @Test
    fun observeSnapshot_withCallMetadata_readsItFromTheProvider() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            stubCallLogQuery(
                rows = listOf(
                    callLogRow(id = 1L).copy(
                        accountComponentName = "example/.PhoneService",
                        accountId = "sim1",
                        postDialDigits = ",12;34",
                        viaNumber = "+12025550187",
                    )
                )
            )
            stubObserverRegistration()

            val entry = createRepository().observeSnapshot().first().entries.single()

            assertEquals("example/.PhoneService", entry.accountComponentName)
            assertEquals("sim1", entry.accountId)
            assertEquals(",12;34", entry.postDialDigits)
            assertEquals("+12025550187", entry.viaNumber)
            assertTrue(
                capturedProjections.single().orEmpty().toList().containsAll(
                    listOf(
                        Calls.PHONE_ACCOUNT_COMPONENT_NAME,
                        Calls.PHONE_ACCOUNT_ID,
                        Calls.POST_DIAL_DIGITS,
                        Calls.VIA_NUMBER,
                    )
                )
            )
        }
    }

    @Test
    fun observeSnapshot_withNullMetadata_preservesNullAccountsAndUsesEmptyDialingStrings() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            stubCallLogQuery(rows = listOf(callLogRow(id = 1L)))
            stubObserverRegistration()

            val entry = createRepository().observeSnapshot().first().entries.single()

            assertNull(entry.accountComponentName)
            assertNull(entry.accountId)
            assertEquals("", entry.postDialDigits)
            assertEquals("", entry.viaNumber)
        }
    }

    @Test
    fun observeSnapshot_withContactAndAccountMetadata_preservesItAcrossCachedRefreshes() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            val row = callLogRow(id = 1L).copy(
                accountComponentName = "example/.Service",
                accountId = "sim2",
            )
            stubCallLogQuery(rows = listOf(row))
            stubObserverRegistration()
            val handle =
                requireNotNull(TelecomUtil.composePhoneAccountHandle("example/.Service", "sim2"))
            every { phoneAccountLookup() } returns
                PhoneAccountSnapshot(mapOf(handle to "Work"), true)
            every { contactLookup(any()) } returns ContactLookupResult.Found(
                "Ada Lovelace",
                null,
                "content://contacts/lookup/42",
                2,
                null,
                alternativeName = "Lovelace, Ada",
                carrierPresence = 1,
            )
            val repository = createRepository(isContactsGranted = true)

            repository.observeSnapshot().test {
                assertEquals("Work", awaitItem().entries.single().accountLabel)
                val enriched = awaitItem().entries.single()
                assertEquals("Lovelace, Ada", enriched.alternativeName)
                assertEquals(1, enriched.carrierPresence)
                assertTrue(enriched.supportsVideoPresence)
                every { phoneAccountLookup() } returns PhoneAccountSnapshot()
                repository.refresh()
                val refreshed = awaitItem().entries.single()
                assertEquals("Lovelace, Ada", refreshed.alternativeName)
                assertNull(refreshed.accountLabel)
                verify(exactly = 1) { contactLookup(any()) }
                verify(exactly = 2) { phoneAccountLookup() }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun observeSnapshot_withAVoicemailNumber_resolvesItsAccountAndSkipsContactLookup() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            val row = callLogRow(id = 1L, number = "123").copy(
                accountComponentName = "example/.Service",
                accountId = "sim2",
            )
            val handle = TelecomUtil.composePhoneAccountHandle("example/.Service", "sim2")
            every { phoneAccountLookup.isVoicemailNumber(handle, "123") } returns true
            stubCallLogQuery(rows = listOf(row))
            stubObserverRegistration()
            val repository = createRepository(isContactsGranted = true)

            repository.observeSnapshot().test {
                assertTrue(awaitItem().entries.single().isVoicemailNumber)
                repository.refresh()
                assertTrue(awaitItem().entries.single().isVoicemailNumber)
                cancelAndIgnoreRemainingEvents()
            }

            verify(exactly = 2) { phoneAccountLookup.isVoicemailNumber(handle, "123") }
            verify(exactly = 0) { contactLookup(any()) }
        }
    }

    @Test
    fun observeSnapshot_withAVoicemailNumberAndNoContactsPermission_keepsVoicemailMetadata() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            every { phoneAccountLookup.isVoicemailNumber(null, "123") } returns true
            stubCallLogQuery(rows = listOf(callLogRow(id = 1L, number = "123")))
            stubObserverRegistration()

            val snapshot = createRepository().observeSnapshot().first()

            assertTrue(snapshot.entries.single().isVoicemailNumber)
        }
    }

    @Test
    fun observeSnapshot_withNoRows_doesNotLookUpAccountsOrVoicemail() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            stubCallLogQuery(rows = emptyList())
            stubObserverRegistration()

            assertTrue(createRepository().observeSnapshot().first().entries.isEmpty())

            verify(exactly = 0) { phoneAccountLookup() }
            verify(exactly = 0) { phoneAccountLookup.isVoicemailNumber(any(), any()) }
        }
    }
}
