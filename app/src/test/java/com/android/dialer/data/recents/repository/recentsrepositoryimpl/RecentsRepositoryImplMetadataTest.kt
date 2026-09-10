package com.android.dialer.data.recents.repository.recentsrepositoryimpl

import android.os.Build
import android.provider.CallLog.Calls
import com.android.dialer.testutil.callLogRow
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
}
