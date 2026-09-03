package com.android.dialer.di.core

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * The bindings every new slice depends on.
 *
 * Installed into Hilt's [SingletonComponent], which is a separate graph from the hand-written
 * `AospDialerRootComponent`. New code binds here; the legacy graph keeps serving its own
 * `Component.get(context)` call sites until they migrate.
 */
@Module
@InstallIn(SingletonComponent::class)
internal class CoreProvidesModule {

    @Provides
    @Reusable
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Reusable
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Reusable
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @Singleton
    @ApplicationCoroutineScope
    fun provideApplicationCoroutineScope(
        @DefaultDispatcher
        defaultDispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + defaultDispatcher)

    @Provides
    @Reusable
    fun provideContentResolver(
        @ApplicationContext
        context: Context,
    ): ContentResolver = context.contentResolver

    @Provides
    @Reusable
    fun providePackageManager(
        @ApplicationContext
        context: Context,
    ): PackageManager = context.packageManager
}
