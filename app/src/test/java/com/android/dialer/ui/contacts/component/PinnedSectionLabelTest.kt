package com.android.dialer.ui.contacts.component

import com.android.dialer.ui.contacts.screen.model.ContactRowUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PinnedSectionLabelTest {

    private fun row(label: String, isSectionStart: Boolean, id: Long = 1L) = ContactRowUiModel(
        id = id,
        lookupKey = "lookup-$id",
        displayName = "Ada",
        photoId = 0L,
        photoUri = null,
        sectionLabel = label,
        isSectionStart = isSectionStart,
    )

    private val rows = listOf(
        row(label = "A", isSectionStart = true, id = 1L),
        row(label = "A", isSectionStart = false, id = 2L),
        row(label = "G", isSectionStart = true, id = 3L),
    )

    private fun labelFor(
        firstVisibleIndex: Int,
        firstVisibleOffset: Int = 0,
        hasAddContactRow: Boolean = false,
    ) = pinnedSectionLabel(
        rows = rows,
        firstVisibleIndex = firstVisibleIndex,
        firstVisibleOffset = firstVisibleOffset,
        hasAddContactRow = hasAddContactRow,
    )

    // The row draws its own label in exactly that spot, so pinning a second one is noise.
    @Test
    fun nothingIsPinnedWhileASectionStartSitsAtTheTop() {
        assertNull(labelFor(firstVisibleIndex = 0))
        assertNull(labelFor(firstVisibleIndex = 2))
    }

    @Test
    fun sectionIsPinnedOnceItsStartHasScrolledPast() {
        assertEquals("A", labelFor(firstVisibleIndex = 1))
    }

    // Scrolled by even a pixel, the row's own label is cut off and the pinned one takes over.
    @Test
    fun sectionIsPinnedAsSoonAsItsStartIsPartiallyScrolled() {
        assertEquals("A", labelFor(firstVisibleIndex = 0, firstVisibleOffset = 12))
    }

    @Test
    fun addContactRowShiftsTheRowLookup() {
        assertEquals("A", labelFor(firstVisibleIndex = 2, hasAddContactRow = true))
        assertNull(labelFor(firstVisibleIndex = 3, hasAddContactRow = true))
    }

    @Test
    fun nothingIsPinnedWhileTheAddContactRowIsAtTheTop() {
        assertNull(labelFor(firstVisibleIndex = 0, hasAddContactRow = true))
    }

    @Test
    fun unlabelledRowsPinNothing() {
        val unlabelled = listOf(row(label = "", isSectionStart = false))

        assertNull(
            pinnedSectionLabel(
                rows = unlabelled,
                firstVisibleIndex = 0,
                firstVisibleOffset = 40,
                hasAddContactRow = false,
            ),
        )
    }

    @Test
    fun anIndexBeyondTheListPinsNothing() {
        assertNull(labelFor(firstVisibleIndex = 99))
    }

    @Test
    fun anEmptyListPinsNothing() {
        assertNull(
            pinnedSectionLabel(
                rows = emptyList(),
                firstVisibleIndex = 0,
                firstVisibleOffset = 0,
                hasAddContactRow = false,
            ),
        )
    }
}
