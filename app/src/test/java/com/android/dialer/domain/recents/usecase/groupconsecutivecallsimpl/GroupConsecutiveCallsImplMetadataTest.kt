package com.android.dialer.domain.recents.usecase.groupconsecutivecallsimpl

import android.os.Build
import com.android.dialer.data.recents.model.CallLogEntry
import com.android.dialer.domain.recents.usecase.GroupConsecutiveCallsImpl
import com.android.dialer.testutil.callLogEntry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class GroupConsecutiveCallsImplMetadataTest {

    private val groupConsecutiveCalls = GroupConsecutiveCallsImpl()
    private val newest = callLogEntry(id = 3L, number = "+12025550186").copy(
        accountComponentName = "example/.PhoneService",
        accountId = "sim1",
        postDialDigits = ",12;34",
        viaNumber = "+12025550187",
    )

    @Test
    fun invoke_withDifferentAccounts_keepsEachCallsDeleteIdsSeparate() {
        assertSeparate(older = newest.copy(accountId = "sim2"))
    }

    @Test
    fun invoke_withDifferentAccountComponents_keepsEachCallsDeleteIdsSeparate() {
        assertSeparate(older = newest.copy(accountComponentName = "other/.PhoneService"))
    }

    @Test
    fun invoke_withDifferentPostDialDigits_keepsEachCallsDeleteIdsSeparate() {
        assertSeparate(older = newest.copy(postDialDigits = ",56;78"))
    }

    @Test
    fun invoke_withDifferentViaNumbers_keepsEachCallsDeleteIdsSeparate() {
        assertSeparate(older = newest.copy(viaNumber = "+12025550188"))
    }

    @Test
    fun invoke_withNullAndEmptyAccounts_keepsTheCallsSeparate() {
        val entries = listOf(
            newest.copy(accountId = null),
            newest.copy(entryId = callLogEntry(id = 2L).entryId, accountId = ""),
        )

        assertEquals(2, groupConsecutiveCalls(entries).size)
    }

    @Test
    fun invoke_withIdenticalMetadata_keepsTheNewestMetadataAndGroupsBothIds() {
        val older = newest.copy(entryId = callLogEntry(id = 2L).entryId)

        val grouped = groupConsecutiveCalls(listOf(newest, older))

        assertEquals(1, grouped.size)
        assertEquals(
            newest.copy(groupedEntryIds = grouped.single().groupedEntryIds),
            grouped.single()
        )
        assertEquals(listOf(3L, 2L), grouped.single().groupedEntryIds.map { it.value })
    }

    private fun assertSeparate(older: CallLogEntry) {
        val entries = listOf(newest, older.copy(entryId = callLogEntry(id = 2L).entryId))

        val grouped = groupConsecutiveCalls(entries)

        assertEquals(
            listOf(listOf(3L), listOf(2L)),
            grouped.map { row ->
                row.groupedEntryIds.map { it.value }
            }
        )
    }
}
