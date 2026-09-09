package com.android.dialer.ui.recents.mapper.recentsitemuimapperimpl

import android.Manifest
import android.os.Build
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
    fun map_exposesTheFormattedNumberAsTheDisplayNumber() {
        val model = map(callLogEntry(id = 1L, number = "6502530000", cachedName = "Ada"))

        assertEquals("formatted 6502530000", model.displayNumber)
    }

    @Test
    fun map_withACallableNumberAndTheSmsPermission_allowsMessaging() {
        assertTrue(map(callLogEntry(id = 1L)).canMessage)
    }

    @Test
    fun map_withoutTheSmsPermission_doesNotAllowMessaging() {
        every { isPermissionGranted(Manifest.permission.SEND_SMS) } returns false

        assertFalse(map(callLogEntry(id = 1L)).canMessage)
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
    fun map_withAStrangerAndTheContactsWritePermission_allowsAddingAContact() {
        assertTrue(map(callLogEntry(id = 1L)).canAddContact)
    }

    @Test
    fun map_withAnExistingContact_doesNotAllowAddingAContact() {
        val entry = callLogEntry(id = 1L, lookupUri = "content://contacts/lookup/1")

        assertFalse(map(entry).canAddContact)
    }

    @Test
    fun map_withoutTheContactsWritePermission_doesNotAllowAddingAContact() {
        every { isPermissionGranted(Manifest.permission.WRITE_CONTACTS) } returns false

        assertFalse(map(callLogEntry(id = 1L)).canAddContact)
    }
}
