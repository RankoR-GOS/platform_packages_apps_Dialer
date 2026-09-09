package com.android.dialer.domain.recents.usecase

import android.content.Context
import com.android.dialer.util.PermissionsUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

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
