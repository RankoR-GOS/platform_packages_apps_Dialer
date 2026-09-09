package com.android.dialer.ui.recents.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.data.recents.model.CallLogFilter
import com.android.dialer.data.recents.model.RecentsWriteResult
import com.android.dialer.data.recents.repository.RecentsRepository
import com.android.dialer.di.core.DefaultDispatcher
import com.android.dialer.ui.recents.mapper.RecentsUiStateMapper
import com.android.dialer.ui.recents.model.RecentsAction as Action
import com.android.dialer.ui.recents.model.RecentsEffect as Effect
import com.android.dialer.ui.recents.model.RecentsUiState as State
import com.android.dialer.util.core.CurrentTimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal interface RecentsScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<State>

    fun onAction(action: Action)
}

@HiltViewModel
internal class RecentsViewModel @Inject constructor(
    private val repository: RecentsRepository,
    uiStateMapper: RecentsUiStateMapper,
    private val currentTimeProvider: CurrentTimeProvider,
    @param:DefaultDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel(),
    RecentsScreenModel {

    private val _effects = Channel<Effect>(capacity = Channel.BUFFERED)
    override val effects: Flow<Effect> = _effects.receiveAsFlow()

    override val uiState: StateFlow<State> = combine(
        repository.observeSnapshot(filter = CallLogFilter.All),
        minuteTicks(),
    ) { snapshot, nowMillis ->
        uiStateMapper.map(snapshot = snapshot, nowMillis = nowMillis)
    }
        .flowOn(defaultDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = STATEFLOW_STOP_TIMEOUT_MILLIS,
            ),
            initialValue = State(),
        )

    override fun onAction(action: Action) {
        when (action) {
            Action.ScreenResumed, Action.CallLogPermissionGranted -> repository.refresh()
            Action.GrantPermissionClicked -> emitEffect(Effect.RequestCallLogPermission)
            is Action.CallBackClicked -> emitEffect(Effect.PlaceCall(number = action.number))
            is Action.VideoCallClicked -> emitEffect(Effect.PlaceVideoCall(number = action.number))
            is Action.MessageClicked -> emitEffect(Effect.SendMessage(number = action.number))
            is Action.AddContactClicked -> emitEffect(Effect.AddContact(number = action.number))
            is Action.CopyNumberClicked -> emitEffect(Effect.CopyNumber(number = action.number))
            is Action.EntryViewed -> markRead(entryIds = action.entryIds)
            is Action.DeleteConfirmed -> delete(entryIds = action.entryIds)
            Action.ClearHistoryConfirmed -> clearHistory()
        }
    }

    private fun minuteTicks(): Flow<Long> {
        return flow {
            while (true) {
                emit(currentTimeProvider.currentTimeMillis())
                delay(MINUTE_MILLIS)
            }
        }
    }

    private fun markRead(entryIds: List<CallLogEntryId>) {
        viewModelScope.launch(defaultDispatcher) {
            repository.markRead(entryIds = entryIds)
        }
    }

    private fun delete(entryIds: List<CallLogEntryId>) {
        viewModelScope.launch(defaultDispatcher) {
            report(result = repository.delete(entryIds = entryIds))
        }
    }

    private fun clearHistory() {
        viewModelScope.launch(defaultDispatcher) {
            report(result = repository.clearHistory())
        }
    }

    private fun report(result: RecentsWriteResult) {
        when (result) {
            RecentsWriteResult.Completed -> Unit
            is RecentsWriteResult.Failed -> emitEffect(Effect.WriteFailed)
        }
    }

    private fun emitEffect(effect: Effect) {
        _effects.trySend(effect)
    }

    private companion object {
        private const val STATEFLOW_STOP_TIMEOUT_MILLIS = 5_000L
        private const val MINUTE_MILLIS = 60_000L
    }
}
