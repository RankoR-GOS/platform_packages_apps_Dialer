package com.android.dialer.di.phone

import com.android.dialer.data.phone.formatter.PhoneNumberFormatter
import com.android.dialer.data.phone.formatter.PhoneNumberFormatterImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PhoneBindsModule {

    @Binds
    @Reusable
    abstract fun bindPhoneNumberFormatter(
        impl: PhoneNumberFormatterImpl,
    ): PhoneNumberFormatter
}
