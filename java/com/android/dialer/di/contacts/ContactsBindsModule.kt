package com.android.dialer.di.contacts

import com.android.dialer.data.contacts.repository.ContactNameOrderSource
import com.android.dialer.data.contacts.repository.ContactsRepository
import com.android.dialer.data.contacts.repository.ContactsRepositoryImpl
import com.android.dialer.data.contacts.repository.LegacyContactNameOrderSource
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ContactsBindsModule {

    @Binds
    @Reusable
    abstract fun bindContactsRepository(impl: ContactsRepositoryImpl): ContactsRepository

    @Binds
    @Reusable
    abstract fun bindContactNameOrderSource(
        impl: LegacyContactNameOrderSource,
    ): ContactNameOrderSource
}
