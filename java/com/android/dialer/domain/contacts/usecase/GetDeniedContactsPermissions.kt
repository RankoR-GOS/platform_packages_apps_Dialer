package com.android.dialer.domain.contacts.usecase

import android.content.Context
import android.content.pm.PackageManager
import com.android.dialer.util.PermissionsUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

internal interface GetDeniedContactsPermissions {
    operator fun invoke(): ImmutableList<String>
}

internal class GetDeniedContactsPermissionsImpl @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
) : GetDeniedContactsPermissions {

    override fun invoke(): ImmutableList<String> =
        PermissionsUtil.allContactsGroupPermissionsUsedInDialer
            .filter { permission ->
                context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
            }
            .toPersistentList()
}
