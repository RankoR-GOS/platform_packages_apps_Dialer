package com.android.dialer.ui.contacts.component

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.dialer.testing.robolectricComposeActivityRule
import com.android.dialer.ui.contacts.common.contactAvatarTestTag
import com.android.dialer.ui.contacts.common.contactRowTestTag
import com.android.dialer.ui.contacts.common.contactSectionHeaderTestTag
import com.android.dialer.ui.contacts.screen.model.ContactRowUiModel
import com.android.dialer.ui.core.DialerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
class ContactRowTest {

    @get:Rule(order = 0)
    val activityRule = robolectricComposeActivityRule()

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private val clicks = mutableListOf<Rect>()

    private fun row(
        id: Long = 1L,
        displayName: String = "Ada Lovelace",
        sectionLabel: String = "A",
        isSectionStart: Boolean = true,
    ) = ContactRowUiModel(
        id = id,
        lookupKey = "lookup-$id",
        displayName = displayName,
        photoId = 0L,
        photoUri = null,
        sectionLabel = sectionLabel,
        isSectionStart = isSectionStart,
    )

    private fun render(vararg rows: ContactRowUiModel) {
        composeRule.setContent {
            DialerTheme(darkTheme = false) {
                Column {
                    rows.forEach { model ->
                        ContactRow(row = model, onClick = { bounds -> clicks += bounds })
                    }
                }
            }
        }
    }

    // Modifier.clickable merges the row's descendants into one accessibility node, so anything
    // tagged inside it is only reachable through the unmerged tree.
    @Test
    fun rowIsTaggedByContactId() {
        render(row(id = 7L))

        composeRule.onNodeWithTag(contactRowTestTag(contactId = 7L)).assertIsDisplayed()
        composeRule
            .onNodeWithTag(contactAvatarTestTag(contactId = 7L), useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun rowShowsTheDisplayName() {
        render(row(displayName = "Grace Hopper"))

        composeRule.onNodeWithText("Grace Hopper").assertIsDisplayed()
    }

    @Test
    fun sectionLabelIsDrawnOnlyOnTheFirstRowOfItsSection() {
        render(
            row(id = 1L, sectionLabel = "A", isSectionStart = true),
            row(id = 2L, sectionLabel = "A", isSectionStart = false),
        )

        val headers = composeRule
            .onAllNodesWithTag(contactSectionHeaderTestTag(label = "A"), useUnmergedTree = true)
            .fetchSemanticsNodes()

        assertEquals(1, headers.size)
    }

    @Test
    fun clickReportsTheAvatarBoundsSoTheCardCanAnimateOutOfIt() {
        render(row(id = 3L))

        composeRule.onNodeWithTag(contactRowTestTag(contactId = 3L)).performClick()

        val bounds = clicks.single()
        assertTrue("avatar bounds should not be empty: $bounds", bounds.width > 0f)
        assertTrue("avatar bounds should not be empty: $bounds", bounds.height > 0f)
    }

    @Test
    fun eachRowReportsItsOwnClick() {
        render(row(id = 1L), row(id = 2L, isSectionStart = false))

        composeRule.onNodeWithTag(contactRowTestTag(contactId = 2L)).performClick()

        assertEquals(1, clicks.size)
    }
}
