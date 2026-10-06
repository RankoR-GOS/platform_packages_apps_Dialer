package com.android.dialer.keypad.domain

import android.content.Context
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telephony.TelephonyManager
import com.android.dialer.common.LogUtil
import com.android.dialer.data.core.store.DeviceSettings
import com.android.dialer.di.core.IoDispatcher
import com.android.dialer.telecom.TelecomUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal interface VoicemailAvailability {

    suspend fun isVoicemailReachable(): Boolean

    suspend fun isAirplaneModeOn(): Boolean
}

internal class VoicemailAvailabilityImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val telephonyManager: TelephonyManager,
    private val deviceSettings: DeviceSettings,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : VoicemailAvailability {

    override suspend fun isVoicemailReachable(): Boolean {
        return withContext(ioDispatcher) {
            hasMultipleSimsWithoutDefault() || hasVoicemailNumber()
        }
    }

    override suspend fun isAirplaneModeOn(): Boolean {
        return withContext(ioDispatcher) {
            deviceSettings.isAirplaneModeOn()
        }
    }

    // Reachable without a known number: Telecom then asks which SIM to call with.
    private fun hasMultipleSimsWithoutDefault(): Boolean {
        return try {
            val accounts = TelecomUtil.getSubscriptionPhoneAccounts(context)
            accounts.size > 1 && defaultVoicemailAccount() !in accounts
        } catch (e: SecurityException) {
            LogUtil.w(TAG, "Cannot read the subscription phone accounts: $e")
            false
        }
    }

    private fun hasVoicemailNumber(): Boolean {
        return try {
            val number = when (val account = defaultVoicemailAccount()) {
                null -> telephonyManager.voiceMailNumber
                else -> TelecomUtil.getVoicemailNumber(context, account)
            }
            !number.isNullOrEmpty()
        } catch (e: SecurityException) {
            LogUtil.w(TAG, "Cannot read the voicemail number: $e")
            false
        }
    }

    private fun defaultVoicemailAccount(): PhoneAccountHandle? {
        return TelecomUtil.getDefaultOutgoingPhoneAccount(context, PhoneAccount.SCHEME_VOICEMAIL)
    }

    private companion object {
        private const val TAG = "VoicemailAvailability"
    }
}
