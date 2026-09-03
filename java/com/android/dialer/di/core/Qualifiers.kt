package com.android.dialer.di.core

import javax.inject.Qualifier

// Qualifiers for the injectable coroutine primitives.
//
// Nothing outside CoreProvidesModule is allowed to name a kotlinx.coroutines.Dispatchers member
// directly: a repository that reaches for Dispatchers.IO itself cannot be driven by a test
// dispatcher, which is exactly what detekt's InjectDispatcher rule exists to catch.

/** CPU-bound work: mapping, sorting, diffing. */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

/** Blocking I/O: `ContentResolver` queries, disk, network. */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

/** UI thread. */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher

/**
 * Scope for work that has to outlive the screen that started it. Backed by a `SupervisorJob`, so
 * one failed child does not tear down the rest of the application's background work.
 */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class ApplicationCoroutineScope
