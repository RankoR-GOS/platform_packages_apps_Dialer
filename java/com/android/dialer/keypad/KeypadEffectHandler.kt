package com.android.dialer.keypad

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.android.contacts.common.dialog.CallSubjectDialog
import com.android.dialer.callintent.CallInitiationType
import com.android.dialer.callintent.CallIntentBuilder
import com.android.dialer.common.LogUtil
import com.android.dialer.dialpadview.SpecialCharSequenceMgr
import com.android.dialer.keypad.model.KeypadScreenEffect as Effect
import com.android.dialer.precall.PreCall

@Composable
internal fun rememberKeypadEffectHandler(
    screenModel: KeypadScreenModel,
    onCallPlaced: () -> Unit,
): KeypadEffectHandler {
    val activity = checkNotNull(LocalActivity.current)
    val currentOnCallPlaced = rememberUpdatedState(newValue = onCallPlaced)

    return remember(activity, screenModel) {
        KeypadEffectHandlerImpl(
            activity = activity,
            screenModel = screenModel,
            onCallPlaced = { currentOnCallPlaced.value() },
        )
    }
}

internal interface KeypadEffectHandler {
    fun handle(effect: Effect)
}

internal class KeypadEffectHandlerImpl(
    private val activity: Activity,
    private val screenModel: KeypadScreenModel,
    private val onCallPlaced: () -> Unit,
) : KeypadEffectHandler {

    override fun handle(effect: Effect) {
        when (effect) {
            is Effect.PlaceCall -> {
                placeCall(
                    builder = CallIntentBuilder(effect.number, CallInitiationType.Type.DIALPAD),
                )
            }
            Effect.CallVoicemail -> {
                placeCall(
                    builder = CallIntentBuilder.forVoicemail(CallInitiationType.Type.DIALPAD),
                )
            }
            is Effect.CallWithNote -> {
                CallSubjectDialog.start(activity, effect.number)
                onCallPlaced()
            }
            is Effect.RunSpecialCode -> runSpecialCode(input = effect.input)
            Effect.OpenAirplaneModeSettings -> {
                openSettings(intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS))
            }
            Effect.OpenVoicemailSettings -> {
                openSettings(intent = Intent(TelephonyManager.ACTION_CONFIGURE_VOICEMAIL))
            }
        }
    }

    private fun placeCall(builder: CallIntentBuilder) {
        PreCall.start(activity, builder)
        onCallPlaced()
    }

    private fun runSpecialCode(input: String) {
        val handled = try {
            // The screen model, not this handler, receives the number: a SIM contact lookup can
            // outlive the Activity this handler was built with.
            SpecialCharSequenceMgr.handleChars(activity, input) { number ->
                screenModel.insertSimContactNumber(number)
            }
        } catch (e: SecurityException) {
            // *#06# needs READ_PRIVILEGED_PHONE_STATE, which only a system install holds.
            LogUtil.w(TAG, "Cannot run the special code: $e")
            false
        }
        if (handled) {
            screenModel.onSpecialCodeHandled(input = input)
        }
    }

    private fun openSettings(intent: Intent) {
        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            LogUtil.w(TAG, "No settings screen for ${intent.action}: $e")
        }
    }

    private companion object {
        private const val TAG = "KeypadEffectHandler"
    }
}
