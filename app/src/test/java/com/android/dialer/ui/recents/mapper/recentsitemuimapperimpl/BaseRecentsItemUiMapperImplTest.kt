package com.android.dialer.ui.recents.mapper.recentsitemuimapperimpl

import android.content.Context
import android.content.res.Resources
import com.android.dialer.R
import com.android.dialer.data.phone.formatter.PhoneNumberFormatter
import com.android.dialer.data.recents.model.CallLogEntry
import com.android.dialer.domain.recents.usecase.CanPlaceCall
import com.android.dialer.domain.recents.usecase.IsEmergencyNumber
import com.android.dialer.domain.recents.usecase.RelativeTimestampFormatter
import com.android.dialer.testutil.TEST_TIMESTAMP_MILLIS
import com.android.dialer.ui.recents.mapper.RecentsItemUiMapperImpl
import com.android.dialer.ui.recents.model.RecentsItemUiModel
import io.mockk.every
import io.mockk.mockk
import org.junit.Before

@Suppress("AbstractClassCanBeConcreteClass")
internal abstract class BaseRecentsItemUiMapperImplTest {

    protected val context = mockk<Context>()
    protected val resources = mockk<Resources>()
    protected val phoneNumberFormatter = mockk<PhoneNumberFormatter>()
    protected val relativeTimestampFormatter = mockk<RelativeTimestampFormatter>()
    protected val canPlaceCall = mockk<CanPlaceCall>()
    protected val isEmergencyNumber = mockk<IsEmergencyNumber>()

    @Before
    fun setUp() {
        every { context.resources } returns resources
        every { context.getString(any()) } answers { "string-${firstArg<Int>()}" }
        every { resources.getQuantityString(any(), any()) } answers {
            "plurals-${firstArg<Int>()}-^1-^2"
        }
        every { resources.getText(any()) } returns "^1; ^2"
        every { phoneNumberFormatter.formatForDisplay(any()) } answers {
            "formatted ${firstArg<String>()}"
        }
        every { relativeTimestampFormatter(any(), NOW_MILLIS, true) } returns SHORT_TIME
        every { relativeTimestampFormatter(any(), NOW_MILLIS, false) } returns LONG_TIME
        every { canPlaceCall(any(), any()) } returns true
        every { isEmergencyNumber(any()) } returns false
    }

    protected fun map(entry: CallLogEntry): RecentsItemUiModel {
        return createMapper().map(entry = entry, nowMillis = NOW_MILLIS)
    }

    protected fun string(resId: Int): String {
        return "string-$resId"
    }

    private fun createMapper(): RecentsItemUiMapperImpl {
        return RecentsItemUiMapperImpl(
            context = context,
            phoneNumberFormatter = phoneNumberFormatter,
            relativeTimestampFormatter = relativeTimestampFormatter,
            canPlaceCall = canPlaceCall,
            isEmergencyNumber = isEmergencyNumber,
        )
    }

    protected companion object {
        const val NOW_MILLIS = TEST_TIMESTAMP_MILLIS + 60_000L
        const val SHORT_TIME = "5 min ago"
        const val LONG_TIME = "5 minutes ago"
        const val LOCATION = "Kingston, Jamaica"
        val VIDEO_LABEL = "string-${R.string.new_call_log_carrier_video}"
    }
}
