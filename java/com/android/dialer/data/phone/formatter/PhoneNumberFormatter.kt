package com.android.dialer.data.phone.formatter

import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import java.util.Locale
import javax.inject.Inject

internal interface PhoneNumberFormatter {
    fun formatForDisplay(number: String): String
}

internal class PhoneNumberFormatterImpl @Inject constructor(
    private val telephonyManager: TelephonyManager?,
) : PhoneNumberFormatter {

    override fun formatForDisplay(number: String): String {
        val countryIso = telephonyManager?.networkCountryIso.orEmpty()
            .ifEmpty { Locale.getDefault().country }
            .uppercase(Locale.ROOT)

        return PhoneNumberUtils.formatNumber(number, countryIso) ?: number
    }
}
