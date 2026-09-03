package com.android.dialer.ui.contacts.component

import com.android.dialer.ui.contacts.screen.model.ContactRowUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FastScrollerLogicTest {

    private fun row(id: Long, label: String) = ContactRowUiModel(
        id = id,
        lookupKey = "lookup-$id",
        displayName = "Contact $id",
        photoId = 0L,
        photoUri = null,
        sectionLabel = label,
        isSectionStart = false,
    )

    // --- visibility -----------------------------------------------------------------------
    @Test
    fun scrollerHidesWhileTheWholeListFits() {
        assertFalse(isFastScrollerVisible(totalItems = 5, visibleItems = 5))
        assertFalse(isFastScrollerVisible(totalItems = 3, visibleItems = 8))
    }

    @Test
    fun scrollerAppearsOnceTheListOutgrowsTheScreen() {
        assertTrue(isFastScrollerVisible(totalItems = 40, visibleItems = 12))
    }

    @Test
    fun scrollerStaysHiddenBeforeAnythingIsMeasured() {
        assertFalse(isFastScrollerVisible(totalItems = 40, visibleItems = 0))
    }

    // --- touch position to item -----------------------------------------------------------
    @Test
    fun dragMapsProportionallyOntoTheItemCount() {
        assertEquals(0, fastScrollTargetIndex(progress = 0f, totalItems = 100))
        assertEquals(50, fastScrollTargetIndex(progress = 0.5f, totalItems = 100))
        assertEquals(99, fastScrollTargetIndex(progress = 1f, totalItems = 100))
    }

    @Test
    fun dragBeyondTheTrackClampsToTheEnds() {
        assertEquals(0, fastScrollTargetIndex(progress = -0.4f, totalItems = 100))
        assertEquals(99, fastScrollTargetIndex(progress = 1.7f, totalItems = 100))
    }

    @Test
    fun anEmptyListHasNoTarget() {
        assertEquals(0, fastScrollTargetIndex(progress = 0.5f, totalItems = 0))
    }

    // --- thumb position -------------------------------------------------------------------
    @Test
    fun thumbSitsAtTheTopBeforeScrolling() {
        assertEquals(
            0f,
            listScrollProgress(firstVisibleIndex = 0, totalItems = 40, visibleItems = 10),
            TOLERANCE,
        )
    }

    @Test
    fun thumbReachesTheBottomAtTheLastScrollPosition() {
        assertEquals(
            1f,
            listScrollProgress(firstVisibleIndex = 30, totalItems = 40, visibleItems = 10),
            TOLERANCE,
        )
    }

    @Test
    fun thumbTracksProgressThroughTheList() {
        assertEquals(
            0.5f,
            listScrollProgress(firstVisibleIndex = 15, totalItems = 40, visibleItems = 10),
            TOLERANCE,
        )
    }

    @Test
    fun thumbStaysAtTheTopWhenNothingCanScroll() {
        assertEquals(
            0f,
            listScrollProgress(firstVisibleIndex = 0, totalItems = 5, visibleItems = 10),
            TOLERANCE,
        )
    }

    // --- bubble label ---------------------------------------------------------------------
    @Test
    fun bubbleShowsTheSectionOfTheRowBeingScrolledTo() {
        val rows =
            listOf(row(id = 0L, label = "A"), row(id = 1L, label = "M"), row(id = 2L, label = "Z"))

        assertEquals(
            "M",
            fastScrollerLabel(
                rows = rows,
                progress = 0.5f,
                totalItems = 3,
                hasAddContactRow = false,
            ),
        )
    }

    @Test
    fun bubbleAccountsForTheAddContactRow() {
        val rows = listOf(row(id = 0L, label = "A"), row(id = 1L, label = "M"))

        assertEquals(
            "A",
            fastScrollerLabel(
                rows = rows,
                progress = 0.34f,
                totalItems = 3,
                hasAddContactRow = true,
            ),
        )
    }

    @Test
    fun bubbleIsBlankOverTheAddContactRow() {
        val rows = listOf(row(id = 0L, label = "A"))

        assertEquals(
            "",
            fastScrollerLabel(
                rows = rows,
                progress = 0f,
                totalItems = 2,
                hasAddContactRow = true,
            ),
        )
    }

    @Test
    fun bubbleIsBlankForUnlabelledRows() {
        val rows = listOf(row(id = 0L, label = ""))

        assertEquals(
            "",
            fastScrollerLabel(
                rows = rows,
                progress = 0f,
                totalItems = 1,
                hasAddContactRow = false,
            ),
        )
    }

    private companion object {
        private const val TOLERANCE = 0.0001f
    }
}
