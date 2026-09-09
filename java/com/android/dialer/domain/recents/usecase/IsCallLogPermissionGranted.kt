package com.android.dialer.domain.recents.usecase

import android.content.Context
import com.android.dialer.util.PermissionsUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal fun interface IsCallLogPermissionGranted {
    operator fun invoke(): Boolean
}

internal class IsCallLogPermissionGrantedImpl @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
) : IsCallLogPermissionGranted {

    override fun invoke(): Boolean {
        return PermissionsUtil.hasCallLogReadPermissions(context)
    }
}

internal fun interface IsContactsPermissionGranted {
    operator fun invoke(): Boolean
}

internal class IsContactsPermissionGrantedImpl @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
) : IsContactsPermissionGranted {

    override fun invoke(): Boolean {
        return PermissionsUtil.hasContactsReadPermissions(context)
    }
}
