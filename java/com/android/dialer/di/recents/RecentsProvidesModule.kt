package com.android.dialer.di.recents

import android.content.Context
import com.android.dialer.contacts.ContactsComponent
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal class RecentsProvidesModule {

    @Provides
    @Reusable
    fun provideContactDisplayPreferences(
        @ApplicationContext context: Context,
    ): ContactDisplayPreferences {
        return ContactsComponent.get(context).contactDisplayPreferences()
    }
}
