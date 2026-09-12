package com.android.dialer.domain.recents.usecase

import com.android.dialer.phonenumberutil.PhoneNumberHelper
import javax.inject.Inject

internal fun interface CanPlaceCall {
    operator fun invoke(number: String, presentation: Int): Boolean
}

internal class CanPlaceCallImpl @Inject constructor() : CanPlaceCall {

    override fun invoke(number: String, presentation: Int): Boolean {
        return PhoneNumberHelper.canPlaceCallsTo(number, presentation)
    }
}
