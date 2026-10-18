package com.android.dialer.settings.data

interface SettingsEnvironment {

    val isNameOrderChangeable: Boolean

    val isPrimaryUser: Boolean

    val phoneCount: Int

    val isCallAccessibilitySupported: Boolean

    val versionName: String

    fun canWriteSystemSettings(): Boolean
}
