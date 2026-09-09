package com.android.dialer.domain.recents.usecase

import android.content.Context
import com.android.dialer.calllogutils.CallLogDates
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal fun interface RelativeTimestampFormatter {
    operator fun invoke(timestampMillis: Long, nowMillis: Long, isAbbreviated: Boolean): String
}

internal class RelativeTimestampFormatterImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : RelativeTimestampFormatter {

    override fun invoke(timestampMillis: Long, nowMillis: Long, isAbbreviated: Boolean): String {
        return CallLogDates.newCallLogTimestampLabel(
            context,
            nowMillis,
            timestampMillis,
            isAbbreviated,
        ).toString()
    }
}
