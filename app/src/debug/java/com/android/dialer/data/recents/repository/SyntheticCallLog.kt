package com.android.dialer.data.recents.repository

import android.content.ContentResolver
import android.provider.CallLog
import android.provider.Settings
import com.android.dialer.data.recents.model.CallLogEntry
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.data.recents.model.CallType
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

private const val SEED_NOW_MILLIS = 1_806_240_000_000L

private const val AGE_ONE_MINUTE = 60_000L
private const val AGE_TWO_MINUTES = 120_000L
private const val AGE_THREE_MINUTES = 180_000L
private const val AGE_ONE_HOUR = 3_600_000L
private const val AGE_TWO_HOURS = 7_200_000L
private const val AGE_ONE_DAY = 86_400_000L
private const val AGE_YESTERDAY = 90_000_000L
private const val AGE_TWO_DAYS = 180_000_000L
private const val AGE_LAST_WEEK = 600_000_000L

private const val DURATION_SHORT = 95L
private const val DURATION_LONG = 340L
private const val DURATION_EMERGENCY = 12L
private const val DURATION_ABSURD = 999_999L
private const val DURATION_NEGATIVE = -1L

private const val MISSED_NUMBER = "+18765550111"
private const val LOCAL_NUMBER = "+18765550100"
private const val REJECTED_NUMBER = "+18765550199"
private const val EMERGENCY_NUMBER = "911"
private const val THIRTY_DIGIT_NUMBER = "123456789012345678901234567890"
private const val VANITY_NUMBER = "1-800-FLOWERS"

private const val GEOCODED_LOCATION = "Kingston, Jamaica"
private const val CONTACT_NAME = "Ada Lovelace"
private const val LONG_CONTACT_NAME =
    "Maximilian Bartholomew Fitzgerald-Whitcombe of Montego Bay and Ocho Rios"
private const val EMOJI_CONTACT_NAME = "Grace 🎉🇯🇲"

private const val ARABIC_CONTACT_NAME = "محمد عبد"
private const val CJK_CONTACT_NAME = "山田太郎"
private const val BLANK_CONTACT_NAME = "  "

private const val UNKNOWN_CALL_TYPE = 99
private const val HOSTILE_RUN_SIZE = 100
private const val HOSTILE_RUN_FIRST_ID = 301L

internal enum class SyntheticCallLogScenario {

    Populated,
    Empty,
    PermissionDenied,
    Hostile,
}

internal fun interface SyntheticCallLogScenarioSource {
    operator fun invoke(): SyntheticCallLogScenario?
}

internal class SyntheticCallLogScenarioSourceImpl @Inject constructor(
    private val contentResolver: ContentResolver,
) : SyntheticCallLogScenarioSource {

    override fun invoke(): SyntheticCallLogScenario? {
        val selected = Settings.Global.getString(contentResolver, SETTING_NAME)

        return SyntheticCallLogScenario.entries.firstOrNull { scenario ->
            scenario.name.equals(selected, ignoreCase = true)
        }
    }

    private companion object {
        private const val SETTING_NAME = "dialer_recents_synthetic_call_log"
    }
}

internal fun syntheticCallLogEntries(
    scenario: SyntheticCallLogScenario,
): ImmutableList<CallLogEntry> {
    return when (scenario) {
        SyntheticCallLogScenario.Populated -> populatedEntries()
        SyntheticCallLogScenario.Hostile -> hostileEntries()
        SyntheticCallLogScenario.Empty -> persistentListOf()
        SyntheticCallLogScenario.PermissionDenied -> persistentListOf()
    }
}

private fun populatedEntries(): ImmutableList<CallLogEntry> {
    return persistentListOf(
        entry(
            id = 1L,
            number = MISSED_NUMBER,
            callType = CallType.Missed,
            ageMillis = AGE_ONE_MINUTE,
            isRead = false,
        ),
        entry(
            id = 2L,
            number = MISSED_NUMBER,
            callType = CallType.Missed,
            ageMillis = AGE_TWO_MINUTES,
            isRead = false,
        ),
        entry(
            id = 3L,
            number = LOCAL_NUMBER,
            callType = CallType.Outgoing,
            ageMillis = AGE_ONE_HOUR,
            durationSeconds = DURATION_SHORT,
            geocodedLocation = GEOCODED_LOCATION,
        ),
        entry(
            id = 4L,
            number = LOCAL_NUMBER,
            callType = CallType.Answered,
            ageMillis = AGE_TWO_HOURS,
            durationSeconds = DURATION_LONG,
            cachedName = CONTACT_NAME,
        ),
        entry(
            id = 5L,
            number = EMERGENCY_NUMBER,
            callType = CallType.Outgoing,
            ageMillis = AGE_YESTERDAY,
            durationSeconds = DURATION_EMERGENCY,
        ),
        entry(
            id = 6L,
            number = REJECTED_NUMBER,
            callType = CallType.Rejected,
            ageMillis = AGE_TWO_DAYS,
        ),
        entry(
            id = 7L,
            number = "",
            callType = CallType.Answered,
            ageMillis = AGE_LAST_WEEK,
            numberPresentation = CallLog.Calls.PRESENTATION_RESTRICTED,
        ),
    )
}

