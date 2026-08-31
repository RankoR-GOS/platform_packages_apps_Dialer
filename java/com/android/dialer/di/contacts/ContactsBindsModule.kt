package com.android.dialer.di.contacts

import com.android.dialer.data.contacts.repository.ContactNameOrderSource
import com.android.dialer.data.contacts.repository.ContactsRepository
import com.android.dialer.data.contacts.repository.ContactsRepositoryImpl
import com.android.dialer.data.contacts.repository.LegacyContactNameOrderSource
import com.android.dialer.domain.contacts.usecase.BuildContactLookupUri
import com.android.dialer.domain.contacts.usecase.BuildContactLookupUriImpl
import com.android.dialer.domain.contacts.usecase.GetDeniedContactsPermissions
import com.android.dialer.domain.contacts.usecase.GetDeniedContactsPermissionsImpl
import com.android.dialer.domain.contacts.usecase.IsReadContactsPermissionGranted
import com.android.dialer.domain.contacts.usecase.IsReadContactsPermissionGrantedImpl
import com.android.dialer.domain.contacts.usecase.ObserveContactsPermissionGrants
import com.android.dialer.domain.contacts.usecase.ObserveContactsPermissionGrantsImpl
import com.android.dialer.ui.contacts.screen.delegate.ContactsDelegate
import com.android.dialer.ui.contacts.screen.delegate.ContactsDelegateImpl
import com.android.dialer.ui.contacts.screen.mapper.ContactsUiStateMapper
import com.android.dialer.ui.contacts.screen.mapper.ContactsUiStateMapperImpl
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

    @Binds
    @Reusable
    abstract fun bindIsReadContactsPermissionGranted(
        impl: IsReadContactsPermissionGrantedImpl,
    ): IsReadContactsPermissionGranted

    @Binds
    @Reusable
    abstract fun bindGetDeniedContactsPermissions(
        impl: GetDeniedContactsPermissionsImpl,
    ): GetDeniedContactsPermissions

    @Binds
    @Reusable
    abstract fun bindObserveContactsPermissionGrants(
        impl: ObserveContactsPermissionGrantsImpl,
    ): ObserveContactsPermissionGrants

    @Binds
    @Reusable
    abstract fun bindBuildContactLookupUri(
        impl: BuildContactLookupUriImpl,
    ): BuildContactLookupUri

    @Binds
    @Reusable
    abstract fun bindContactsUiStateMapper(
        impl: ContactsUiStateMapperImpl,
    ): ContactsUiStateMapper

    @Binds
    abstract fun bindContactsDelegate(impl: ContactsDelegateImpl): ContactsDelegate
}
