package com.android.dialer.data.contacts.repository

import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.DisplayOrder
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.SortOrder
import com.android.dialer.data.contacts.model.ContactNameOrder
import org.junit.Assert.assertEquals
import org.junit.Test

class ContactNameOrderMappingTest {

    @Test
    fun displayOrderMapsOntoMatchingNameOrder() {
        assertEquals(ContactNameOrder.PRIMARY, DisplayOrder.PRIMARY.toContactNameOrder())
        assertEquals(ContactNameOrder.ALTERNATIVE, DisplayOrder.ALTERNATIVE.toContactNameOrder())
    }

    @Test
    fun sortOrderMapsOntoMatchingNameOrder() {
        assertEquals(ContactNameOrder.PRIMARY, SortOrder.BY_PRIMARY.toContactNameOrder())
        assertEquals(ContactNameOrder.ALTERNATIVE, SortOrder.BY_ALTERNATIVE.toContactNameOrder())
    }

    @Test
    fun absentPreferenceFallsBackToPrimary() {
        assertEquals(ContactNameOrder.PRIMARY, (null as DisplayOrder?).toContactNameOrder())
        assertEquals(ContactNameOrder.PRIMARY, (null as SortOrder?).toContactNameOrder())
    }
}
