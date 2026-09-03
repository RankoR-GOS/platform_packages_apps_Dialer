package com.android.dialer.domain.contacts.usecase

import android.Manifest
import android.os.Build
import android.os.Looper
import app.cash.turbine.test
import com.android.dialer.util.PermissionsUtil
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
class ObserveContactsPermissionGrantsTest {

    private val application = RuntimeEnvironment.getApplication()
    private val observeGrants = ObserveContactsPermissionGrantsImpl(context = application)

    private fun TestScope.announceGrant(permission: String) {
        runCurrent()
        PermissionsUtil.notifyPermissionGranted(application, permission)
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun emitsNothingUntilAGrantIsAnnounced() = runTest {
        observeGrants().test {
            runCurrent()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun emitsWhenAnotherScreenAnnouncesTheReadGrant() = runTest {
        observeGrants().test {
            announceGrant(Manifest.permission.READ_CONTACTS)

            assertEquals(Unit, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun ignoresGrantsForOtherPermissions() = runTest {
        observeGrants().test {
            announceGrant(Manifest.permission.READ_CALL_LOG)

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun emitsOncePerAnnouncement() = runTest {
        observeGrants().test {
            repeat(times = 3) {
                announceGrant(Manifest.permission.READ_CONTACTS)

                assertEquals(Unit, awaitItem())
            }

            cancelAndIgnoreRemainingEvents()
        }
    }
}
