package com.android.dialer.data.contacts.repository

import android.content.Context
import com.android.dialer.contacts.ContactsComponent
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.DisplayOrder
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.SortOrder
import com.android.dialer.data.contacts.model.ContactNameOrder
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal interface ContactNameOrderSource {
    fun displayOrder(): ContactNameOrder

    fun sortOrder(): ContactNameOrder
}

internal fun DisplayOrder?.toContactNameOrder(): ContactNameOrder =
    when (this) {
        DisplayOrder.ALTERNATIVE -> ContactNameOrder.ALTERNATIVE
        DisplayOrder.PRIMARY, null -> ContactNameOrder.PRIMARY
    }

internal fun SortOrder?.toContactNameOrder(): ContactNameOrder =
    when (this) {
        SortOrder.BY_ALTERNATIVE -> ContactNameOrder.ALTERNATIVE
        SortOrder.BY_PRIMARY, null -> ContactNameOrder.PRIMARY
    }

internal class LegacyContactNameOrderSource @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
) : ContactNameOrderSource {

    override fun displayOrder(): ContactNameOrder = preferences().displayOrder.toContactNameOrder()

    override fun sortOrder(): ContactNameOrder = preferences().sortOrder.toContactNameOrder()

    private fun preferences() = ContactsComponent.get(context).contactDisplayPreferences()
}
