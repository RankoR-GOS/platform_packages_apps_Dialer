package com.android.dialer.di.core

import com.android.dialer.data.core.store.DeviceSettings
import com.android.dialer.data.core.store.DeviceSettingsImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface CoreBindsModule {

    @Binds
    @Reusable
    fun bindDeviceSettings(impl: DeviceSettingsImpl): DeviceSettings
}
