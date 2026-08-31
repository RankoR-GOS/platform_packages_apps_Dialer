package com.android.dialer.ui.contacts.screen.mapper

import com.android.dialer.data.contacts.model.Contact
import com.android.dialer.data.contacts.model.ContactsIndex
import com.android.dialer.data.contacts.model.ContactsSnapshot
import com.android.dialer.ui.contacts.screen.model.ContactRowUiModel
import com.android.dialer.ui.contacts.screen.model.ContactsUiState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactsUiStateMapperTest {

    private val mapper = ContactsUiStateMapperImpl()

    private fun contact(id: Long, name: String) =
        Contact(
            id = id,
            lookupKey = "lookup-$id",
            displayName = name,
            photoId = 0L,
            photoUri = null,
        )

    private fun snapshot(
        names: List<String>,
        titles: List<String> = emptyList(),
        counts: List<Int> = emptyList(),
    ) = ContactsSnapshot(
        contacts = names.mapIndexed { i, name -> contact(id = i.toLong(), name = name) }
            .toPersistentList(),
        index = ContactsIndex(
            titles = titles.toPersistentList(),
            counts = counts.toPersistentList(),
        ),
    )

    private fun loadedRows(state: ContactsUiState): List<ContactRowUiModel> =
        (state as ContactsUiState.Loaded).rows

    private fun labelsAndStarts(state: ContactsUiState): List<Pair<String, Boolean>> =
        loadedRows(state).map { it.sectionLabel to it.isSectionStart }

    @Test
    fun mapsAnEmptySnapshotToTheEmptyState() {
        val state = mapper.map(snapshot = ContactsSnapshot.EMPTY, showsAddContactRow = true)

        assertEquals(ContactsUiState.Empty, state)
    }

    @Test
    fun carriesEveryFieldTheRowAndItsTapNeed() {
        val snapshot = ContactsSnapshot(
            contacts = persistentListOf(
                Contact(
                    id = 5L,
                    lookupKey = "key-5",
                    displayName = "Ada",
                    photoId = 9L,
                    photoUri = "content://photo/5",
                ),
            ),
            index = ContactsIndex(
                titles = persistentListOf("A"),
                counts = persistentListOf(1),
            ),
        )

        val row = loadedRows(mapper.map(snapshot, showsAddContactRow = false)).single()

        assertEquals(
            ContactRowUiModel(
                id = 5L,
                lookupKey = "key-5",
                displayName = "Ada",
                photoId = 9L,
                photoUri = "content://photo/5",
                sectionLabel = "A",
                isSectionStart = true,
            ),
            row,
        )
    }

    @Test
    fun labelsEachRowWithItsSectionAndMarksOnlyTheFirstOfEach() {
        val state = mapper.map(
            snapshot = snapshot(
                names = listOf("Ada", "Alan", "Grace", "Linus", "Ada Two"),
                titles = listOf("A", "G", "L"),
                counts = listOf(2, 1, 2),
            ),
            showsAddContactRow = true,
        )

        assertEquals(
            listOf(
                "A" to true,
                "A" to false,
                "G" to true,
                "L" to true,
                "L" to false,
            ),
            labelsAndStarts(state),
        )
    }

    // A repeated label after a different one starts a new visible section, exactly as the legacy
    // "differs from the row above" rule did.
    @Test
    fun treatsARepeatedLabelAfterAnotherAsANewSection() {
        val state = mapper.map(
            snapshot = snapshot(
                names = listOf("Ada", "Grace", "Alan"),
                titles = listOf("A", "G", "A"),
                counts = listOf(1, 1, 1),
            ),
            showsAddContactRow = true,
        )

        assertEquals(
            listOf("A" to true, "G" to true, "A" to true),
            labelsAndStarts(state),
        )
    }

    @Test
    fun passesTheAddContactRowFlagThrough() {
        val withRow = mapper.map(snapshot(names = listOf("Ada")), showsAddContactRow = true)
        val withoutRow = mapper.map(snapshot(names = listOf("Ada")), showsAddContactRow = false)

        assertTrue((withRow as ContactsUiState.Loaded).showsAddContactRow)
        assertTrue(!(withoutRow as ContactsUiState.Loaded).showsAddContactRow)
    }

    // --- the provider disagreeing with itself --------------------------------------------

    @Test
    fun leavesRowsBeyondTheIndexUnlabelled() {
        val state = mapper.map(
            snapshot = snapshot(
                names = listOf("Ada", "Grace", "Linus"),
                titles = listOf("A"),
                counts = listOf(1),
            ),
            showsAddContactRow = true,
        )

        assertEquals(
            listOf("A" to true, "" to false, "" to false),
            labelsAndStarts(state),
        )
    }

    // The legacy adapter walked counts[++index] past the end of the array here and crashed.
    @Test
    fun survivesCountsThatOverrunTheRows() {
        val state = mapper.map(
            snapshot = snapshot(
                names = listOf("Ada", "Grace"),
                titles = listOf("A", "G", "L"),
                counts = listOf(5, 5, 5),
            ),
            showsAddContactRow = true,
        )

        assertEquals(listOf("A" to true, "A" to false), labelsAndStarts(state))
    }

    @Test
    fun survivesMoreTitlesThanCounts() {
        val state = mapper.map(
            snapshot = snapshot(
                names = listOf("Ada", "Grace"),
                titles = listOf("A", "G"),
                counts = listOf(1),
            ),
            showsAddContactRow = true,
        )

        assertEquals(listOf("A" to true, "" to false), labelsAndStarts(state))
    }

    @Test
    fun survivesANegativeCount() {
        val state = mapper.map(
            snapshot = snapshot(
                names = listOf("Ada", "Grace"),
                titles = listOf("A", "G"),
                counts = listOf(-3, 2),
            ),
            showsAddContactRow = true,
        )

        assertEquals(listOf("G" to true, "G" to false), labelsAndStarts(state))
    }

    @Test
    fun leavesEveryRowUnlabelledWhenTheIndexIsMissing() {
        val state = mapper.map(
            snapshot = snapshot(names = listOf("Ada", "Grace")),
            showsAddContactRow = true,
        )

        assertEquals(listOf("" to false, "" to false), labelsAndStarts(state))
    }
}