private fun hostileEntries(): ImmutableList<CallLogEntry> {
    return (
        hostileNumberEntries() +
            hostileTimestampEntries() +
            hostileFilteredEntries() +
            hostileNameEntries() +
            hostileRunEntries()
        ).toImmutableList()
}

private fun hostileNumberEntries(): List<CallLogEntry> {
    return listOf(
        entry(
            id = 101L,
            number = THIRTY_DIGIT_NUMBER,
            callType = CallType.Outgoing,
            ageMillis = 1L,
        ),
        entry(id = 102L, number = VANITY_NUMBER, callType = CallType.Outgoing, ageMillis = 2L),
        entry(id = 103L, number = "+", callType = CallType.Answered, ageMillis = AGE_ONE_MINUTE),
        entry(
            id = 104L,
            number = "",
            callType = CallType.Answered,
            ageMillis = AGE_TWO_MINUTES,
            numberPresentation = CallLog.Calls.PRESENTATION_RESTRICTED,
        ),
        entry(
            id = 105L,
            number = "",
            callType = CallType.Missed,
            ageMillis = AGE_THREE_MINUTES,
            numberPresentation = CallLog.Calls.PRESENTATION_UNKNOWN,
        ),
        entry(
            id = 106L,
            number = LOCAL_NUMBER,
            callType = CallType.Answered,
            ageMillis = AGE_ONE_HOUR,
            durationSeconds = DURATION_ABSURD,
        ),
        entry(
            id = 107L,
            number = LOCAL_NUMBER,
            callType = CallType.Answered,
            ageMillis = AGE_TWO_HOURS,
            durationSeconds = DURATION_NEGATIVE,
        ),
    )
}

private fun hostileTimestampEntries(): List<CallLogEntry> {
    return listOf(
        entry(
            id = 108L,
            number = LOCAL_NUMBER,
            callType = CallType.Outgoing,
            ageMillis = -AGE_ONE_DAY,
        ),
        entry(
            id = 109L,
            number = LOCAL_NUMBER,
            callType = CallType.Answered,
            ageMillis = SEED_NOW_MILLIS,
        ),
    )
}

private fun hostileFilteredEntries(): List<CallLogEntry> {
    return listOf(
        entry(
            id = 110L,
            number = LOCAL_NUMBER,
            callType = CallType.Blocked,
            ageMillis = AGE_YESTERDAY,
        ),
        entry(
            id = 111L,
            number = LOCAL_NUMBER,
            callType = CallType.Voicemail,
            ageMillis = AGE_TWO_DAYS,
        ),
        entry(
            id = 112L,
            number = LOCAL_NUMBER,
            callType = CallType.Unknown(rawType = UNKNOWN_CALL_TYPE),
            ageMillis = AGE_LAST_WEEK,
        ),
    )
}

private fun hostileNameEntries(): List<CallLogEntry> {
    return listOf(
        entry(
            id = 201L,
            number = "${LOCAL_NUMBER}1",
            callType = CallType.Answered,
            ageMillis = AGE_THREE_MINUTES,
            cachedName = LONG_CONTACT_NAME,
        ),
        entry(
            id = 202L,
            number = "${LOCAL_NUMBER}2",
            callType = CallType.Missed,
            ageMillis = AGE_ONE_HOUR,
            cachedName = EMOJI_CONTACT_NAME,
            isRead = false,
        ),
        entry(
            id = 203L,
            number = "${LOCAL_NUMBER}3",
            callType = CallType.Outgoing,
            ageMillis = AGE_TWO_HOURS,
            cachedName = ARABIC_CONTACT_NAME,
        ),
        entry(
            id = 204L,
            number = "${LOCAL_NUMBER}4",
            callType = CallType.Answered,
            ageMillis = AGE_YESTERDAY,
            cachedName = CJK_CONTACT_NAME,
        ),
        entry(
            id = 205L,
            number = "${LOCAL_NUMBER}5",
            callType = CallType.Answered,
            ageMillis = AGE_TWO_DAYS,
            cachedName = BLANK_CONTACT_NAME,
        ),
    )
}

private fun hostileRunEntries(): List<CallLogEntry> {
    return List(size = HOSTILE_RUN_SIZE) { index ->
        entry(
            id = HOSTILE_RUN_FIRST_ID + index,
            number = MISSED_NUMBER,
            callType = CallType.Missed,
            ageMillis = AGE_ONE_MINUTE + index,
            isRead = false,
        )
    }
}

private fun entry(
    id: Long,
    number: String,
    callType: CallType,
    ageMillis: Long,
    durationSeconds: Long = 0L,
    cachedName: String? = null,
    geocodedLocation: String? = null,
    numberPresentation: Int = CallLog.Calls.PRESENTATION_ALLOWED,
    isRead: Boolean = true,
): CallLogEntry {
    return CallLogEntry(
        entryId = CallLogEntryId(value = id),
        number = number,
        formattedNumber = null,
        numberPresentation = numberPresentation,
        geocodedLocation = geocodedLocation,
        cachedName = cachedName,
        photoUri = null,
        lookupUri = null,
        timestampMillis = SEED_NOW_MILLIS - ageMillis,
        durationSeconds = durationSeconds,
        features = 0,
        callType = callType,
        isRead = isRead,
    )
}
