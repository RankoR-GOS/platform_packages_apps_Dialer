package com.android.dialer.data.recents.repository.syntheticrecentsrepository

import com.android.dialer.data.recents.model.CallLogFilter
import com.android.dialer.data.recents.model.CallLogSnapshot
import com.android.dialer.data.recents.repository.SyntheticCallLogScenario
import com.android.dialer.data.recents.repository.SyntheticCallLogScenarioSource
import com.android.dialer.data.recents.repository.SyntheticRecentsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first

@Suppress("AbstractClassCanBeConcreteClass")
internal abstract class BaseSyntheticRecentsRepositoryTest {

    protected val selectedScenario = mockk<SyntheticCallLogScenarioSource>()

    protected fun repository(scenario: SyntheticCallLogScenario?): SyntheticRecentsRepository {
        every { selectedScenario() } returns scenario

        return SyntheticRecentsRepository(selectedScenario = selectedScenario)
    }

    protected suspend fun snapshot(
        scenario: SyntheticCallLogScenario?,
        filter: CallLogFilter = CallLogFilter.All,
    ): CallLogSnapshot {
        return repository(scenario = scenario).observeSnapshot(filter = filter).first()
    }

    protected companion object {
        const val THIRTY_DIGITS = 30
        const val HOSTILE_RUN_SIZE = 100
        const val FUTURE_MARGIN_MILLIS = 1_806_240_000_000L
    }
}
