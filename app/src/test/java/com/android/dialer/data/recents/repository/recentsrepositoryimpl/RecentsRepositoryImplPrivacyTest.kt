package com.android.dialer.data.recents.repository.recentsrepositoryimpl

import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteDiskIOException
import android.database.sqlite.SQLiteFullException
import android.os.Build
import com.android.dialer.data.recents.model.CallLogEntryId
import io.mockk.every
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsRepositoryImplPrivacyTest : BaseRecentsRepositoryImplTest() {

    @Test
    fun observeSnapshot_whenProviderErrorsContainPrivateData_logsOnlyTheFailureCategory() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            stubObserverRegistration()
            errors().forEach { error ->
                ShadowLog.clear()
                stubQueryThrows(error)

                createRepository().observeSnapshot().first()

                assertPrivateDataIsAbsent()
            }
        }
    }

    @Test
    fun delete_whenProviderErrorsContainPrivateData_logsOnlyTheFailureCategory() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            errors().forEach { error ->
                ShadowLog.clear()
                every { contentResolver.delete(any(), any(), any()) } throws error

                createRepository().delete(entryIds = listOf(CallLogEntryId(value = 1L)))

                assertPrivateDataIsAbsent()
            }
        }
    }

    @Test
    fun markRead_whenProviderErrorsContainPrivateData_logsOnlyTheFailureCategory() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            errors().forEach { error ->
                ShadowLog.clear()
                every { contentResolver.update(any(), any(), any(), any()) } throws error

                createRepository().markRead(entryIds = listOf(CallLogEntryId(value = 1L)))

                assertPrivateDataIsAbsent()
            }
        }
    }

    private fun errors(): List<Exception> {
        return listOf(
            SecurityException(PRIVATE_DATA),
            SQLiteDiskIOException(PRIVATE_DATA),
            SQLiteFullException(PRIVATE_DATA),
            SQLiteDatabaseCorruptException(PRIVATE_DATA),
            IllegalArgumentException(PRIVATE_DATA),
        ).onEach { it.initCause(IllegalStateException(PRIVATE_CAUSE)) }
    }

    private fun assertPrivateDataIsAbsent() {
        val logs = ShadowLog.getLogs().map { it.msg }

        assertTrue(logs.any { it.contains("RecentsRepositoryImpl.") })
        assertFalse(logs.any { it.contains(PRIVATE_DATA) || it.contains(PRIVATE_CAUSE) })
    }

    private companion object {
        private const val PRIVATE_DATA = "reviewer42@example.invalid +15550101999"
        private const val PRIVATE_CAUSE = "content://contacts/lookup/private-sentinel"
    }
}
