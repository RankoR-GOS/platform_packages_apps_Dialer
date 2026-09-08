package com.android.dialer.di.core

import android.content.ContentResolver
import android.os.Build
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(
    application = HiltTestApplication::class,
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.BAKLAVA],
)
internal class CoreGraphTest {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface CoreTestEntryPoint {
        @DefaultDispatcher
        fun defaultDispatcher(): CoroutineDispatcher

        @IoDispatcher
        fun ioDispatcher(): CoroutineDispatcher

        fun contentResolver(): ContentResolver
    }

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var entryPoint: CoreTestEntryPoint

    @Before
    fun setUp() {
        hiltRule.inject()
        entryPoint = EntryPointAccessors.fromApplication(
            RuntimeEnvironment.getApplication(),
            CoreTestEntryPoint::class.java,
        )
    }

    @Test
    fun graph_resolvesEachDispatcherQualifierToItsProvider() {
        val module = CoreProvidesModule()

        assertSame(module.provideDefaultDispatcher(), entryPoint.defaultDispatcher())
        assertSame(module.provideIoDispatcher(), entryPoint.ioDispatcher())
    }

    @Test
    fun graph_resolvesThePlatformHandlesFromTheApplicationContext() {
        val application = RuntimeEnvironment.getApplication()

        assertSame(application.contentResolver, entryPoint.contentResolver())
    }
}
