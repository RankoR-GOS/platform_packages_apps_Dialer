package com.android.dialer.data.contacts.repository

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.Contacts

/**
 * Stands in for the contacts provider so the repository's real `ContentResolver` calls can be
 * observed: what URI it asked for, with which projection, selection and sort order.
 */
internal class FakeContactsProvider : ContentProvider() {

    data class QueryArgs(
        val uri: Uri,
        val projection: List<String>,
        val selection: String?,
        val selectionArgs: List<String>,
        val sortOrder: String?,
    )

    val queries = mutableListOf<QueryArgs>()

    var result: Cursor? = emptyCursor()

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? {
        queries += QueryArgs(
            uri = uri,
            projection = projection?.toList().orEmpty(),
            selection = selection,
            selectionArgs = selectionArgs?.toList().orEmpty(),
            sortOrder = sortOrder,
        )
        return result
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    companion object {
        val COLUMNS = arrayOf("_id", "display_name", "photo_id", "photo_uri", "lookup")

        fun emptyCursor(): Cursor = MatrixCursor(COLUMNS)

        fun cursorOf(
            rows: List<Array<Any?>>,
            titles: Array<String>? = null,
            counts: IntArray? = null,
        ): Cursor {
            val cursor = MatrixCursor(COLUMNS)
            rows.forEach(cursor::addRow)

            if (titles != null || counts != null) {
                cursor.extras = Bundle().apply {
                    titles?.let { putStringArray(Contacts.EXTRA_ADDRESS_BOOK_INDEX_TITLES, it) }
                    counts?.let { putIntArray(Contacts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS, it) }
                }
            }

            return cursor
        }

        fun row(
            id: Long,
            displayName: String?,
            photoId: Long = 0L,
            photoUri: String? = null,
            lookupKey: String? = "lookup-$id",
        ): Array<Any?> = arrayOf(id, displayName, photoId, photoUri, lookupKey)
    }
}
