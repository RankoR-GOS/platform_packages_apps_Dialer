package com.android.dialer.domain.recents.usecase

internal fun interface IsCallLogPermissionGranted {
    operator fun invoke(): Boolean
}
