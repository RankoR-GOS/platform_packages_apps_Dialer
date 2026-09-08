package com.android.dialer.data.recents.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.database.ContentObserver
import android.database.Cursor
import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteDiskIOException
import android.database.sqlite.SQLiteFullException
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import com.android.dialer.common.LogUtil
import com.android.dialer.data.recents.model.CallLogEntry
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.data.recents.model.CallLogFilter
import com.android.dialer.data.recents.model.CallLogSnapshot
import com.android.dialer.data.recents.model.CallType
import com.android.dialer.data.recents.model.RecentsWriteFailure
import com.android.dialer.data.recents.model.RecentsWriteResult
import com.android.dialer.di.core.IoDispatcher
import com.android.dialer.domain.recents.usecase.IsCallLogPermissionGranted
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withContext

internal interface RecentsRepository {

    fun observeSnapshot(filter: CallLogFilter): Flow<CallLogSnapshot>

    suspend fun delete(entryIds: List<CallLogEntryId>): RecentsWriteResult

    suspend fun markRead(entryIds: List<CallLogEntryId>): RecentsWriteResult

    suspend fun clearHistory(): RecentsWriteResult

    fun refresh()
}

internal class RecentsRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    private val isCallLogPermissionGranted: IsCallLogPermissionGranted,
    @param:IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : RecentsRepository {

    private val manualRefresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun observeSnapshot(filter: CallLogFilter): Flow<CallLogSnapshot> {
        return merge(
            observeUri(uri = CallLog.Calls.CONTENT_URI),
            manualRefresh,
        )
            .conflate()
            .mapNotNull { querySnapshot(filter = filter) }
            .flowOn(ioDispatcher)
    }

    override suspend fun delete(entryIds: List<CallLogEntryId>): RecentsWriteResult {
        if (entryIds.isEmpty()) {
            return RecentsWriteResult.Completed
        }

        return withContext(ioDispatcher) {
            runWrite(operation = "delete") {
                contentResolver.delete(
                    CallLog.Calls.CONTENT_URI,
                    idSelection(entryIds = entryIds),
                    null,
                )
            }
        }
    }

    override suspend fun markRead(entryIds: List<CallLogEntryId>): RecentsWriteResult {
        if (entryIds.isEmpty()) {
            return RecentsWriteResult.Completed
        }

        return withContext(ioDispatcher) {
            val values = ContentValues(1).apply {
                put(CallLog.Calls.IS_READ, 1)
            }
            runWrite(operation = "markRead") {
                contentResolver.update(
                    CallLog.Calls.CONTENT_URI,
                    values,
                    idSelection(entryIds = entryIds),
                    null,
                )
            }
        }
    }

    override suspend fun clearHistory(): RecentsWriteResult {
        return withContext(ioDispatcher) {
            runWrite(operation = "clearHistory") {
                contentResolver.delete(CallLog.Calls.CONTENT_URI, null, null)
            }
        }
    }

    override fun refresh() {
        manualRefresh.tryEmit(Unit)
    }

    private inline fun runWrite(operation: String, write: () -> Int): RecentsWriteResult {
        return try {
            write()
            RecentsWriteResult.Completed
        } catch (e: SecurityException) {
            LogUtil.e(TAG, "RecentsRepositoryImpl.$operation: permission revoked", e)
            RecentsWriteResult.Failed(cause = RecentsWriteFailure.PermissionRevoked)
        } catch (e: SQLiteDiskIOException) {
            LogUtil.e(TAG, "RecentsRepositoryImpl.$operation: disk write failed", e)
            RecentsWriteResult.Failed(cause = RecentsWriteFailure.Storage)
        } catch (e: SQLiteFullException) {
            LogUtil.e(TAG, "RecentsRepositoryImpl.$operation: disk full", e)
            RecentsWriteResult.Failed(cause = RecentsWriteFailure.Storage)
        } catch (e: SQLiteDatabaseCorruptException) {
            LogUtil.e(TAG, "RecentsRepositoryImpl.$operation: call log corrupt", e)
            RecentsWriteResult.Failed(cause = RecentsWriteFailure.Storage)
        } catch (e: IllegalArgumentException) {
            LogUtil.e(TAG, "RecentsRepositoryImpl.$operation: provider rejected the write", e)
            RecentsWriteResult.Failed(cause = RecentsWriteFailure.ProviderAbsent)
        }
    }

    private fun idSelection(entryIds: List<CallLogEntryId>): String {
        val ids = entryIds.joinToString(separator = ",") { entryId -> entryId.value.toString() }
        return "${CallLog.Calls._ID} IN ($ids)"
    }

    private fun observeUri(uri: Uri): Flow<Unit> {
        return callbackFlow {
            val observer = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    trySend(Unit)
                }
            }

            contentResolver.registerContentObserver(uri, true, observer)
            trySend(Unit)

            awaitClose {
                contentResolver.unregisterContentObserver(observer)
            }
        }
    }

    private fun querySnapshot(filter: CallLogFilter): CallLogSnapshot? {
        if (!isCallLogPermissionGranted()) {
            return permissionDeniedSnapshot()
        }

        return try {
            queryEntries(filter = filter)?.let { entries ->
                CallLogSnapshot(entries = entries, isPermissionGranted = true)
            }
        } catch (e: SecurityException) {
            LogUtil.e(TAG, "RecentsRepositoryImpl.querySnapshot: permission revoked mid-query", e)
            permissionDeniedSnapshot()
        }
    }

    private fun permissionDeniedSnapshot(): CallLogSnapshot {
        return CallLogSnapshot(entries = persistentListOf(), isPermissionGranted = false)
    }

    private fun queryEntries(filter: CallLogFilter): ImmutableList<CallLogEntry>? {
        return try {
            val cursor = contentResolver.query(
                CALL_LOG_LIST_URI,
                CALL_LOG_PROJECTION,
                queryArgs(filter = filter),
                null,
            ) ?: return persistentListOf()

            cursor.use(::mapEntries).toImmutableList()
        } catch (e: SQLiteDiskIOException) {
            LogUtil.e(TAG, "RecentsRepositoryImpl.queryEntries: disk read failed", e)
            null
        } catch (e: SQLiteFullException) {
            LogUtil.e(TAG, "RecentsRepositoryImpl.queryEntries: disk full", e)
            null
        } catch (e: SQLiteDatabaseCorruptException) {
            LogUtil.e(TAG, "RecentsRepositoryImpl.queryEntries: call log corrupt", e)
            null
        } catch (e: IllegalArgumentException) {
            LogUtil.e(TAG, "RecentsRepositoryImpl.queryEntries: provider rejected the query", e)
            null
        }
    }

    private fun mapEntries(cursor: Cursor): List<CallLogEntry> {
        val columns = CallLogCursorColumns(
            idIndex = cursor.getColumnIndexOrThrow(CallLog.Calls._ID),
            numberIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER),
            formattedNumberIndex = cursor.getColumnIndexOrThrow(
                CallLog.Calls.CACHED_FORMATTED_NUMBER,
            ),
            presentationIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER_PRESENTATION),
            typeIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE),
            dateIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE),
            durationIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION),
            featuresIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.FEATURES),
            geocodedLocationIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.GEOCODED_LOCATION),
            nameIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME),
            photoUriIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_PHOTO_URI),
            lookupUriIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_LOOKUP_URI),
            isReadIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.IS_READ),
        )

        return buildList(capacity = cursor.count) {
            while (cursor.moveToNext()) {
                mapEntry(cursor = cursor, columns = columns)?.let(::add)
            }
        }
    }

    private fun mapEntry(cursor: Cursor, columns: CallLogCursorColumns): CallLogEntry? {
        val entryId = CallLogEntryId.fromOrNull(cursor.getLong(columns.idIndex)) ?: return null

        return CallLogEntry(
            entryId = entryId,
            number = cursor.getString(columns.numberIndex)?.trim().orEmpty(),
            formattedNumber = cursor.getString(columns.formattedNumberIndex)
                ?.takeIf { it.isNotBlank() },
            numberPresentation = cursor.getInt(columns.presentationIndex),
            geocodedLocation = cursor.getString(columns.geocodedLocationIndex)
                ?.takeIf { it.isNotBlank() },
            cachedName = cursor.getString(columns.nameIndex)?.takeIf { it.isNotBlank() },
            photoUri = cursor.getString(columns.photoUriIndex)?.takeIf { it.isNotBlank() },
            lookupUri = cursor.getString(columns.lookupUriIndex)?.takeIf { it.isNotBlank() },
            timestampMillis = cursor.getLong(columns.dateIndex),
            durationSeconds = cursor.getLong(columns.durationIndex),
            features = cursor.getInt(columns.featuresIndex),
            callType = callType(rawType = cursor.getInt(columns.typeIndex)),
            isRead = cursor.getInt(columns.isReadIndex) != 0,
        )
    }

    private fun callType(rawType: Int): CallType {
        return when (rawType) {
            CallLog.Calls.INCOMING_TYPE, CallLog.Calls.ANSWERED_EXTERNALLY_TYPE -> CallType.Answered
            CallLog.Calls.OUTGOING_TYPE -> CallType.Outgoing
            CallLog.Calls.MISSED_TYPE -> CallType.Missed
            CallLog.Calls.REJECTED_TYPE -> CallType.Rejected
            CallLog.Calls.BLOCKED_TYPE -> CallType.Blocked
            CallLog.Calls.VOICEMAIL_TYPE -> CallType.Voicemail
            else -> CallType.Unknown(rawType = rawType)
        }
    }

    private fun queryArgs(filter: CallLogFilter): Bundle {
        val selection = selection(filter = filter)
        return Bundle().apply {
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection.where)
            putStringArray(
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                selection.args.toTypedArray(),
            )
            putStringArray(
                ContentResolver.QUERY_ARG_SORT_COLUMNS,
                arrayOf(CallLog.Calls.DATE),
            )
            putInt(
                ContentResolver.QUERY_ARG_SORT_DIRECTION,
                ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
            )
        }
    }

    private fun selection(filter: CallLogFilter): CallLogSelection {
        val predicates = mutableListOf<String>()
        val args = mutableListOf<String>()

        predicates.add("(${CallLog.Calls.TYPE} != ?)")
        args.add(CallLog.Calls.BLOCKED_TYPE.toString())

        when (filter) {
            CallLogFilter.All -> {
                predicates.add("NOT (${CallLog.Calls.TYPE} = ?)")
                args.add(CallLog.Calls.VOICEMAIL_TYPE.toString())
            }

            CallLogFilter.Missed -> {
                predicates.add("(${CallLog.Calls.TYPE} = ?)")
                args.add(CallLog.Calls.MISSED_TYPE.toString())
            }
        }

        predicates.add(DUO_EXCLUSION)
        args.add(DUO_PACKAGE_PATTERN)

        return CallLogSelection(
            where = predicates.joinToString(separator = " AND "),
            args = args,
        )
    }

    private data class CallLogSelection(
        val where: String,
        val args: List<String>,
    )

    private data class CallLogCursorColumns(
        val idIndex: Int,
        val numberIndex: Int,
        val formattedNumberIndex: Int,
        val presentationIndex: Int,
        val typeIndex: Int,
        val dateIndex: Int,
        val durationIndex: Int,
        val featuresIndex: Int,
        val geocodedLocationIndex: Int,
        val nameIndex: Int,
        val photoUriIndex: Int,
        val lookupUriIndex: Int,
        val isReadIndex: Int,
    )

    internal companion object {
        private const val TAG = "RecentsRepositoryImpl"

        private const val CALL_LOG_LIMIT = 1_000

        private const val DUO_PACKAGE_PATTERN = "com.google.android.apps.tachyon%"

        private const val DUO_EXCLUSION =
            "(${CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME} IS NULL OR " +
                "${CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME} NOT LIKE ? OR " +
                "${CallLog.Calls.FEATURES} & ${CallLog.Calls.FEATURES_VIDEO} == " +
                "${CallLog.Calls.FEATURES_VIDEO})"

        private val CALL_LOG_LIST_URI: Uri = CallLog.Calls.CONTENT_URI.buildUpon()
            .appendQueryParameter(CallLog.Calls.LIMIT_PARAM_KEY, CALL_LOG_LIMIT.toString())
            .build()

        internal val CALL_LOG_PROJECTION = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_FORMATTED_NUMBER,
            CallLog.Calls.NUMBER_PRESENTATION,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.FEATURES,
            CallLog.Calls.GEOCODED_LOCATION,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.CACHED_PHOTO_URI,
            CallLog.Calls.CACHED_LOOKUP_URI,
            CallLog.Calls.IS_READ,
        )
    }
}
