package com.android.dialer.domain.recents.usecase.groupconsecutivecallsimpl

import android.os.Build
import android.provider.CallLog.Calls
import com.android.dialer.domain.recents.usecase.GroupConsecutiveCallsImpl
import com.android.dialer.testutil.callLogEntry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class GroupConsecutiveCallsImplNumberMatchingTest {

    private val groupConsecutiveCalls = GroupConsecutiveCallsImpl()

    @Test
    fun invoke_withNumbersThatDifferOnlyInFormatting_groupsThem() {
        val entries = listOf(
            callLogEntry(id = 2L, number = "+1 555-123-4567"),
            callLogEntry(id = 1L, number = "(555) 123-4567"),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(1, grouped.size)
        assertEquals(2, grouped.first().groupedCallCount)
    }

    @Test
    fun invoke_withNumbersThatDifferByASpecialCharacter_keepsThemInSeparateEntries() {
        val entries = listOf(
            callLogEntry(id = 2L, number = "#5551234567"),
            callLogEntry(id = 1L, number = "5551234567"),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(2, grouped.size)
    }

    @Test
    fun invoke_withAPlainNumberBesideASipAddress_keepsThemInSeparateEntries() {
        val entries = listOf(
            callLogEntry(id = 2L, number = "5551234567"),
            callLogEntry(id = 1L, number = "jane@example.com"),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(2, grouped.size)
    }

    @Test
    fun invoke_withSipAddresses_groupsOnTheHostIgnoringItsCase() {
        val entries = listOf(
            callLogEntry(id = 2L, number = "jane@Example.COM"),
            callLogEntry(id = 1L, number = "jane@example.com"),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(1, grouped.size)
        assertEquals(2, grouped.first().groupedCallCount)
        assertEquals(
            2,
            groupConsecutiveCalls(
                entries = listOf(
                    callLogEntry(id = 2L, number = "jane@example.com"),
                    callLogEntry(id = 1L, number = "jane@other.example"),
                ),
            ).size,
        )
    }

    @Test
    fun invoke_withSipAddressesThatDifferInUserInfoCase_keepsThemInSeparateEntries() {
        val entries = listOf(
            callLogEntry(id = 2L, number = "Jane@example.com"),
            callLogEntry(id = 1L, number = "jane@example.com"),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(2, grouped.size)
    }

    @Test
    fun invoke_whenBothNumbersAreBlank_keepsTheCallsInSeparateEntries() {
        val entries = listOf(
            callLogEntry(id = 2L, number = "", numberPresentation = Calls.PRESENTATION_RESTRICTED),
            callLogEntry(id = 1L, number = "", numberPresentation = Calls.PRESENTATION_RESTRICTED),
        )

        val grouped = groupConsecutiveCalls(entries = entries)

        assertEquals(2, grouped.size)
    }
}
