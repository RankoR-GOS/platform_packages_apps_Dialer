package com.android.dialer.di.core

import android.content.ContentResolver
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.components.SingletonComponent
import kotlin.coroutines.ContinuationInterceptor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Resolves every core binding through the real Hilt graph rather than by calling
 * [CoreProvidesModule] directly.
 *
 * This project sets `dagger.hilt.disableModulesHaveInstallInCheck=true`, because Dialer's 33
 * legacy `@Module`s belong to `AospDialerRootComponent` and would otherwise fail the build. The
 * cost is that a new Hilt module which forgets `@InstallIn` no longer fails loudly — it silently
 * contributes nothing. A test that pulls the bindings out of the graph is what catches that.
 */
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(
    application = HiltTestApplication::class,
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.BAKLAVA],
)
class CoreGraphTest {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface CoreTestEntryPoint {
        @DefaultDispatcher
        fun defaultDispatcher(): CoroutineDispatcher

        @IoDispatcher
        fun ioDispatcher(): CoroutineDispatcher

        @MainDispatcher
        fun mainDispatcher(): CoroutineDispatcher

        @ApplicationCoroutineScope
        fun applicationCoroutineScope(): CoroutineScope

        fun contentResolver(): ContentResolver

        fun packageManager(): PackageManager
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
    fun graphResolvesEachDispatcherToItsOwnQualifier() {
        assertSame(Dispatchers.Default, entryPoint.defaultDispatcher())
        assertSame(Dispatchers.IO, entryPoint.ioDispatcher())
        assertSame(Dispatchers.Main, entryPoint.mainDispatcher())
    }

    @Test
    fun graphResolvesApplicationCoroutineScopeOnTheDefaultDispatcher() {
        val scope = entryPoint.applicationCoroutineScope()

        assertSame(Dispatchers.Default, scope.coroutineContext[ContinuationInterceptor])
    }

    // @Singleton, unlike the @Reusable dispatchers, has to hand back the very same scope.
    @Test
    fun graphScopesApplicationCoroutineScopeAsASingleton() {
        assertSame(
            entryPoint.applicationCoroutineScope(),
            entryPoint.applicationCoroutineScope(),
        )
    }

    @Test
    fun graphResolvesPlatformServicesFromTheApplicationContext() {
        val application = RuntimeEnvironment.getApplication()

        assertNotNull(entryPoint.contentResolver())
        assertSame(application.contentResolver, entryPoint.contentResolver())
        assertSame(application.packageManager, entryPoint.packageManager())
    }
}
