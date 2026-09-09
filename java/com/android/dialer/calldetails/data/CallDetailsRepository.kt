package com.android.dialer.calldetails.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.provider.CallLog.Calls
import com.android.dialer.R
import com.android.dialer.calldetails.CallDetailsEntries
import com.android.dialer.calldetails.model.CallDetailsData
import com.android.dialer.calldetails.model.CallDetailsEntryData
import com.android.dialer.calldetails.model.CallDetailsHeaderData
import com.android.dialer.calllogutils.PhoneAccountUtils
import com.android.dialer.common.LogUtil
import com.android.dialer.dialercontact.DialerContact
import com.android.dialer.inject.IoDispatcher
import com.android.dialer.phonenumberutil.PhoneNumberHelper
import com.android.dialer.telecom.TelecomUtil
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Repository providing access to call details data and operations.
 *
 * Encapsulates reading aggregated call log history from the system provider, constructing
 * domain models from pre-parsed proto payloads, and deleting call records.
 */
internal interface CallDetailsRepository {

    /**
     * Retrieves aggregated call details for the specified call log entry identifiers.
     *
     * Queries the system call log provider for the given [callLogIds]. If [callLogIds] is
     * empty or an error occurs during query execution, fallback domain data using
     * [fallbackNumber] is returned.
     *
     * @param callLogIds list of call log database row IDs to query and aggregate.
     * @param fallbackNumber phone number to use if records cannot be loaded or have no number.
     * @return [CallDetailsData] containing parsed header information and chronological call
     *     entries.
     */
    suspend fun getCallDetails(
        callLogIds: List<Long>,
        fallbackNumber: String,
    ): CallDetailsData

    /**
     * Constructs [CallDetailsData] directly from pre-parsed intent extras.
     *
     * Used when the calling component supplies proto-serialized [DialerContact] and
     * [CallDetailsEntries] payloads, avoiding an immediate database query.
     *
     * @param contact optional contact information containing display name, number, and labels.
     * @param entries optional proto collection of individual call log records.
     * @param fallbackNumber phone number to display if [contact] contains no number.
     * @param canReportCallerId whether caller ID reporting actions should be enabled.
     * @param canSupportAssistedDialing whether assisted dialing settings are supported.
     * @return [CallDetailsData] populated from the provided proto representations.
     */
    fun createFromProto(
        contact: DialerContact?,
        entries: CallDetailsEntries?,
        fallbackNumber: String,
        canReportCallerId: Boolean = false,
        canSupportAssistedDialing: Boolean = false,
    ): CallDetailsData

    /**
     * Deletes the specified calls from the system call log provider.
     *
     * @param callIds list of call log database row IDs to delete.
     */
    suspend fun deleteCalls(
        callIds: List<Long>,
    )
}

@Singleton
internal class CallDetailsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CallDetailsRepository {

    override suspend fun getCallDetails(
        callLogIds: List<Long>,
        fallbackNumber: String,
    ): CallDetailsData = withContext(ioDispatcher) {
        if (callLogIds.isEmpty()) {
            return@withContext createFallbackData(fallbackNumber)
        }
        queryCallLog(callLogIds, fallbackNumber) ?: createFallbackData(fallbackNumber)
    }

    override fun createFromProto(
        contact: DialerContact?,
        entries: CallDetailsEntries?,
        fallbackNumber: String,
        canReportCallerId: Boolean,
        canSupportAssistedDialing: Boolean,
    ): CallDetailsData {
        val number = contact?.number?.takeIf { it.isNotBlank() } ?: fallbackNumber
        val isEmergency = PhoneNumberHelper.isLocalEmergencyNumber(context, number)
        val nameOrNumber = contact?.nameOrNumber.orEmpty()
        val displayName = resolveDisplayName(isEmergency, nameOrNumber, number)
        val secondaryText = resolveProtoSecondaryText(isEmergency, nameOrNumber, number, contact)
        val hasBlocked = entries?.entriesList?.any { it.callType == Calls.BLOCKED_TYPE } == true

        val headerData = CallDetailsHeaderData(
            primaryText = displayName.ifEmpty { "Unknown" },
            secondaryText = secondaryText,
            number = number,
            postDialDigits = contact?.postDialDigits.orEmpty(),
            photoUri = contact?.photoUri?.takeIf { it.isNotBlank() },
            contactLookupKey = contact?.contactUri?.takeIf { it.isNotBlank() },
            contactUri = contact?.contactUri?.takeIf { it.isNotBlank() },
            contactType = contact?.contactType ?: 1,
            isSpam = contact?.contactType == 5,
            isBlocked = hasBlocked,
            accountLabel = contact?.simDetails?.network?.takeIf { it.isNotBlank() },
        )

        return CallDetailsData(
            header = headerData,
            entries = parseProtoEntries(entries, headerData.accountLabel),
            canReportCallerId = canReportCallerId,
            canSupportAssistedDialing = canSupportAssistedDialing,
        )
    }

