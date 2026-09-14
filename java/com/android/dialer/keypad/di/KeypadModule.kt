package com.android.dialer.keypad.di

import com.android.dialer.keypad.domain.CallWithNoteAvailability
import com.android.dialer.keypad.domain.CallWithNoteAvailabilityImpl
import com.android.dialer.keypad.domain.CheckIfNumberIsProhibited
import com.android.dialer.keypad.domain.CheckIfNumberIsProhibitedImpl
import com.android.dialer.keypad.domain.DialIntentNumber
import com.android.dialer.keypad.domain.DialIntentNumberImpl
import com.android.dialer.keypad.domain.DtmfTonePlayer
import com.android.dialer.keypad.domain.DtmfTonePlayerImpl
import com.android.dialer.keypad.domain.EmergencyCallWarning
import com.android.dialer.keypad.domain.EmergencyCallWarningImpl
import com.android.dialer.keypad.domain.LastOutgoingCall
import com.android.dialer.keypad.domain.LastOutgoingCallImpl
import com.android.dialer.keypad.domain.PhoneNumberFormatting
import com.android.dialer.keypad.domain.PhoneNumberFormattingImpl
import com.android.dialer.keypad.domain.ToneGeneratorFactory
import com.android.dialer.keypad.domain.ToneGeneratorFactoryImpl
import com.android.dialer.keypad.domain.Vibration
import com.android.dialer.keypad.domain.VibrationImpl
import com.android.dialer.keypad.domain.VoicemailAvailability
import com.android.dialer.keypad.domain.VoicemailAvailabilityImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Application-context bindings only. What needs an `Activity` (`PreCall`, `SpecialCharSequenceMgr`)
 * is an effect carried out by the fragment, never a binding.
 */
@Suppress("Unused")
@Module
@InstallIn(SingletonComponent::class)
internal interface KeypadModule {

    @Binds
    fun bindDtmfTonePlayer(player: DtmfTonePlayerImpl): DtmfTonePlayer

    @Binds
    fun bindToneGeneratorFactory(factory: ToneGeneratorFactoryImpl): ToneGeneratorFactory

    @Binds
    fun bindVoicemailAvailability(availability: VoicemailAvailabilityImpl): VoicemailAvailability

    @Binds
    fun bindLastOutgoingCall(lastOutgoingCall: LastOutgoingCallImpl): LastOutgoingCall

    @Binds
    fun bindEmergencyCallWarning(warning: EmergencyCallWarningImpl): EmergencyCallWarning

    @Binds
    fun bindPhoneNumberFormatting(formatting: PhoneNumberFormattingImpl): PhoneNumberFormatting

    @Binds
    fun bindCheckIfNumberIsProhibited(
        check: CheckIfNumberIsProhibitedImpl,
    ): CheckIfNumberIsProhibited

    @Binds
    fun bindDialIntentNumber(number: DialIntentNumberImpl): DialIntentNumber

    @Binds
    fun bindVibration(vibration: VibrationImpl): Vibration

    @Binds
    fun bindCallWithNoteAvailability(
        availability: CallWithNoteAvailabilityImpl,
    ): CallWithNoteAvailability
}
