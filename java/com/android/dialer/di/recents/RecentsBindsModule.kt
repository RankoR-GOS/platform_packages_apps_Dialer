package com.android.dialer.di.recents

import com.android.dialer.data.recents.repository.RecentsRepository
import com.android.dialer.data.recents.repository.RecentsRepositoryImpl
import com.android.dialer.domain.recents.usecase.GroupConsecutiveCalls
import com.android.dialer.domain.recents.usecase.GroupConsecutiveCallsImpl
import com.android.dialer.domain.recents.usecase.IsCallLogPermissionGranted
import com.android.dialer.domain.recents.usecase.IsCallLogPermissionGrantedImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RecentsBindsModule {

    @Binds
    @Reusable
    abstract fun bindRecentsRepository(
        impl: RecentsRepositoryImpl,
    ): RecentsRepository

    @Binds
    @Reusable
    abstract fun bindIsCallLogPermissionGranted(
        impl: IsCallLogPermissionGrantedImpl,
    ): IsCallLogPermissionGranted

    @Binds
    @Reusable
    abstract fun bindGroupConsecutiveCalls(
        impl: GroupConsecutiveCallsImpl,
    ): GroupConsecutiveCalls
}
