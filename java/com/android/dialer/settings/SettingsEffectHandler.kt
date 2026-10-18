package com.android.dialer.settings

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.core.net.toUri
import com.android.dialer.R
import com.android.dialer.about.LicenseMenuActivity
import com.android.dialer.common.LogUtil
import com.android.dialer.settings.model.SettingsEffect as Effect

internal class SettingsEffectHandler(
    private val activity: Activity,
) {

    fun handle(effect: Effect) {
        if (effect is Effect.OpenSoundSettings && effect.showCannotWriteSettingsWarning) {
            Toast.makeText(
                activity,
                R.string.toast_cannot_write_system_settings,
                Toast.LENGTH_SHORT,
            ).show()
        }
        start(intentFor(effect))
    }

    private fun intentFor(effect: Effect): Intent = when (effect) {
        // "Sounds and vibration" goes directly to the system sound settings. This helps since:
        // - Dialer doesn't need its own sounds and vibration screen, as everything it needs is
        //   present in the system sound settings.
        // - OEMs, e.g. Moto, that support dual SIM ringtones don't need to update Dialer.
        is Effect.OpenSoundSettings -> Intent(Settings.ACTION_SOUND_SETTINGS)
        Effect.OpenQuickResponses -> Intent(TelecomManager.ACTION_SHOW_RESPOND_VIA_SMS_SETTINGS)
        Effect.OpenCallSettings -> Intent(TelecomManager.ACTION_SHOW_CALL_SETTINGS)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        Effect.OpenCallingAccounts -> Intent(TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        Effect.OpenCallAccessibility ->
            Intent(TelecomManager.ACTION_SHOW_CALL_ACCESSIBILITY_SETTINGS)
        // Targets the class rather than a package name, as the package differs between AOSP and
        // the GoogleDialer.
        Effect.OpenLicenses -> Intent(activity, LicenseMenuActivity::class.java)
        Effect.OpenPrivacyPolicy -> viewUrl(R.string.privacy_policy_url)
        Effect.OpenTermsOfService -> viewUrl(R.string.terms_of_service_url)
    }

    private fun viewUrl(urlRes: Int): Intent =
        Intent(Intent.ACTION_VIEW, activity.getString(urlRes).toUri())

    private fun start(intent: Intent) {
        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            LogUtil.e("SettingsEffectHandler.start", "no activity for ${intent.action}", e)
        }
    }
}
