package com.android.dialer.settings.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.android.dialer.contacts.ContactsComponent
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences
import com.android.dialer.settings.data.AndroidSettingsEnvironment
import com.android.dialer.settings.data.SettingsEnvironment
import com.android.dialer.settings.data.SettingsRepository
import com.android.dialer.settings.data.SharedPreferencesSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultSharedPreferences

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    abstract fun bindSettingsRepository(
        impl: SharedPreferencesSettingsRepository,
    ): SettingsRepository

    @Binds
    abstract fun bindSettingsEnvironment(impl: AndroidSettingsEnvironment): SettingsEnvironment

    companion object {

        @Suppress("DEPRECATION")
        @Provides
        @DefaultSharedPreferences
        fun provideDefaultSharedPreferences(
            @ApplicationContext context: Context,
        ): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        @Provides
        fun provideContactDisplayPreferences(
            @ApplicationContext context: Context,
        ): ContactDisplayPreferences =
            ContactsComponent.get(context).contactDisplayPreferences()
    }
}
