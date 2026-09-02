package com.android.dialer.ui.contacts.component

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.dialer.ui.contacts.common.CONTACTS_ADD_CONTACT_ROW_TEST_TAG
import com.android.dialer.ui.contacts.common.CONTACTS_LIST_TEST_TAG
import com.android.dialer.ui.contacts.common.CONTACTS_PINNED_SECTION_TEST_TAG
import com.android.dialer.ui.contacts.common.contactRowTestTag
import com.android.dialer.ui.contacts.screen.model.ContactRowUiModel
import com.android.dialer.ui.core.DialerTheme
import kotlinx.collections.immutable.toPersistentList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContactsListWithAddRowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var addContactClicks = 0
    private val contactClicks = mutableListOf<Long>()

    private fun row(id: Long, label: String, isStart: Boolean) = ContactRowUiModel(
        id = id,
        lookupKey = "lookup-$id",
        displayName = "Contact $id",
        photoId = 0L,
        photoUri = null,
        sectionLabel = label,
        isSectionStart = isStart,
    )

    private fun render(rows: List<ContactRowUiModel>) {
        composeRule.setContent {
            DialerTheme(darkTheme = false) {
                ContactsList(
                    rows = rows.toPersistentList(),
                    showsAddContactRow = true,
                    onContactClick = { id, _ -> contactClicks += id },
                    onAddContactClick = { addContactClicks++ },
                )
            }
        }
    }

    @Test
    fun addContactRowSitsAboveTheContacts() {
        render(listOf(row(id = 1L, label = "A", isStart = true)))

        composeRule.onNodeWithTag(CONTACTS_ADD_CONTACT_ROW_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(contactRowTestTag(contactId = 1L)).assertIsDisplayed()
    }

    @Test
    fun addContactRowReportsItsOwnClick() {
        render(listOf(row(id = 1L, label = "A", isStart = true)))

        composeRule.onNodeWithTag(CONTACTS_ADD_CONTACT_ROW_TEST_TAG).performClick()

        assertEquals(1, addContactClicks)
        assertEquals(emptyList<Long>(), contactClicks)
    }

    @Test
    fun pinnedLabelAccountsForTheAddContactRow() {
        val rows = List(size = 40) { index ->
            row(id = index.toLong(), label = "A", isStart = index == 0)
        }
        render(rows)

        composeRule.onNodeWithTag(CONTACTS_LIST_TEST_TAG).performScrollToIndex(20)

        composeRule
            .onNodeWithTag(CONTACTS_PINNED_SECTION_TEST_TAG, useUnmergedTree = true)
            .assertIsDisplayed()
    }
}
