package com.android.dialer.settings

import com.android.dialer.settings.model.CallAccountsEntry

internal data class SettingsStaticState(
    val isNameOrderChangeable: Boolean,
    val callAccountsEntry: CallAccountsEntry,
    val isAccessibilityVisible: Boolean,
    val versionName: String,
)
