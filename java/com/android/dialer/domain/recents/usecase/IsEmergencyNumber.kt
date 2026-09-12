package com.android.dialer.domain.recents.usecase

import android.telephony.TelephonyManager
import com.android.dialer.common.LogUtil
import javax.inject.Inject

internal fun interface IsEmergencyNumber {
    operator fun invoke(number: String): Boolean
}

internal class IsEmergencyNumberImpl @Inject constructor(
    private val telephonyManager: TelephonyManager?,
) : IsEmergencyNumber {

    override fun invoke(number: String): Boolean {
        if (number.isBlank() || telephonyManager == null) {
            return false
        }

        return try {
            telephonyManager.isEmergencyNumber(number)
        } catch (_: IllegalStateException) {
            LogUtil.e(TAG, "IsEmergencyNumberImpl: telephony unavailable")
            false
        }
    }

    private companion object {
        private const val TAG = "IsEmergencyNumberImpl"
    }
}
