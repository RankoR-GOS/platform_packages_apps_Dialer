package com.android.dialer.ui.contacts.component

import android.os.Build
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.android.dialer.testing.robolectricComposeActivityRule
import com.android.dialer.ui.contacts.common.CONTACTS_LIST_TEST_TAG
import com.android.dialer.ui.contacts.common.CONTACTS_PINNED_SECTION_TEST_TAG
import com.android.dialer.ui.contacts.common.contactRowTestTag
import com.android.dialer.ui.contacts.screen.model.ContactRowUiModel
import com.android.dialer.ui.core.DialerTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
class ContactsListTest {

    @get:Rule(order = 0)
    val activityRule = robolectricComposeActivityRule()

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private val contactClicks = mutableListOf<Pair<Long, Rect>>()

    private fun row(id: Long, name: String, label: String, isStart: Boolean) = ContactRowUiModel(
        id = id,
        lookupKey = "lookup-$id",
        displayName = name,
        photoId = 0L,
        photoUri = null,
        sectionLabel = label,
        isSectionStart = isStart,
    )

    private val rows = persistentListOf(
        row(id = 1L, name = "Ada Lovelace", label = "A", isStart = true),
        row(id = 2L, name = "Alan Turing", label = "A", isStart = false),
        row(id = 3L, name = "Grace Hopper", label = "G", isStart = true),
    )

    private fun render(rows: List<ContactRowUiModel> = this.rows) {
        composeRule.setContent {
            DialerTheme(darkTheme = false) {
                ContactsList(
                    rows = rows.toPersistentList(),
                    showsAddContactRow = false,
                    onContactClick = { id, bounds -> contactClicks += id to bounds },
                    onAddContactClick = {},
                )
            }
        }
    }

    @Test
    fun listIsTagged() {
        render()

        composeRule.onNodeWithTag(CONTACTS_LIST_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun everyContactGetsItsOwnRow() {
        render()

        rows.forEach { row ->
            composeRule.onNodeWithTag(contactRowTestTag(contactId = row.id)).assertIsDisplayed()
        }
    }

    @Test
    fun clickingARowReportsThatContactAndItsAvatarBounds() {
        render()

        composeRule.onNodeWithTag(contactRowTestTag(contactId = 2L)).performClick()

        val (id, bounds) = contactClicks.single()
        assertEquals(2L, id)
        assertTrue("expected real avatar bounds, got $bounds", bounds.width > 0f)
    }

    @Test
    fun nothingIsPinnedAtRest() {
        render()

        assertEquals(0, pinnedLabelCount())
    }

    @Test
    fun scrollingPastASectionStartPinsItsLabel() {
        val longSection = List(size = 40) { index ->
            row(
                id = index.toLong(),
                name = "Contact $index",
                label = "A",
                isStart = index == 0,
            )
        }
        render(rows = longSection)

        composeRule.onNodeWithTag(CONTACTS_LIST_TEST_TAG).performScrollToIndex(20)

        composeRule
            .onNodeWithTag(CONTACTS_PINNED_SECTION_TEST_TAG, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun anEmptyRowListRendersAnEmptyList() {
        render(rows = emptyList())

        composeRule.onNodeWithTag(CONTACTS_LIST_TEST_TAG).assertIsDisplayed()
        assertEquals(0, pinnedLabelCount())
    }

    private fun pinnedLabelCount(): Int =
        composeRule
            .onAllNodesWithTag(CONTACTS_PINNED_SECTION_TEST_TAG, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .size
}
