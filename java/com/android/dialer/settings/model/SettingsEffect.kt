package com.android.dialer.settings.model

sealed interface SettingsEffect {
    data class OpenSoundSettings(
        val showCannotWriteSettingsWarning: Boolean,
    ) : SettingsEffect

    data object OpenQuickResponses : SettingsEffect
    data object OpenCallSettings : SettingsEffect
    data object OpenCallingAccounts : SettingsEffect
    data object OpenCallAccessibility : SettingsEffect
    data object OpenLicenses : SettingsEffect
    data object OpenPrivacyPolicy : SettingsEffect
    data object OpenTermsOfService : SettingsEffect
}
