package com.android.dialer.di.recents

import com.android.dialer.data.recents.repository.RecentsRepository
import com.android.dialer.data.recents.repository.SyntheticCallLogScenarioSource
import com.android.dialer.data.recents.repository.SyntheticCallLogScenarioSourceImpl
import com.android.dialer.data.recents.repository.SyntheticRecentsRepository
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class SyntheticCallLog

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RecentsDebugBindsModule {

    @Binds
    @Singleton
    @SyntheticCallLog
    abstract fun bindSyntheticRecentsRepository(
        impl: SyntheticRecentsRepository,
    ): RecentsRepository

    @Binds
    @Reusable
    abstract fun bindSyntheticCallLogScenarioSource(
        impl: SyntheticCallLogScenarioSourceImpl,
    ): SyntheticCallLogScenarioSource
}
