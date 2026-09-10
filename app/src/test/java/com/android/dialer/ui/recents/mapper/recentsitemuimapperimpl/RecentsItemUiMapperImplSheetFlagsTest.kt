package com.android.dialer.ui.recents.mapper.recentsitemuimapperimpl

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.provider.CallLog.Calls
import com.android.dialer.testutil.callLogEntry
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsItemUiMapperImplSheetFlagsTest : BaseRecentsItemUiMapperImplTest() {

    @Test
    fun map_withoutTheSmsPermission_allowsOpeningTheMessageEditor() {
        every { context.checkSelfPermission(Manifest.permission.SEND_SMS) } returns
            PackageManager.PERMISSION_DENIED

        assertTrue(map(callLogEntry(id = 1L)).canMessage)
    }

    @Test
    fun map_withAnEmergencyNumber_doesNotAllowMessagingOrAddingAContact() {
        every { isEmergencyNumber("911") } returns true

        val model = map(callLogEntry(id = 1L, number = "911"))

        assertFalse(model.canMessage)
        assertFalse(model.canAddContact)
    }

    @Test
    fun map_whenTheNumberCannotBeCalled_allowsNeitherMessagingNorAddingAContact() {
        every { canPlaceCall(any(), any()) } returns false

        val model = map(callLogEntry(id = 1L))

        assertFalse(model.canCallBack)
        assertFalse(model.canVideoCall)
        assertFalse(model.canMessage)
        assertFalse(model.canAddContact)
        assertFalse(model.canEditNumberBeforeCall)
    }

    @Test
    fun map_withACallableNumber_allowsEditingItBeforeTheCall() {
        assertTrue(map(callLogEntry(id = 1L)).canEditNumberBeforeCall)
    }

    @Test
    fun map_withASipUri_doesNotAllowEditingItBeforeTheCall() {
        val model = map(callLogEntry(id = 1L, number = "sip:ada@example.com"))

        assertFalse(model.canEditNumberBeforeCall)
    }

    @Test
    fun map_withAnExistingContact_doesNotAllowAddingAContact() {
        val entry = callLogEntry(id = 1L, lookupUri = "content://contacts/lookup/1")

        assertFalse(map(entry).canAddContact)
    }

    @Test
    fun map_withoutTheContactsWritePermission_allowsOpeningTheContactEditor() {
        every { context.checkSelfPermission(Manifest.permission.WRITE_CONTACTS) } returns
            PackageManager.PERMISSION_DENIED

        assertTrue(map(callLogEntry(id = 1L)).canAddContact)
    }
}
