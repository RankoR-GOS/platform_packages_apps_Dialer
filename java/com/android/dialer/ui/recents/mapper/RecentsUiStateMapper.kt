package com.android.dialer.ui.recents.mapper

import android.content.Context
import com.android.dialer.R
import com.android.dialer.calllogutils.CallLogDates
import com.android.dialer.data.recents.model.CallLogEntry
import com.android.dialer.data.recents.model.CallLogSnapshot
import com.android.dialer.domain.recents.usecase.GroupConsecutiveCalls
import com.android.dialer.ui.recents.model.RecentsActionLabelsUiModel
import com.android.dialer.ui.recents.model.RecentsContentUiState
import com.android.dialer.ui.recents.model.RecentsListItemUiModel
import com.android.dialer.ui.recents.model.RecentsUiState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList

internal interface RecentsUiStateMapper {
    fun map(snapshot: CallLogSnapshot, nowMillis: Long): RecentsUiState
}

internal class RecentsUiStateMapperImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val groupConsecutiveCalls: GroupConsecutiveCalls,
    private val itemUiMapper: RecentsItemUiMapper,
) : RecentsUiStateMapper {

    override fun map(snapshot: CallLogSnapshot, nowMillis: Long): RecentsUiState {
        return RecentsUiState(
            content = snapshot.toContent(nowMillis = nowMillis),
            actionLabels = actionLabels(),
        )
    }

    private fun CallLogSnapshot.toContent(nowMillis: Long): RecentsContentUiState {
        return when {
            !isPermissionGranted -> RecentsContentUiState.PermissionRequired(
                message = context.getString(R.string.new_call_log_permission_no_calllog),
                actionLabel = context.getString(R.string.permission_single_turn_on),
            )

            entries.isEmpty() -> RecentsContentUiState.Empty(
                message = context.getString(R.string.call_log_all_empty),
            )

            else -> RecentsContentUiState.Entries(
                items = listItems(entries = groupConsecutiveCalls(entries), nowMillis = nowMillis),
            )
        }
    }

    private fun listItems(entries: List<CallLogEntry>, nowMillis: Long) = buildList {
        var currentDay: Day? = null

        entries.forEach { entry ->
            val day = entry.day(nowMillis = nowMillis)

            if (day != currentDay) {
                add(RecentsListItemUiModel.DayHeader(key = day.name, label = day.label()))
                currentDay = day
            }

            add(RecentsListItemUiModel.Entry(item = itemUiMapper.map(entry, nowMillis)))
        }
    }.toImmutableList()

    private fun CallLogEntry.day(nowMillis: Long): Day {
        return when {
            timestampMillis > nowMillis -> Day.Today
            else -> when (CallLogDates.getDayDifference(nowMillis, timestampMillis)) {
                0 -> Day.Today
                1 -> Day.Yesterday
                else -> Day.Older
            }
        }
    }

    private fun actionLabels(): RecentsActionLabelsUiModel {
        return RecentsActionLabelsUiModel(
            call = context.getString(R.string.voice_call),
            videoCall = context.getString(R.string.video_call),
            message = context.getString(R.string.send_a_message),
            addContact = context.getString(R.string.add_to_contacts),
            block = context.getString(R.string.block_number),
            copyNumber = context.getString(R.string.copy_number),
            callDetails = context.getString(R.string.call_details_menu_label),
            delete = context.getString(R.string.delete),
        )
    }

    private fun Day.label(): String {
        val resId = when (this) {
            Day.Today -> R.string.call_log_header_today
            Day.Yesterday -> R.string.call_log_header_yesterday
            Day.Older -> R.string.call_log_header_other
        }

        return context.getString(resId)
    }

    private enum class Day {
        Today,
        Yesterday,
        Older,
    }
}
