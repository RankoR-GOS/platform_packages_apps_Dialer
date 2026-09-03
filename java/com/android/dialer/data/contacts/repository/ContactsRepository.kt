package com.android.dialer.data.contacts.repository

import android.content.ContentResolver
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract.Contacts
import com.android.dialer.data.contacts.model.Contact
import com.android.dialer.data.contacts.model.ContactNameOrder
import com.android.dialer.data.contacts.model.ContactsIndex
import com.android.dialer.data.contacts.model.ContactsQuery
import com.android.dialer.data.contacts.model.ContactsSnapshot
import com.android.dialer.di.core.IoDispatcher
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal interface ContactsRepository {
    fun observeContacts(query: ContactsQuery): Flow<ContactsSnapshot>
}

internal class ContactsRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    private val nameOrderSource: ContactNameOrderSource,
    @param:IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ContactsRepository {

    override fun observeContacts(query: ContactsQuery): Flow<ContactsSnapshot> =
        contactsChanges()
            .conflate()
            .map { readSnapshot(query = query) }
            .flowOn(ioDispatcher)

    private fun contactsChanges(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }

        // Registered before the first emission, so a change landing mid-query is not missed.
        contentResolver.registerContentObserver(Contacts.CONTENT_URI, true, observer)
        send(Unit)

        awaitClose { contentResolver.unregisterContentObserver(observer) }
    }

    private fun readSnapshot(query: ContactsQuery): ContactsSnapshot {
        val displayOrder = nameOrderSource.displayOrder()
        val displayNameColumn = displayNameColumn(order = displayOrder)

        val cursor = contentResolver.query(
            buildUri(filter = query.filter),
            projection(displayNameColumn = displayNameColumn),
            selection(
                displayNameColumn = displayNameColumn,
                requirePhoneNumber = query.requirePhoneNumber,
            ),
            /* selectionArgs = */
            null,
            "${sortKeyColumn(order = nameOrderSource.sortOrder())} ASC",
        ) ?: return ContactsSnapshot.EMPTY

        return cursor.use { open ->
            ContactsSnapshot(
                contacts = readContacts(cursor = open),
                index = readIndex(cursor = open),
            )
        }
    }

    private fun readContacts(cursor: Cursor): ImmutableList<Contact> {
        val contacts = ArrayList<Contact>(cursor.count)

        while (cursor.moveToNext()) {
            val displayName = cursor.getString(DISPLAY_NAME_INDEX)?.takeIf(String::isNotBlank)
                ?: continue

            contacts += Contact(
                id = cursor.getLong(ID_INDEX),
                lookupKey = cursor.getString(LOOKUP_KEY_INDEX).orEmpty(),
                displayName = displayName,
                photoId = cursor.getLong(PHOTO_ID_INDEX),
                photoUri = cursor.getString(PHOTO_URI_INDEX)?.takeIf(String::isNotBlank),
            )
        }

        return contacts.toPersistentList()
    }

    private fun readIndex(cursor: Cursor): ContactsIndex {
        val extras = cursor.extras
        val titles = extras?.getStringArray(Contacts.EXTRA_ADDRESS_BOOK_INDEX_TITLES)
        val counts = extras?.getIntArray(Contacts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS)

        if (titles == null || counts == null) {
            return ContactsIndex.EMPTY
        }

        val size = minOf(titles.size, counts.size)

        return ContactsIndex(
            titles = titles.take(size).map { title -> title.orEmpty() }.toPersistentList(),
            counts = counts.take(size).toPersistentList(),
        )
    }

    private fun buildUri(filter: String): Uri {
        val base = when {
            filter.isEmpty() -> Contacts.CONTENT_URI.buildUpon()
            else -> Contacts.CONTENT_FILTER_URI.buildUpon().appendPath(filter)
        }

        return base
            .appendQueryParameter(Contacts.EXTRA_ADDRESS_BOOK_INDEX, "true")
            .build()
    }

    private fun projection(displayNameColumn: String): Array<String> =
        arrayOf(
            Contacts._ID,
            displayNameColumn,
            Contacts.PHOTO_ID,
            Contacts.PHOTO_THUMBNAIL_URI,
            Contacts.LOOKUP_KEY,
        )

    private fun selection(displayNameColumn: String, requirePhoneNumber: Boolean): String =
        buildString {
            append(displayNameColumn)
            append(" IS NOT NULL")
            if (requirePhoneNumber) {
                append(" AND ")
                append(Contacts.HAS_PHONE_NUMBER)
                append("=1")
            }
        }

    private fun displayNameColumn(order: ContactNameOrder): String =
        when (order) {
            ContactNameOrder.PRIMARY -> Contacts.DISPLAY_NAME_PRIMARY
            ContactNameOrder.ALTERNATIVE -> Contacts.DISPLAY_NAME_ALTERNATIVE
        }

    private fun sortKeyColumn(order: ContactNameOrder): String =
        when (order) {
            ContactNameOrder.PRIMARY -> Contacts.SORT_KEY_PRIMARY
            ContactNameOrder.ALTERNATIVE -> Contacts.SORT_KEY_ALTERNATIVE
        }

    private companion object {
        private const val ID_INDEX = 0
        private const val DISPLAY_NAME_INDEX = 1
        private const val PHOTO_ID_INDEX = 2
        private const val PHOTO_URI_INDEX = 3
        private const val LOOKUP_KEY_INDEX = 4
    }
}
