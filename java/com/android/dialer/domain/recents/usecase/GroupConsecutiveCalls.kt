package com.android.dialer.domain.recents.usecase

import android.telephony.PhoneNumberUtils
import com.android.dialer.compat.telephony.TelephonyManagerCompat
import com.android.dialer.data.recents.model.CallLogEntry
import com.android.dialer.data.recents.model.CallType
import com.android.dialer.phonenumberutil.PhoneNumberHelper
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal fun interface GroupConsecutiveCalls {
    operator fun invoke(entries: List<CallLogEntry>): ImmutableList<CallLogEntry>
}

internal class GroupConsecutiveCallsImpl @Inject constructor() : GroupConsecutiveCalls {

    override fun invoke(entries: List<CallLogEntry>): ImmutableList<CallLogEntry> {
        if (entries.isEmpty()) {
            return persistentListOf()
        }

        val grouped = mutableListOf<CallLogEntry>()
        val runIds = mutableListOf(entries.first().entryId)
        var runStart = entries.first()

        for (index in 1 until entries.size) {
            val entry = entries[index]

            when {
                canMerge(runStart = runStart, entry = entry) -> runIds.add(entry.entryId)

                else -> {
                    grouped.add(runStart.copy(groupedEntryIds = runIds.toImmutableList()))
                    runIds.clear()
                    runIds.add(entry.entryId)
                    runStart = entry
                }
            }
        }

        grouped.add(runStart.copy(groupedEntryIds = runIds.toImmutableList()))
        return grouped.toImmutableList()
    }

    private fun canMerge(runStart: CallLogEntry, entry: CallLogEntry): Boolean {
        return equalNumbers(number1 = runStart.number, number2 = entry.number) &&
            sameCallbackAction(runStart = runStart, entry = entry) &&
            areBothNotVoicemail(callType = entry.callType, runCallType = runStart.callType) &&
            (
                areBothNotBlocked(callType = entry.callType, runCallType = runStart.callType) ||
                    areBothBlocked(callType = entry.callType, runCallType = runStart.callType)
                ) &&
            meetsAssistedDialingGroupingCriteria(
                runFeatures = runStart.features,
                callFeatures = entry.features,
            )
    }

    @Suppress("DEPRECATION")
    private fun equalNumbers(number1: String, number2: String): Boolean {
        return when {
            PhoneNumberHelper.isUriNumber(number1) || PhoneNumberHelper.isUriNumber(number2) ->
                compareSipAddresses(number1 = number1, number2 = number2)

            PhoneNumberHelper.numberHasSpecialChars(number1) ||
                PhoneNumberHelper.numberHasSpecialChars(number2) ->
                PhoneNumberHelper.sameRawNumbers(number1, number2)

            else -> PhoneNumberUtils.compare(number1, number2)
        }
    }

    private fun compareSipAddresses(number1: String, number2: String): Boolean {
        val userInfo1 = number1.substringBefore(delimiter = SIP_DELIMITER)
        val userInfo2 = number2.substringBefore(delimiter = SIP_DELIMITER)
        val rest1 = number1.removePrefix(prefix = userInfo1)
        val rest2 = number2.removePrefix(prefix = userInfo2)

        return userInfo1 == userInfo2 && rest1.equals(rest2, ignoreCase = true)
    }

    private fun sameCallbackAction(runStart: CallLogEntry, entry: CallLogEntry): Boolean {
        return runStart.isVideoCall == entry.isVideoCall
    }

    private fun areBothNotVoicemail(callType: CallType, runCallType: CallType): Boolean {
        return callType != CallType.Voicemail && runCallType != CallType.Voicemail
    }

    private fun areBothNotBlocked(callType: CallType, runCallType: CallType): Boolean {
        return callType != CallType.Blocked && runCallType != CallType.Blocked
    }

    private fun areBothBlocked(callType: CallType, runCallType: CallType): Boolean {
        return callType == CallType.Blocked && runCallType == CallType.Blocked
    }

    private fun meetsAssistedDialingGroupingCriteria(runFeatures: Int, callFeatures: Int): Boolean {
        val runAssisted = runFeatures and TelephonyManagerCompat.FEATURES_ASSISTED_DIALING
        val callAssisted = callFeatures and TelephonyManagerCompat.FEATURES_ASSISTED_DIALING

        return runAssisted == callAssisted
    }

    private companion object {
        private const val SIP_DELIMITER = '@'
    }
}
