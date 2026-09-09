package com.android.dialer.data.recents.repository

import com.android.dialer.data.recents.model.CallLogEntry
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.data.recents.model.CallLogFilter
import com.android.dialer.data.recents.model.CallLogSnapshot
import com.android.dialer.data.recents.model.CallType
import com.android.dialer.data.recents.model.RecentsWriteFailure
import com.android.dialer.data.recents.model.RecentsWriteResult
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

internal class SyntheticRecentsRepository @Inject constructor(
    private val selectedScenario: SyntheticCallLogScenarioSource,
) : RecentsRepository {

    private val log = MutableStateFlow(seed(scenario = DEFAULT_SCENARIO))

    override fun observeSnapshot(filter: CallLogFilter): Flow<CallLogSnapshot> {
        return log
            .onStart { reseed() }
            .map { state -> state.toSnapshot(filter = filter) }
    }

    override suspend fun delete(entryIds: List<CallLogEntryId>): RecentsWriteResult {
        if (entryIds.isEmpty()) {
            return RecentsWriteResult.Completed
        }

        val removed = entryIds.toSet()
        return write { state ->
            state.copy(
                entries = state.entries
                    .filterNot { entry -> entry.entryId in removed }
                    .toImmutableList(),
            )
        }
    }

    override suspend fun markRead(entryIds: List<CallLogEntryId>): RecentsWriteResult {
        if (entryIds.isEmpty()) {
            return RecentsWriteResult.Completed
        }

        val read = entryIds.toSet()
        return write { state ->
            state.copy(
                entries = state.entries
                    .map { entry ->
                        when (entry.entryId) {
                            in read -> entry.copy(isRead = true)
                            else -> entry
                        }
                    }
                    .toImmutableList(),
            )
        }
    }

    override suspend fun clearHistory(): RecentsWriteResult {
        return write { state -> state.copy(entries = persistentListOf()) }
    }

    override fun refresh() {
        reseed()
    }

    private fun write(edit: (SyntheticCallLogState) -> SyntheticCallLogState): RecentsWriteResult {
        if (log.value.scenario == SyntheticCallLogScenario.PermissionDenied) {
            return RecentsWriteResult.Failed(cause = RecentsWriteFailure.PermissionRevoked)
        }

        log.update(edit)
        return RecentsWriteResult.Completed
    }

    private fun reseed() {
        val scenario = selectedScenario() ?: DEFAULT_SCENARIO

        if (scenario != log.value.scenario) {
            log.value = seed(scenario = scenario)
        }
    }

    private fun seed(scenario: SyntheticCallLogScenario): SyntheticCallLogState {
        return SyntheticCallLogState(
            scenario = scenario,
            entries = syntheticCallLogEntries(scenario = scenario),
        )
    }

    private data class SyntheticCallLogState(
        val scenario: SyntheticCallLogScenario,
        val entries: ImmutableList<CallLogEntry>,
    ) {
        fun toSnapshot(filter: CallLogFilter): CallLogSnapshot {
            return CallLogSnapshot(
                entries = entries
                    .filter { entry -> matches(entry = entry, filter = filter) }
                    .sortedByDescending { entry -> entry.timestampMillis }
                    .toImmutableList(),
                isPermissionGranted = scenario != SyntheticCallLogScenario.PermissionDenied,
            )
        }

        private fun matches(entry: CallLogEntry, filter: CallLogFilter): Boolean {
            return when {
                entry.callType == CallType.Blocked -> false
                filter == CallLogFilter.Missed -> entry.callType == CallType.Missed
                else -> entry.callType != CallType.Voicemail
            }
        }
    }

    private companion object {
        private val DEFAULT_SCENARIO = SyntheticCallLogScenario.Populated
    }
}
