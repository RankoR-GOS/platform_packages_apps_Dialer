package com.android.dialer.ui.recents.screen

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.PersistableBundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.android.dialer.callintent.CallInitiationType
import com.android.dialer.callintent.CallIntentBuilder
import com.android.dialer.precall.PreCall
import com.android.dialer.ui.recents.model.RecentsEffect
import com.android.dialer.util.DialerUtils
import com.android.dialer.util.IntentUtil

internal interface RecentsEffectHandler {
    fun handle(effect: RecentsEffect)
}

@Composable
internal fun rememberRecentsEffectHandler(): RecentsEffectHandler {
    val context = LocalContext.current

    return remember(context) {
        RecentsEffectHandlerImpl(context = context)
    }
}

internal class RecentsEffectHandlerImpl(
    private val context: Context,
) : RecentsEffectHandler {

    override fun handle(effect: RecentsEffect) {
        when (effect) {
            is RecentsEffect.PlaceCall -> placeCall(number = effect.number, isVideoCall = false)
            is RecentsEffect.PlaceVideoCall -> placeCall(number = effect.number, isVideoCall = true)
            is RecentsEffect.SendMessage -> startActivity(
                intent = IntentUtil.getSendSmsIntent(effect.number),
            )
            is RecentsEffect.AddContact -> startActivity(
                intent = IntentUtil.getAddToExistingContactIntent(effect.number),
            )
            is RecentsEffect.CopyNumber -> copyNumber(number = effect.number)
            RecentsEffect.RequestCallLogPermission, RecentsEffect.WriteFailed -> Unit
        }
    }

    private fun placeCall(number: String, isVideoCall: Boolean) {
        val builder = CallIntentBuilder(number, CallInitiationType.Type.CALL_LOG)
            .setIsVideoCall(isVideoCall)

        PreCall.start(context, builder)
    }

    private fun copyNumber(number: String) {
        val clip = ClipData.newPlainText(null, number).apply {
            description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }

        context.getSystemService(ClipboardManager::class.java)?.setPrimaryClip(clip)
    }

    private fun startActivity(intent: Intent) {
        DialerUtils.startActivityWithErrorToast(context, intent)
    }
}
