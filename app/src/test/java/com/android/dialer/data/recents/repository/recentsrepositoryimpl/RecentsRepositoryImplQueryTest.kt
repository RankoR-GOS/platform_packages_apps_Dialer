package com.android.dialer.data.recents.repository.recentsrepositoryimpl

import android.content.ContentResolver
import android.os.Build
import android.provider.CallLog
import com.android.dialer.testutil.callLogRow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsRepositoryImplQueryTest : BaseRecentsRepositoryImplTest() {

    @Test
    fun observeSnapshot_selectsEveryCallButBlockedAndVoicemail() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            val queryArgs = stubCallLogQuery(rows = listOf(callLogRow(id = 1L)))
            stubObserverRegistration()

            createRepository().observeSnapshot().first()

            assertEquals(
                joinPredicates(BLOCKED_PREDICATE, VOICEMAIL_EXCLUSION, DUO_EXCLUSION),
                capturedSelection(queryArgs = queryArgs),
            )
            assertEquals(
                listOf(BLOCKED_TYPE_ARG, VOICEMAIL_TYPE_ARG, DUO_PACKAGE_PATTERN),
                capturedArgs(queryArgs = queryArgs),
            )
        }
    }

    @Test
    fun observeSnapshot_bindsEveryComparedValueAsASelectionArgument() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            val queryArgs = stubCallLogQuery(rows = listOf(callLogRow(id = 1L)))
            stubObserverRegistration()

            createRepository().observeSnapshot().first()

            val selection = capturedSelection(queryArgs = queryArgs)
            assertEquals(
                capturedArgs(queryArgs = queryArgs).size,
                selection.count { character -> character == '?' },
            )
            assertFalse(selection.contains(DUO_PACKAGE_PATTERN))
        }
    }

    @Test
    fun observeSnapshot_capsTheRowCountThroughTheUriParameter() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            stubCallLogQuery(rows = listOf(callLogRow(id = 1L)))
            stubObserverRegistration()

            createRepository().observeSnapshot().first()

            assertEquals(
                CALL_LOG_LIMIT.toString(),
                capturedUris.single().getQueryParameter(CallLog.Calls.LIMIT_PARAM_KEY),
            )
        }
    }

    @Test
    fun observeSnapshot_sortsByDateDescending() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            val queryArgs = stubCallLogQuery(rows = listOf(callLogRow(id = 1L)))
            stubObserverRegistration()

            createRepository().observeSnapshot().first()

            val captured = queryArgs.captured
            assertEquals(
                listOf(CallLog.Calls.DATE),
                captured.getStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS)?.toList(),
            )
            assertEquals(
                ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
                captured.getInt(ContentResolver.QUERY_ARG_SORT_DIRECTION),
            )
        }
    }

    private fun joinPredicates(vararg predicates: String): String {
        return predicates.joinToString(separator = " AND ")
    }

    private companion object {
        private const val CALL_LOG_LIMIT = 1_000
        private const val DUO_PACKAGE_PATTERN = "com.google.android.apps.tachyon%"

        private val BLOCKED_PREDICATE = "(${CallLog.Calls.TYPE} != ?)"
        private val VOICEMAIL_EXCLUSION = "NOT (${CallLog.Calls.TYPE} = ?)"
        private val DUO_EXCLUSION =
            "(${CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME} IS NULL OR " +
                "${CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME} NOT LIKE ? OR " +
                "${CallLog.Calls.FEATURES} & ${CallLog.Calls.FEATURES_VIDEO} == " +
                "${CallLog.Calls.FEATURES_VIDEO})"

        private val BLOCKED_TYPE_ARG = CallLog.Calls.BLOCKED_TYPE.toString()
        private val VOICEMAIL_TYPE_ARG = CallLog.Calls.VOICEMAIL_TYPE.toString()
    }
}
