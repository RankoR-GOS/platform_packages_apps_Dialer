package com.android.dialer.ui.recents.mapper

import android.content.Context
import android.provider.CallLog
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.text.TextUtils
import com.android.dialer.R
import com.android.dialer.compat.telephony.TelephonyManagerCompat
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences
import com.android.dialer.data.phone.formatter.PhoneNumberFormatter
import com.android.dialer.data.recents.model.CallLogEntry
import com.android.dialer.data.recents.model.CallType
import com.android.dialer.domain.recents.usecase.CanPlaceCall
import com.android.dialer.domain.recents.usecase.IsEmergencyNumber
import com.android.dialer.domain.recents.usecase.RelativeTimestampFormatter
import com.android.dialer.phonenumberutil.PhoneNumberHelper
import com.android.dialer.ui.recents.model.RecentsAvatarUiModel
import com.android.dialer.ui.recents.model.RecentsCallTypeIcon
import com.android.dialer.ui.recents.model.RecentsItemUiModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

internal interface RecentsItemUiMapper {
    fun map(entry: CallLogEntry, nowMillis: Long): RecentsItemUiModel
}

internal class RecentsItemUiMapperImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val phoneNumberFormatter: PhoneNumberFormatter,
    private val relativeTimestampFormatter: RelativeTimestampFormatter,
    private val canPlaceCall: CanPlaceCall,
    private val isEmergencyNumber: IsEmergencyNumber,
    private val contactDisplayPreferences: ContactDisplayPreferences,
) : RecentsItemUiMapper {

    override fun map(entry: CallLogEntry, nowMillis: Long): RecentsItemUiModel {
        return mapWithPreferredName(
            entry = entry.copy(
                cachedName = contactDisplayPreferences.getDisplayName(
                    entry.cachedName,
                    entry.alternativeName,
                ),
            ),
            nowMillis = nowMillis,
        )
    }

    private fun mapWithPreferredName(entry: CallLogEntry, nowMillis: Long): RecentsItemUiModel {
        val isEmergency = isEmergencyNumber(entry.number)
        val displayNumber = entry.displayNumber()
        val primaryText = entry.primaryText(
            isEmergency = isEmergency,
            displayNumber = displayNumber,
        )
        val spokenDisplayNumber = spokenDigits(number = displayNumber)
        val spokenPrimaryText = when (primaryText) {
            displayNumber -> spokenDisplayNumber
            else -> primaryText
        }
        val canCall = canPlaceCall(number = entry.number, presentation = entry.numberPresentation)
        val initial = entry.cachedName?.firstOrNull()?.takeIf { it.isLatinLetter() }

        return RecentsItemUiModel(
            entryId = entry.entryId,
            primaryText = primaryText,
            secondaryText = entry.secondaryText(
                nowMillis = nowMillis,
                isEmergency = isEmergency,
                isAbbreviated = true,
            ).joinToString(separator = SECONDARY_TEXT_SEPARATOR),
            displayNumber = displayNumber,
            spokenDisplayNumber = spokenDisplayNumber,
            contentDescription = entry.contentDescription(
                spokenPrimaryText = spokenPrimaryText,
                nowMillis = nowMillis,
                isEmergency = isEmergency,
            ),
            clickActionLabel = context.getString(R.string.a11y_new_call_log_entry_tap_action),
            callActionLabel = entry.callActionLabel(canCall = canCall, primaryText = primaryText),
            avatar = RecentsAvatarUiModel(
                photoUri = entry.photoUri,
                letter = initial?.uppercaseChar(),
            ),
            callTypeIcon = entry.callType.toIcon(),
            accountLabel = entry.accountLabelText(isSpoken = false),
            isHdCall = entry.features and CallLog.Calls.FEATURES_HD_CALL != 0,
            isRttCall = entry.features and CallLog.Calls.FEATURES_RTT != 0,
            isAssistedDialing =
                entry.features and TelephonyManagerCompat.FEATURES_ASSISTED_DIALING != 0,
            groupedCallCountLabel = entry.groupedCallCountLabel(),
            groupedEntryIds = entry.groupedEntryIds,
            number = entry.number,
            postDialDigits = entry.postDialDigits,
            isUnreadMissedCall = entry.callType == CallType.Missed && !entry.isRead,
            canCallBack = canCall,
            canVideoCall = entry.canVideoCall(canCall = canCall, isEmergency = isEmergency),
            isVideoCall = entry.isVideoCall && !isEmergency,
            accountComponentName = entry.accountComponentName,
            accountId = entry.accountId,
            canMessage = canCall && !isEmergency,
            canAddContact = canCall && !isEmergency && entry.lookupUri == null,
            canEditNumberBeforeCall = canCall && !PhoneNumberHelper.isUriNumber(entry.number),
        )
    }

    private fun CallLogEntry.canVideoCall(canCall: Boolean, isEmergency: Boolean): Boolean {
        return canCall && !isEmergency && (
            isVideoCall || (
                supportsVideoPresence && carrierPresence and Phone.CARRIER_PRESENCE_VT_CAPABLE != 0
                )
            )
    }

    private fun CallLogEntry.displayNumber(): String {
        return when {
            !formattedNumber.isNullOrBlank() -> formattedNumber
            number.isNotBlank() -> phoneNumberFormatter.formatForDisplay(
                number = number,
                countryIso = countryIso,
            ) + postDialDigits
            else -> ""
        }
    }

    private fun CallLogEntry.primaryText(isEmergency: Boolean, displayNumber: String): String {
        val presentationName = presentationName()

        return when {
            isEmergency -> context.getString(R.string.emergency_number)
            presentationName != null -> presentationName
            !cachedName.isNullOrBlank() -> cachedName
            displayNumber.isNotBlank() -> displayNumber
            else -> context.getString(R.string.new_call_log_unknown)
        }
    }

    private fun CallLogEntry.presentationName(): String? {
        val resId = when (numberPresentation) {
            CallLog.Calls.PRESENTATION_UNKNOWN -> R.string.unknown
            CallLog.Calls.PRESENTATION_RESTRICTED -> R.string.private_num_non_verizon
            CallLog.Calls.PRESENTATION_PAYPHONE -> R.string.payphone
            else -> return null
        }

        return context.getString(resId)
    }

    private fun CallLogEntry.secondaryText(
        nowMillis: Long,
        isEmergency: Boolean,
        isAbbreviated: Boolean,
    ): List<String> {
        val time = relativeTimestampFormatter(
            timestampMillis = timestampMillis,
            nowMillis = nowMillis,
            isAbbreviated = isAbbreviated,
        )
        val videoLabel = when {
            isVideoCall -> context.getString(R.string.new_call_log_carrier_video)
            else -> null
        }
        val descriptor = listOfNotNull(videoLabel, typeLabelOrLocation())
            .joinToString(separator = DESCRIPTOR_SEPARATOR)
            .takeIf { it.isNotEmpty() }

        return when {
            isEmergency -> listOf(time)
            else -> listOfNotNull(descriptor, time)
        }
    }

    private fun CallLogEntry.typeLabelOrLocation(): String? {
        val label = when {
            cachedName == null && geocodedLocation != null -> geocodedLocation
            numberType == Phone.TYPE_CUSTOM && numberLabel.isNullOrEmpty() -> null
            else -> Phone.getTypeLabel(context.resources, numberType, numberLabel)
                .toString()
                .takeIf { it.isNotBlank() }
        }

        return label ?: displayNumber().takeIf { cachedName != null && it.isNotBlank() }
    }

    private fun CallLogEntry.contentDescription(
        spokenPrimaryText: String,
        nowMillis: Long,
        isEmergency: Boolean,
    ): String {
        val primaryDescription = TextUtils.expandTemplate(
            context.resources.getQuantityString(callType.toDescriptionPlurals(), groupedCallCount),
            groupedCallCount.toString(),
            spokenPrimaryText,
        )
        val secondaryDescription = secondaryText(
            nowMillis = nowMillis,
            isEmergency = isEmergency,
            isAbbreviated = false,
        ).joinToString(separator = DESCRIPTOR_SEPARATOR)

        val accountDescription = accountLabelText(isSpoken = true)
        val template = when (accountDescription) {
            null -> R.string.a11y_new_call_log_entry_full_description_without_phone_account_info
            else -> R.string.a11y_new_call_log_entry_full_description_with_phone_account_info
        }

        return TextUtils.expandTemplate(
            context.resources.getText(template),
            primaryDescription,
            secondaryDescription,
            accountDescription.orEmpty(),
        ).toString()
    }

    private fun CallLogEntry.accountLabelText(isSpoken: Boolean): String? {
        val via = viaNumber.takeIf { it.isNotBlank() }?.let { number ->
            if (isSpoken) spokenDigits(number) else number
        }

        return when {
            via != null && !accountLabel.isNullOrBlank() -> context.getString(
                if (isSpoken) {
                    R.string.description_via_number_phone_account
                } else {
                    R.string.call_log_via_number_phone_account
                },
                accountLabel,
                via,
            )
            via != null -> context.getString(
                if (isSpoken) R.string.description_via_number else R.string.call_log_via_number,
                via,
            )
            !accountLabel.isNullOrBlank() && isSpoken -> TextUtils.expandTemplate(
                context.getText(R.string.description_phone_account),
                accountLabel,
            ).toString()
            else -> accountLabel?.takeIf { it.isNotBlank() }
        }
    }

    private fun CallLogEntry.callActionLabel(canCall: Boolean, primaryText: String): String? {
        val resId = when {
            !canCall -> return null
            isVideoCall -> R.string.description_video_call_action
            else -> R.string.description_call_action
        }

        return TextUtils.expandTemplate(context.getText(resId), primaryText).toString()
    }

    private fun spokenDigits(number: String): String {
        if (PhoneNumberHelper.isUriNumber(number)) {
            return number
        }

        val digits = number.filter { it.isDigit() || it == '+' }

        return when {
            digits.isEmpty() -> number
            else -> digits.map { it }.joinToString(separator = " ")
        }
    }

    private fun CallType.toDescriptionPlurals(): Int {
        return when (this) {
            CallType.Answered -> R.plurals.a11y_new_call_log_entry_answered_call
            CallType.Outgoing -> R.plurals.a11y_new_call_log_entry_outgoing_call
            CallType.Blocked -> R.plurals.a11y_new_call_log_entry_blocked_call
            CallType.Missed,
            CallType.Rejected,
            CallType.Voicemail,
            is CallType.Unknown,
            -> R.plurals.a11y_new_call_log_entry_missed_call
        }
    }

    private fun CallType.toIcon(): RecentsCallTypeIcon {
        return when (this) {
            CallType.Answered -> RecentsCallTypeIcon.Incoming
            CallType.Outgoing -> RecentsCallTypeIcon.Outgoing
            CallType.Blocked -> RecentsCallTypeIcon.Blocked
            CallType.Voicemail -> RecentsCallTypeIcon.Voicemail
            CallType.Missed, CallType.Rejected, is CallType.Unknown -> RecentsCallTypeIcon.Missed
        }
    }

    private fun CallLogEntry.groupedCallCountLabel(): String? {
        return when {
            groupedCallCount > 1 -> String.format(Locale.getDefault(), "(%d)", groupedCallCount)
            else -> null
        }
    }

    private fun Char.isLatinLetter(): Boolean {
        return this in 'A'..'Z' || this in 'a'..'z'
    }

    private companion object {
        private const val SECONDARY_TEXT_SEPARATOR = " • "
        private const val DESCRIPTOR_SEPARATOR = ", "
    }
}
