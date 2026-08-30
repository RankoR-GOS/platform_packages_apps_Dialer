package com.android.dialer.domain.contacts.usecase

import android.Manifest
import android.os.Build
import com.android.dialer.util.PermissionsUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
class ContactsPermissionUseCasesTest {

    private val application = RuntimeEnvironment.getApplication()

    private val isGranted = IsReadContactsPermissionGrantedImpl(context = application)
    private val getDenied = GetDeniedContactsPermissionsImpl(context = application)

    private fun grant(vararg permissions: String) {
        shadowOf(application).grantPermissions(*permissions)
    }

    private fun deny(vararg permissions: String) {
        shadowOf(application).denyPermissions(*permissions)
    }

    @Test
    fun readPermissionIsNotGrantedByDefault() {
        deny(Manifest.permission.READ_CONTACTS)

        assertFalse(isGranted())
    }

    @Test
    fun readPermissionIsGrantedOnceHeld() {
        grant(Manifest.permission.READ_CONTACTS)

        assertTrue(isGranted())
    }

    @Test
    fun deniedListCoversTheWholeContactsGroup() {
        deny(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)

        assertEquals(PermissionsUtil.allContactsGroupPermissionsUsedInDialer, getDenied())
    }

    @Test
    fun deniedListExcludesWhatIsAlreadyHeld() {
        deny(Manifest.permission.WRITE_CONTACTS)
        grant(Manifest.permission.READ_CONTACTS)

        assertEquals(listOf(Manifest.permission.WRITE_CONTACTS), getDenied())
    }

    @Test
    fun deniedListIsEmptyWhenTheGroupIsFullyGranted() {
        grant(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)

        assertEquals(emptyList<String>(), getDenied())
    }
}
