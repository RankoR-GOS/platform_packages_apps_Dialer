package com.android.dialer.ui.recents.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsPermissionsTest {

    private val context = mockk<Context>()

    @Test
    fun deniedPermissions_withoutAnyGrants_returnsOnlyTheFiveRecentsPermissions() {
        givenGrantedPermissions(emptyList())

        assertArrayEquals(REQUIRED_PERMISSIONS.toTypedArray(), deniedRecentsPermissions(context))
    }

    @Test
    fun deniedPermissions_withEachPermissionRevoked_returnsOnlyTheMissingPermission() {
        REQUIRED_PERMISSIONS.forEach { deniedPermission ->
            givenGrantedPermissions(REQUIRED_PERMISSIONS - deniedPermission)

            assertArrayEquals(arrayOf(deniedPermission), deniedRecentsPermissions(context))
        }
    }

    @Test
    fun deniedPermissions_afterAllPermissionsAreGranted_returnsNoPermissions() {
        givenGrantedPermissions(emptyList())
        assertArrayEquals(REQUIRED_PERMISSIONS.toTypedArray(), deniedRecentsPermissions(context))

        givenGrantedPermissions(REQUIRED_PERMISSIONS)

        assertArrayEquals(emptyArray<String>(), deniedRecentsPermissions(context))
    }

    private fun givenGrantedPermissions(permissions: List<String>) {
        every { context.checkPermission(any(), any(), any()) } answers {
            if (firstArg<String>() in permissions) {
                PackageManager.PERMISSION_GRANTED
            } else {
                PackageManager.PERMISSION_DENIED
            }
        }
    }

    private companion object {
        val REQUIRED_PERMISSIONS = listOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
        )
    }
}
