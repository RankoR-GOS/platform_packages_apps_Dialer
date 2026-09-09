package com.android.dialer.domain.recents.usecase

import android.content.Context
import com.android.dialer.util.PermissionsUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal fun interface IsPermissionGranted {
    operator fun invoke(permission: String): Boolean
}

internal class IsPermissionGrantedImpl @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
) : IsPermissionGranted {

    override fun invoke(permission: String): Boolean {
        return PermissionsUtil.hasPermission(context, permission)
    }
}