    private fun resolveDisplayName(
        isEmergency: Boolean,
        nameOrNumber: String,
        number: String,
    ): String = when {
        isEmergency -> context.getString(R.string.emergency_number)
        nameOrNumber.isNotBlank() -> nameOrNumber
        else -> number
    }

    private fun resolveProtoSecondaryText(
        isEmergency: Boolean,
        nameOrNumber: String,
        number: String,
        contact: DialerContact?,
    ): String? {
        if (isEmergency) return null
        val displayNumber = contact?.displayNumber?.takeIf { it.isNotBlank() }
        val numberLabel = contact?.numberLabel?.takeIf { it.isNotBlank() }
        return when {
            displayNumber != null && numberLabel != null ->
                context.getString(
                    R.string.call_subject_type_and_number,
                    numberLabel,
                    displayNumber,
                )
            displayNumber != null &&
                nameOrNumber.isNotBlank() &&
                nameOrNumber != displayNumber -> displayNumber
            nameOrNumber.isNotBlank() && nameOrNumber != number -> number
            else -> null
        }
    }

    private fun parseProtoEntries(
        entries: CallDetailsEntries?,
        accountLabel: String? = null,
    ): List<CallDetailsEntryData> {
        return entries?.entriesList?.map { entry ->
            val isVideo = (entry.features and Calls.FEATURES_VIDEO) != 0 || entry.isDuoCall
            val isRtt = entry.hasRttTranscript || (entry.features and Calls.FEATURES_RTT) != 0
            val note = entry.historyResultsList.firstOrNull()?.text?.takeIf { it.isNotBlank() }
            CallDetailsEntryData(
                callId = entry.callId,
                callType = entry.callType,
                timestamp = entry.date,
                durationSeconds = entry.duration,
                dataUsage = entry.dataUsage,
                isVideoCall = isVideo,
                isRtt = isRtt,
                postCallNote = note,
                accountLabel = accountLabel,
            )
        } ?: emptyList()
    }

    private fun queryCallLog(
        callLogIds: List<Long>,
        fallbackNumber: String,
    ): CallDetailsData? {
        val placeholders = callLogIds.joinToString(",") { "?" }
        val selection = "${Calls._ID} IN ($placeholders)"
        val selectionArgs = Array(callLogIds.size) { callLogIds[it].toString() }

        val cursor: Cursor? = try {
            context.contentResolver.query(
                Calls.CONTENT_URI,
                PROJECTION,
                selection,
                selectionArgs,
                "${Calls.DATE} DESC",
            )
        } catch (e: SecurityException) {
            LogUtil.e(TAG, "Permission denied reading call log", e)
            null
        } catch (e: SQLiteException) {
            LogUtil.e(TAG, "Database error reading call log", e)
            null
        } catch (e: IllegalArgumentException) {
            LogUtil.e(TAG, "Invalid argument reading call log", e)
            null
        } catch (e: IllegalStateException) {
            LogUtil.e(TAG, "Illegal state reading call log", e)
            null
        }

        return cursor?.use { c ->
            if (c.moveToFirst()) {
                parseCursor(c, fallbackNumber)
            } else {
                null
            }
        }
    }

    private fun parseCursor(
        c: Cursor,
        fallbackNumber: String,
    ): CallDetailsData {
        val numberIndex = c.getColumnIndexOrThrow(Calls.NUMBER)
        val postDialDigitsIndex = c.getColumnIndex(Calls.POST_DIAL_DIGITS)
        val nameIndex = c.getColumnIndexOrThrow(Calls.CACHED_NAME)

        val number = c.getString(numberIndex) ?: fallbackNumber
        val postDialDigits = if (postDialDigitsIndex >= 0) {
            c.getString(postDialDigitsIndex).orEmpty()
        } else {
            ""
        }
        val isEmergency = PhoneNumberHelper.isLocalEmergencyNumber(context, number)
        val cachedName = c.getString(nameIndex)
        val displayName = resolveCursorDisplayName(isEmergency, cachedName, number)
        val entries = parseCursorEntries(c)
        val hasBlocked = entries.any { it.callType == Calls.BLOCKED_TYPE }

        val header = CallDetailsHeaderData(
            primaryText = displayName,
            secondaryText = resolveCursorSecondaryText(isEmergency, cachedName, number),
            number = number,
            postDialDigits = postDialDigits,
            isBlocked = hasBlocked,
            accountLabel = entries.firstOrNull { !it.accountLabel.isNullOrBlank() }?.accountLabel,
        )

        return CallDetailsData(
            header = header,
            entries = entries,
            canReportCallerId = true,
            canSupportAssistedDialing = false,
        )
    }

