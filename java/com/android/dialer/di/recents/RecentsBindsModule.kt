package com.android.dialer.di.recents

import com.android.dialer.data.recents.repository.RecentsRepository
import com.android.dialer.data.recents.repository.RecentsRepositoryImpl
import com.android.dialer.domain.recents.usecase.CanPlaceCall
import com.android.dialer.domain.recents.usecase.CanPlaceCallImpl
import com.android.dialer.domain.recents.usecase.GroupConsecutiveCalls
import com.android.dialer.domain.recents.usecase.GroupConsecutiveCallsImpl
import com.android.dialer.domain.recents.usecase.IsCallLogPermissionGranted
import com.android.dialer.domain.recents.usecase.IsCallLogPermissionGrantedImpl
import com.android.dialer.domain.recents.usecase.IsEmergencyNumber
import com.android.dialer.domain.recents.usecase.IsEmergencyNumberImpl
import com.android.dialer.domain.recents.usecase.RelativeTimestampFormatter
import com.android.dialer.domain.recents.usecase.RelativeTimestampFormatterImpl
import com.android.dialer.ui.recents.mapper.RecentsItemUiMapper
import com.android.dialer.ui.recents.mapper.RecentsItemUiMapperImpl
import com.android.dialer.ui.recents.mapper.RecentsUiStateMapper
import com.android.dialer.ui.recents.mapper.RecentsUiStateMapperImpl
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

    @Binds
    @Reusable
    abstract fun bindCanPlaceCall(
        impl: CanPlaceCallImpl,
    ): CanPlaceCall

    @Binds
    @Reusable
    abstract fun bindIsEmergencyNumber(
        impl: IsEmergencyNumberImpl,
    ): IsEmergencyNumber

    @Binds
    @Reusable
    abstract fun bindRelativeTimestampFormatter(
        impl: RelativeTimestampFormatterImpl,
    ): RelativeTimestampFormatter

    @Binds
    @Reusable
    abstract fun bindRecentsItemUiMapper(
        impl: RecentsItemUiMapperImpl,
    ): RecentsItemUiMapper

    @Binds
    @Reusable
    abstract fun bindRecentsUiStateMapper(
        impl: RecentsUiStateMapperImpl,
    ): RecentsUiStateMapper
}
