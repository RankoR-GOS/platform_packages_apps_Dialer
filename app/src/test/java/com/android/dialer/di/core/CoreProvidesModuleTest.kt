package com.android.dialer.di.core

import android.os.Build
import kotlin.coroutines.ContinuationInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.job
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
class CoreProvidesModuleTest {

    private val module = CoreProvidesModule()

    @Test
    fun provideDefaultDispatcher_isTheDefaultDispatcher() {
        assertSame(Dispatchers.Default, module.provideDefaultDispatcher())
    }

    @Test
    fun provideIoDispatcher_isTheIoDispatcher() {
        assertSame(Dispatchers.IO, module.provideIoDispatcher())
    }

    @Test
    fun provideMainDispatcher_isTheMainDispatcher() {
        assertSame(Dispatchers.Main, module.provideMainDispatcher())
    }

    @Test
    fun provideApplicationCoroutineScope_runsOnTheInjectedDispatcher() {
        val dispatcher = UnconfinedTestDispatcher()

        val scope = module.provideApplicationCoroutineScope(defaultDispatcher = dispatcher)

        assertSame(dispatcher, scope.coroutineContext[ContinuationInterceptor])
    }

    // The whole point of SupervisorJob: background work started by one screen must not be able to
    // tear down background work started by another. Swap SupervisorJob() for Job() and both of
    // these fail.
    //
    // async, not launch: the scope carries no CoroutineExceptionHandler (neither does the
    // reference implementation in Messaging), so a failing launch would reach the thread's
    // uncaught handler and take the test down with it. async parks the failure in the Deferred,
    // which is what lets the supervisor semantics be observed at all.
    @Test
    fun provideApplicationCoroutineScope_survivesAFailedChild() = runTest {
        val scope = module.provideApplicationCoroutineScope(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val failing = scope.async { error("boom") }
        failing.join()

        assertEquals("boom", failing.getCompletionExceptionOrNull()?.message)
        assertFalse(scope.coroutineContext.job.isCancelled)
        assertTrue(scope.coroutineContext.job.isActive)
    }

    @Test
    fun provideApplicationCoroutineScope_keepsSiblingsRunningAfterAFailure() = runTest {
        val scope = module.provideApplicationCoroutineScope(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val failing = scope.async { error("boom") }
        val sibling = scope.async { "still alive" }
        failing.join()

        assertEquals("still alive", sibling.await())
        assertTrue(scope.coroutineContext.job.isActive)
    }

    @Test
    fun provideContentResolver_comesFromTheApplicationContext() {
        val context = RuntimeEnvironment.getApplication()

        assertSame(context.contentResolver, module.provideContentResolver(context = context))
    }

    @Test
    fun providePackageManager_comesFromTheApplicationContext() {
        val context = RuntimeEnvironment.getApplication()

        assertSame(context.packageManager, module.providePackageManager(context = context))
    }
}
