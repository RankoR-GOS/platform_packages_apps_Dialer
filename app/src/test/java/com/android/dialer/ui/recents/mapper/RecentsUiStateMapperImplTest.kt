package com.android.dialer.ui.recents.mapper

import android.content.Context
import com.android.dialer.R
import com.android.dialer.data.recents.model.CallLogEntry
import com.android.dialer.domain.recents.usecase.GroupConsecutiveCalls
import com.android.dialer.testutil.TEST_TIMESTAMP_MILLIS
import com.android.dialer.testutil.callLogEntry
import com.android.dialer.testutil.callLogSnapshot
import com.android.dialer.testutil.recentsItemUiModel
import com.android.dialer.ui.recents.model.RecentsContentUiState
import com.android.dialer.ui.recents.model.RecentsListItemUiModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.TimeZone
import kotlinx.collections.immutable.toImmutableList
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class RecentsUiStateMapperImplTest {

    private val context = mockk<Context>()
    private val groupConsecutiveCalls = mockk<GroupConsecutiveCalls>()
    private val itemUiMapper = mockk<RecentsItemUiMapper>()
    private val defaultTimeZone = TimeZone.getDefault()

    @Before
    fun setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        every { context.getString(any()) } answers { "string-${firstArg<Int>()}" }
        every { groupConsecutiveCalls(any()) } answers {
            firstArg<List<CallLogEntry>>().toImmutableList()
        }
        every { itemUiMapper.map(any(), any()) } answers {
            recentsItemUiModel(id = firstArg<CallLogEntry>().entryId.value)
        }
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(defaultTimeZone)
    }

    @Test
    fun map_withoutPermission_returnsPermissionRequiredWithResolvedCopy() {
        val state = createMapper().map(callLogSnapshot(isPermissionGranted = false), NOW_MILLIS)

        assertEquals(
            RecentsContentUiState.PermissionRequired(
                message = "string-${R.string.new_call_log_permission_no_calllog}",
                actionLabel = "string-${R.string.permission_single_turn_on}",
            ),
            state.content,
        )
    }

    @Test
    fun map_withNoEntries_returnsEmptyWithResolvedCopy() {
        val state = createMapper().map(callLogSnapshot(), NOW_MILLIS)

        assertEquals(
            RecentsContentUiState.Empty(message = "string-${R.string.call_log_all_empty}"),
            state.content,
        )
    }

    @Test
    fun map_withEntries_groupsThemThenMapsEachWithTheCurrentTime() {
        val entries = listOf(
            callLogEntry(id = 2L, timestampMillis = NOW_MILLIS),
            callLogEntry(id = 1L, timestampMillis = NOW_MILLIS - HOUR_MILLIS),
        )
        val grouped = listOf(entries.first().copy(groupedCallCount = 2)).toImmutableList()
        every { groupConsecutiveCalls(entries) } returns grouped

        val state = createMapper().map(callLogSnapshot(*entries.toTypedArray()), NOW_MILLIS)

        val items = (state.content as RecentsContentUiState.Entries).items
        assertEquals(listOf(2L), items.entryIds())
        verify(exactly = 1) { itemUiMapper.map(grouped.single(), NOW_MILLIS) }
    }

    @Test
    fun map_withEntriesAcrossThreeDays_insertsOneHeaderPerDayInOrder() {
        val snapshot = callLogSnapshot(
            callLogEntry(id = 4L, timestampMillis = NOW_MILLIS),
            callLogEntry(id = 3L, timestampMillis = NOW_MILLIS - HOUR_MILLIS),
            callLogEntry(id = 2L, timestampMillis = NOW_MILLIS - DAY_MILLIS),
            callLogEntry(id = 1L, timestampMillis = NOW_MILLIS - THREE_DAYS_MILLIS),
        )

        val state = createMapper().map(snapshot, NOW_MILLIS)

        val items = (state.content as RecentsContentUiState.Entries).items
        assertEquals(
            listOf(
                "string-${R.string.call_log_header_today}",
                "string-${R.string.call_log_header_yesterday}",
                "string-${R.string.call_log_header_other}",
            ),
            items.filterIsInstance<RecentsListItemUiModel.DayHeader>().map { it.label },
        )
        assertEquals(listOf("Today", "Yesterday", "Older"), items.headerKeys())
        assertEquals(listOf(4L, 3L, 2L, 1L), items.entryIds())
        assertEquals(RecentsListItemUiModel.DayHeader::class, items[0]::class)
        assertEquals(RecentsListItemUiModel.DayHeader::class, items[3]::class)
        assertEquals(RecentsListItemUiModel.DayHeader::class, items[5]::class)
    }

    @Test
    fun map_resolvesTheSheetLabelsFromTheLegacyStrings() {
        val labels = createMapper().map(callLogSnapshot(), NOW_MILLIS).actionLabels

        assertEquals("string-${R.string.voice_call}", labels.call)
        assertEquals("string-${R.string.video_call}", labels.videoCall)
        assertEquals("string-${R.string.send_a_message}", labels.message)
        assertEquals("string-${R.string.add_to_contacts}", labels.addContact)
        assertEquals("string-${R.string.block_number}", labels.block)
        assertEquals("string-${R.string.copy_number}", labels.copyNumber)
        assertEquals("string-${R.string.call_details_menu_label}", labels.callDetails)
        assertEquals("string-${R.string.delete}", labels.delete)
    }

    @Test
    fun map_withAFutureTimestamp_filesItUnderToday() {
        val snapshot = callLogSnapshot(
            callLogEntry(id = 2L, timestampMillis = NOW_MILLIS + DAY_MILLIS),
            callLogEntry(id = 1L, timestampMillis = NOW_MILLIS),
        )

        val state = createMapper().map(snapshot, NOW_MILLIS)

        val items = (state.content as RecentsContentUiState.Entries).items
        assertEquals(listOf("Today"), items.headerKeys())
    }

    private fun List<RecentsListItemUiModel>.headerKeys(): List<String> {
        return filterIsInstance<RecentsListItemUiModel.DayHeader>().map { it.key }
    }

    private fun List<RecentsListItemUiModel>.entryIds(): List<Long> {
        return filterIsInstance<RecentsListItemUiModel.Entry>().map { it.item.entryId.value }
    }

    private fun createMapper(): RecentsUiStateMapperImpl {
        return RecentsUiStateMapperImpl(
            context = context,
            groupConsecutiveCalls = groupConsecutiveCalls,
            itemUiMapper = itemUiMapper,
        )
    }

    private companion object {
        private const val NOW_MILLIS = TEST_TIMESTAMP_MILLIS
        private const val HOUR_MILLIS = 3_600_000L
        private const val DAY_MILLIS = 86_400_000L
        private const val THREE_DAYS_MILLIS = 3 * DAY_MILLIS
    }
}
