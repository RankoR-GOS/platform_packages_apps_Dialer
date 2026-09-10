package com.android.dialer.domain.recents.usecase

import android.content.Context
import android.os.Build
import com.android.dialer.R
import com.android.dialer.testutil.TEST_TIMESTAMP_MILLIS
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import java.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RelativeTimestampFormatterImplTest {

    private val context = mockk<Context>()
    private val defaultLocale = Locale.getDefault()
    private val defaultTimeZone = TimeZone.getDefault()

    @Before
    fun setUp() {
        Locale.setDefault(Locale.US)
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        every { context.getString(R.string.just_now) } returns JUST_NOW
    }

    @After
    fun tearDown() {
        Locale.setDefault(defaultLocale)
        TimeZone.setDefault(defaultTimeZone)
    }

    @Test
    fun invoke_underAMinuteAgo_returnsJustNow() {
        val formatter = createFormatter()

        val label = formatter(
            timestampMillis = NOW_MILLIS - HALF_A_MINUTE_MILLIS,
            nowMillis = NOW_MILLIS,
            isAbbreviated = true,
        )

        assertEquals(JUST_NOW, label)
    }

    @Test
    fun invoke_minutesAgo_abbreviatesOnlyWhenAsked() {
        val formatter = createFormatter()

        val label = formatter(
            timestampMillis = NOW_MILLIS - FIVE_MINUTES_MILLIS,
            nowMillis = NOW_MILLIS,
            isAbbreviated = true,
        )

        assertEquals("5 min ago", label)
        assertEquals(
            "5 minutes ago",
            formatter(
                timestampMillis = NOW_MILLIS - FIVE_MINUTES_MILLIS,
                nowMillis = NOW_MILLIS,
                isAbbreviated = false,
            ),
        )
    }

    @Test
    fun invoke_threeDaysAgo_returnsTheWeekdayShortOrFull() {
        val formatter = createFormatter()

        val label = formatter(
            timestampMillis = NOW_MILLIS - THREE_DAYS_MILLIS,
            nowMillis = NOW_MILLIS,
            isAbbreviated = true,
        )

        assertEquals("Thu", label)
        assertEquals(
            "Thursday",
            formatter(
                timestampMillis = NOW_MILLIS - THREE_DAYS_MILLIS,
                nowMillis = NOW_MILLIS,
                isAbbreviated = false,
            ),
        )
    }

    @Test
    fun invoke_aMinuteBeforeMidnightUtc_returnsThePreviousWeekday() {
        val formatter = createFormatter()

        val label = formatter(
            timestampMillis = NOW_MILLIS - MILLIS_SINCE_MIDNIGHT - ONE_MINUTE_MILLIS,
            nowMillis = NOW_MILLIS,
            isAbbreviated = true,
        )

        assertEquals("Sat", label)
    }

    @Test
    fun invoke_twoCalendarDaysAcrossTheNewYorkSpringForward_returnsTheWeekdayNotYesterday() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
        val formatter = createFormatter()

        val label = formatter(
            timestampMillis = SPRING_FORWARD_TWO_CALENDAR_DAYS_EARLIER_MILLIS,
            nowMillis = SPRING_FORWARD_NOW_MILLIS,
            isAbbreviated = false,
        )

        assertEquals("Saturday", label)
    }

    private fun createFormatter(): RelativeTimestampFormatterImpl {
        return RelativeTimestampFormatterImpl(context = context)
    }

    private companion object {
        private const val JUST_NOW = "Just now"
        private const val NOW_MILLIS = TEST_TIMESTAMP_MILLIS
        private const val HALF_A_MINUTE_MILLIS = 30_000L
        private const val FIVE_MINUTES_MILLIS = 300_000L
        private const val THREE_DAYS_MILLIS = 3 * 86_400_000L
        private const val ONE_MINUTE_MILLIS = 60_000L
        private const val MILLIS_SINCE_MIDNIGHT = 48_000_000L
        private const val SPRING_FORWARD_NOW_MILLIS = 1_773_072_000_000L
        private const val SPRING_FORWARD_TWO_CALENDAR_DAYS_EARLIER_MILLIS = 1_772_902_800_000L
    }
}
