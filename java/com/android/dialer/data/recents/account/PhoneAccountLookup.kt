package com.android.dialer.data.recents.account

import android.content.Context
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import com.android.dialer.telecom.TelecomUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal data class PhoneAccountSnapshot(
    val labels: Map<PhoneAccountHandle, String> = emptyMap(),
    val supportsVideoPresence: Boolean = false,
)

internal interface PhoneAccountLookup {
    operator fun invoke(): PhoneAccountSnapshot

    fun isVoicemailNumber(handle: PhoneAccountHandle?, number: String): Boolean
}

internal class PhoneAccountLookupImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : PhoneAccountLookup {

    override fun isVoicemailNumber(handle: PhoneAccountHandle?, number: String): Boolean {
        return try {
            number.isNotBlank() && TelecomUtil.hasReadPhoneStatePermission(context) &&
                TelecomUtil.isVoicemailNumber(context, handle, number)
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    override fun invoke(): PhoneAccountSnapshot {
        return try {
            val handles = TelecomUtil.getCallCapablePhoneAccounts(context)
            val accounts = handles.mapNotNull { handle ->
                TelecomUtil.getPhoneAccount(context, handle)?.let { handle to it }
            }
            val videoAccount = accounts.firstOrNull { (_, account) ->
                account.hasCapabilities(PhoneAccount.CAPABILITY_VIDEO_CALLING)
            }?.second

            PhoneAccountSnapshot(
                labels = when {
                    handles.size > 1 -> accounts.mapNotNull { (handle, account) ->
                        account.label?.toString()?.takeIf { it.isNotBlank() }?.let { handle to it }
                    }.toMap()
                    else -> emptyMap()
                },
                supportsVideoPresence = videoAccount?.hasCapabilities(
                    PhoneAccount.CAPABILITY_VIDEO_CALLING_RELIES_ON_PRESENCE,
                ) == true,
            )
        } catch (_: SecurityException) {
            PhoneAccountSnapshot()
        } catch (_: IllegalArgumentException) {
            PhoneAccountSnapshot()
        }
    }
}
