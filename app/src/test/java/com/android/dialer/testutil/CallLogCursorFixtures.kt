package com.android.dialer.testutil

import android.database.MatrixCursor
import android.provider.CallLog.Calls
import com.android.dialer.data.recents.repository.RecentsRepositoryImpl

internal const val TEST_TIMESTAMP_MILLIS = 1_806_240_000_000L

internal const val TEST_CALL_DURATION_SECONDS = 65L

internal fun callLogCursor(vararg rows: TestCallLogRow): MatrixCursor {
    val projection = RecentsRepositoryImpl.CALL_LOG_PROJECTION
    val cursor = MatrixCursor(projection)

    rows.forEach { row ->
        val values = row.toColumnValues()
        cursor.addRow(projection.map { column -> values[column] }.toTypedArray())
    }

    return cursor
}

internal fun callLogRow(
    id: Long,
    number: String? = "+1555000$id",
    formattedNumber: String? = null,
    numberPresentation: Int = Calls.PRESENTATION_ALLOWED,
    callType: Int = Calls.INCOMING_TYPE,
    date: Long = TEST_TIMESTAMP_MILLIS + id,
    duration: Long = TEST_CALL_DURATION_SECONDS,
    features: Int = 0,
    geocodedLocation: String? = null,
    cachedName: String? = null,
    cachedPhotoUri: String? = null,
    cachedLookupUri: String? = null,
    isRead: Int = 1,
): TestCallLogRow {
    return TestCallLogRow(
        id = id,
        number = number,
        formattedNumber = formattedNumber,
        numberPresentation = numberPresentation,
        callType = callType,
        date = date,
        duration = duration,
        features = features,
        geocodedLocation = geocodedLocation,
        cachedName = cachedName,
        cachedPhotoUri = cachedPhotoUri,
        cachedLookupUri = cachedLookupUri,
        isRead = isRead,
    )
}

internal data class TestCallLogRow(
    val id: Long,
    val number: String?,
    val formattedNumber: String?,
    val numberPresentation: Int,
    val callType: Int,
    val date: Long,
    val duration: Long,
    val features: Int,
    val geocodedLocation: String?,
    val cachedName: String?,
    val cachedPhotoUri: String?,
    val cachedLookupUri: String?,
    val isRead: Int,
) {

    fun toColumnValues(): Map<String, Any?> {
        return mapOf(
            Calls._ID to id,
            Calls.NUMBER to number,
            Calls.CACHED_FORMATTED_NUMBER to formattedNumber,
            Calls.NUMBER_PRESENTATION to numberPresentation,
            Calls.TYPE to callType,
            Calls.DATE to date,
            Calls.DURATION to duration,
            Calls.FEATURES to features,
            Calls.GEOCODED_LOCATION to geocodedLocation,
            Calls.CACHED_NAME to cachedName,
            Calls.CACHED_PHOTO_URI to cachedPhotoUri,
            Calls.CACHED_LOOKUP_URI to cachedLookupUri,
            Calls.IS_READ to isRead,
        )
    }
}
