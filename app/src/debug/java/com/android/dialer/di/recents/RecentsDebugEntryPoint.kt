package com.android.dialer.di.recents

import android.content.Context
import com.android.dialer.data.recents.repository.RecentsRepository
import com.android.dialer.data.recents.repository.SyntheticCallLogScenarioSource
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface RecentsDebugEntryPoint {
    fun systemRecentsRepository(): RecentsRepository

    @SyntheticCallLog
    fun syntheticRecentsRepository(): RecentsRepository

    fun syntheticCallLogScenarioSource(): SyntheticCallLogScenarioSource
}

internal fun debugRecentsRepository(context: Context): RecentsRepository {
    val entryPoint = EntryPointAccessors.fromApplication(
        context,
        RecentsDebugEntryPoint::class.java,
    )
    val selectedScenario = entryPoint.syntheticCallLogScenarioSource()

    return when (selectedScenario()) {
        null -> entryPoint.systemRecentsRepository()
        else -> entryPoint.syntheticRecentsRepository()
    }
}
