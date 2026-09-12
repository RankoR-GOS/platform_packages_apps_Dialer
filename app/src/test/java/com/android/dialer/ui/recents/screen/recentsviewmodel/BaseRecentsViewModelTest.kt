package com.android.dialer.ui.recents.screen.recentsviewmodel

import com.android.dialer.data.recents.model.CallLogSnapshot
import com.android.dialer.data.recents.repository.RecentsRepository
import com.android.dialer.testutil.MainDispatcherRule
import com.android.dialer.testutil.TEST_TIMESTAMP_MILLIS
import com.android.dialer.testutil.callLogEntry
import com.android.dialer.testutil.callLogSnapshot
import com.android.dialer.ui.recents.mapper.RecentsUiStateMapper
import com.android.dialer.ui.recents.model.RecentsContentUiState
import com.android.dialer.ui.recents.model.RecentsUiState
import com.android.dialer.ui.recents.screen.RecentsViewModel
import com.android.dialer.util.core.CurrentTimeProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("AbstractClassCanBeConcreteClass")
internal abstract class BaseRecentsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    protected val repository = mockk<RecentsRepository>()
    protected val uiStateMapper = mockk<RecentsUiStateMapper>()
    protected val currentTimeProvider = mockk<CurrentTimeProvider>()

    @Before
    fun setUp() {
        every { repository.observeSnapshot() } returns flowOf(SNAPSHOT)
        every { currentTimeProvider.currentTimeMillis() } returns NOW_MILLIS
        every { uiStateMapper.map(any(), any()) } returns MAPPED_STATE
    }

    protected fun createViewModel(): RecentsViewModel {
        return RecentsViewModel(
            repository = repository,
            uiStateMapper = uiStateMapper,
            currentTimeProvider = currentTimeProvider,
            defaultDispatcher = mainDispatcherRule.testDispatcher,
        )
    }

    protected companion object {
        const val NOW_MILLIS = TEST_TIMESTAMP_MILLIS
        const val MINUTE_MILLIS = 60_000L
        const val STOP_TIMEOUT_MILLIS = 5_000L
        val SNAPSHOT: CallLogSnapshot = callLogSnapshot(callLogEntry(id = 1L))
        val MAPPED_STATE = RecentsUiState(
            content = RecentsContentUiState.Empty(message = "mapped", actionLabel = "call"),
        )
    }
}
