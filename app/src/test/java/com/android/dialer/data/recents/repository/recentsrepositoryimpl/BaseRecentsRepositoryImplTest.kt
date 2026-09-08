package com.android.dialer.data.recents.repository.recentsrepositoryimpl

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import com.android.dialer.data.recents.repository.RecentsRepositoryImpl
import com.android.dialer.domain.recents.usecase.IsCallLogPermissionGranted
import com.android.dialer.testutil.MainDispatcherRule
import com.android.dialer.testutil.TestCallLogRow
import com.android.dialer.testutil.callLogCursor
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("AbstractClassCanBeConcreteClass")
internal abstract class BaseRecentsRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    protected val contentResolver = mockk<ContentResolver>()
    protected val isCallLogPermissionGranted = mockk<IsCallLogPermissionGranted>()
    protected val capturedProjections = mutableListOf<Array<String>?>()
    protected val capturedUris = mutableListOf<Uri>()

    protected fun stubCallLogQuery(rows: List<TestCallLogRow>): CapturingSlot<Bundle> {
        val queryArgs = slot<Bundle>()

        every {
            contentResolver.query(
                match { uri: Uri -> uri.path == CallLog.Calls.CONTENT_URI.path },
                any(),
                capture(queryArgs),
                any(),
            )
        } answers {
            capturedUris.add(firstArg())
            capturedProjections.add(secondArg())
            callLogCursor(rows = rows.toTypedArray())
        }

        return queryArgs
    }

    protected fun stubQueryThrows(error: Throwable) {
        every { contentResolver.query(any(), any(), any<Bundle>(), any()) } throws error
    }

    protected fun stubObserverRegistration(): CapturingSlot<ContentObserver> {
        val observerSlot = slot<ContentObserver>()

        every {
            contentResolver.registerContentObserver(any(), any(), capture(observerSlot))
        } just runs
        every { contentResolver.unregisterContentObserver(any()) } just runs

        return observerSlot
    }

    protected fun capturedSelection(queryArgs: CapturingSlot<Bundle>): String {
        return queryArgs.captured.getString(ContentResolver.QUERY_ARG_SQL_SELECTION).orEmpty()
    }

    protected fun capturedArgs(queryArgs: CapturingSlot<Bundle>): List<String> {
        return queryArgs.captured
            .getStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS)
            .orEmpty()
            .toList()
    }

    protected fun createRepository(
        isCallLogGranted: Boolean = true,
        dispatcher: CoroutineDispatcher = mainDispatcherRule.testDispatcher,
    ): RecentsRepositoryImpl {
        every { isCallLogPermissionGranted() } returns isCallLogGranted

        return RecentsRepositoryImpl(
            contentResolver = contentResolver,
            isCallLogPermissionGranted = isCallLogPermissionGranted,
            ioDispatcher = dispatcher,
        )
    }
}
