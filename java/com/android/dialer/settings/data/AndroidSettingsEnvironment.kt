package com.android.dialer.settings.data

import android.content.Context
import android.os.UserManager
import android.provider.Settings
import android.telephony.TelephonyManager
import com.android.dialer.R
import com.android.dialer.compat.telephony.TelephonyManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidSettingsEnvironment @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsEnvironment {

    private val telephonyManager: TelephonyManager
        get() = context.getSystemService(TelephonyManager::class.java)

    // For languages such as Chinese, Japanese, or Korean, display options aren't useful since
    // contacts are sorted and displayed family name first by default.
    override val isNameOrderChangeable: Boolean
        get() = context.resources.getBoolean(R.bool.config_display_order_user_changeable) &&
            context.resources.getBoolean(R.bool.config_sort_order_user_changeable)

    override val isPrimaryUser: Boolean
        get() = context.getSystemService(UserManager::class.java).isSystemUser

    override val phoneCount: Int
        get() = TelephonyManagerCompat.getPhoneCount(telephonyManager)

    override val isCallAccessibilitySupported: Boolean
        get() = TelephonyManagerCompat.isTtyModeSupported(telephonyManager) ||
            TelephonyManagerCompat.isHearingAidCompatibilitySupported(telephonyManager)

    override val versionName: String
        get() = context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()

    override fun canWriteSystemSettings(): Boolean = Settings.System.canWrite(context)
}
