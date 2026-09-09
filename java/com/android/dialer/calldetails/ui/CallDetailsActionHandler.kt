package com.android.dialer.calldetails.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.android.dialer.callintent.CallInitiationType
import com.android.dialer.callintent.CallIntentBuilder
import com.android.dialer.clipboard.ClipboardUtils
import com.android.dialer.common.LogUtil
import com.android.dialer.precall.PreCall
import com.android.dialer.util.CallUtil
import com.android.dialer.util.DialerUtils
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

internal interface CallDetailsActionHandler {
    fun placeVoiceCall(
        phoneNumber: String,
        postDialDigits: String = "",
    )

    fun placeVideoCall(
        phoneNumber: String,
    )

    fun sendSms(
        phoneNumber: String,
    )

    fun copyNumber(
        phoneNumber: String,
    )

    fun editNumber(
        phoneNumber: String,
    )

    fun openContact(
        contactUri: String,
    )
}

@Singleton
internal class CallDetailsActionHandlerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : CallDetailsActionHandler {

    override fun placeVoiceCall(
        phoneNumber: String,
        postDialDigits: String,
    ) {
        if (phoneNumber.isBlank()) return
        val fullNumber = "$phoneNumber$postDialDigits"
        val builder = CallIntentBuilder(fullNumber, CallInitiationType.Type.CALL_DETAILS)
        PreCall.start(context, builder)
    }

    override fun placeVideoCall(
        phoneNumber: String,
    ) {
        if (phoneNumber.isBlank()) return
        val builder = CallIntentBuilder(phoneNumber, CallInitiationType.Type.CALL_DETAILS)
            .setIsVideoCall(true)
        PreCall.start(context, builder)
    }

    override fun sendSms(
        phoneNumber: String,
    ) {
        if (phoneNumber.isBlank()) return
        val smsIntent = Intent(
            Intent.ACTION_SENDTO,
            Uri.fromParts("smsto", phoneNumber, null),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivitySafely(smsIntent)
    }

    override fun copyNumber(
        phoneNumber: String,
    ) {
        if (phoneNumber.isBlank()) return
        ClipboardUtils.copyText(context, null, phoneNumber, true)
    }

    override fun editNumber(
        phoneNumber: String,
    ) {
        if (phoneNumber.isBlank()) return
        val dialIntent = Intent(Intent.ACTION_DIAL, CallUtil.getCallUri(phoneNumber)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivitySafely(dialIntent)
    }

    override fun openContact(
        contactUri: String,
    ) {
        if (contactUri.isBlank()) return
        val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(contactUri)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivitySafely(viewIntent)
    }

    private fun startActivitySafely(intent: Intent) {
        try {
            DialerUtils.startActivityWithErrorToast(context, intent)
        } catch (e: ActivityNotFoundException) {
            LogUtil.e(TAG, "No activity found to handle intent", e)
        } catch (e: SecurityException) {
            LogUtil.e(TAG, "Permission denied starting activity", e)
        }
    }

    private companion object {
        const val TAG = "CallDetailsActionHandler"
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CallDetailsActionModule {

    @Binds
    @Singleton
    abstract fun bindCallDetailsActionHandler(
        impl: CallDetailsActionHandlerImpl,
    ): CallDetailsActionHandler
}