    private fun resolveCursorDisplayName(
        isEmergency: Boolean,
        cachedName: String?,
        number: String,
    ): String = when {
        isEmergency -> context.getString(R.string.emergency_number)
        !cachedName.isNullOrBlank() -> cachedName
        else -> number
    }

    private fun resolveCursorSecondaryText(
        isEmergency: Boolean,
        cachedName: String?,
        number: String,
    ): String? {
        if (isEmergency || cachedName.isNullOrBlank() || cachedName == number) {
            return null
        }
        return number
    }

    private fun parseCursorEntries(c: Cursor): List<CallDetailsEntryData> {
        val idIndex = c.getColumnIndexOrThrow(Calls._ID)
        val typeIndex = c.getColumnIndexOrThrow(Calls.TYPE)
        val dateIndex = c.getColumnIndexOrThrow(Calls.DATE)
        val durationIndex = c.getColumnIndexOrThrow(Calls.DURATION)
        val dataUsageIndex = c.getColumnIndex(Calls.DATA_USAGE)
        val featuresIndex = c.getColumnIndex(Calls.FEATURES)
        val accountCompIndex = c.getColumnIndex(Calls.PHONE_ACCOUNT_COMPONENT_NAME)
        val accountIdIndex = c.getColumnIndex(Calls.PHONE_ACCOUNT_ID)

        return buildList {
            do {
                val features = if (featuresIndex >= 0) c.getInt(featuresIndex) else 0
                val dataUsage = if (dataUsageIndex >= 0) c.getLong(dataUsageIndex) else 0L
                val isVideo = (features and Calls.FEATURES_VIDEO) != 0
                val isRtt = (features and Calls.FEATURES_RTT) != 0
                val accountLabel = if (accountCompIndex >= 0 && accountIdIndex >= 0) {
                    val component = c.getString(accountCompIndex)
                    val id = c.getString(accountIdIndex)
                    if (!component.isNullOrBlank() && !id.isNullOrBlank()) {
                        val handle = TelecomUtil.composePhoneAccountHandle(component, id)
                        PhoneAccountUtils.getAccountLabel(context, handle)
                    } else {
                        null
                    }
                } else {
                    null
                }
                add(
                    CallDetailsEntryData(
                        callId = c.getLong(idIndex),
                        callType = c.getInt(typeIndex),
                        timestamp = c.getLong(dateIndex),
                        durationSeconds = c.getLong(durationIndex),
                        dataUsage = dataUsage,
                        isVideoCall = isVideo,
                        isRtt = isRtt,
                        accountLabel = accountLabel,
                    ),
                )
            } while (c.moveToNext())
        }
    }

    private fun createFallbackData(fallbackNumber: String): CallDetailsData =
        CallDetailsData(
            header = CallDetailsHeaderData(
                primaryText = fallbackNumber.ifEmpty { "Unknown" },
                number = fallbackNumber,
                postDialDigits = "",
            ),
            entries = emptyList(),
            canReportCallerId = false,
            canSupportAssistedDialing = false,
        )

    override suspend fun deleteCalls(
        callIds: List<Long>,
    ) = withContext(ioDispatcher) {
        if (callIds.isEmpty()) return@withContext
        val placeholders = callIds.joinToString(",") { "?" }
        val selection = "${Calls._ID} IN ($placeholders)"
        val selectionArgs = Array(callIds.size) { callIds[it].toString() }
        try {
            context.contentResolver.delete(Calls.CONTENT_URI, selection, selectionArgs)
        } catch (e: SecurityException) {
            LogUtil.e(TAG, "Permission denied deleting calls", e)
        } catch (e: SQLiteException) {
            LogUtil.e(TAG, "Database error deleting calls", e)
        } catch (e: IllegalArgumentException) {
            LogUtil.e(TAG, "Invalid argument deleting calls", e)
        }
    }

    private companion object {
        const val TAG = "CallDetailsRepositoryImpl"
        val PROJECTION = arrayOf(
            Calls._ID,
            Calls.NUMBER,
            Calls.POST_DIAL_DIGITS,
            Calls.CACHED_NAME,
            Calls.TYPE,
            Calls.DATE,
            Calls.DURATION,
            Calls.DATA_USAGE,
            Calls.FEATURES,
            Calls.PHONE_ACCOUNT_COMPONENT_NAME,
            Calls.PHONE_ACCOUNT_ID,
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CallDetailsDataModule {

    @Binds
    @Singleton
    abstract fun bindCallDetailsRepository(
        impl: CallDetailsRepositoryImpl,
    ): CallDetailsRepository
}
