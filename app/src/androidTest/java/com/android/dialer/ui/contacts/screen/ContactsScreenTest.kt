package com.android.dialer.ui.contacts.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.dialer.R
import com.android.dialer.ui.contacts.common.CONTACTS_ADD_CONTACT_ROW_TEST_TAG
import com.android.dialer.ui.contacts.common.CONTACTS_EMPTY_STATE_ACTION_TEST_TAG
import com.android.dialer.ui.contacts.common.CONTACTS_EMPTY_STATE_MESSAGE_TEST_TAG
import com.android.dialer.ui.contacts.common.CONTACTS_EMPTY_STATE_TEST_TAG
import com.android.dialer.ui.contacts.common.CONTACTS_LIST_TEST_TAG
import com.android.dialer.ui.contacts.common.contactRowTestTag
import com.android.dialer.ui.contacts.screen.model.ContactRowUiModel
import com.android.dialer.ui.contacts.screen.model.ContactsAction as Action
import com.android.dialer.ui.contacts.screen.model.ContactsScreenEffect as Effect
import com.android.dialer.ui.contacts.screen.model.ContactsUiState as State
import com.android.dialer.ui.core.DialerTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContactsScreenTest {

    private class FakeScreenModel : ContactsScreenModel {
        val mutableState = MutableStateFlow<State>(State.Loading)
        val mutableEffects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
        val actions = mutableListOf<Action>()

        override val uiState: StateFlow<State> = mutableState.asStateFlow()
        override val effects: Flow<Effect> = mutableEffects

        override fun onAction(action: Action) {
            actions += action
        }
    }

    private class RecordingEffectHandler : ContactsEffectHandler {
        val handled = mutableListOf<Effect>()

        override fun handle(effect: Effect) {
            handled += effect
        }
    }

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val screenModel = FakeScreenModel()
    private val effectHandler = RecordingEffectHandler()

    private fun row(id: Long) = ContactRowUiModel(
        id = id,
        lookupKey = "lookup-$id",
        displayName = "Contact $id",
        photoId = 0L,
        photoUri = null,
        sectionLabel = "C",
        isSectionStart = id == 1L,
    )

    private fun render(showsAddContactRow: Boolean = true) {
        composeRule.setContent {
            DialerTheme(darkTheme = false) {
                ContactsScreen(
                    screenModel = screenModel,
                    effectHandler = effectHandler,
                    showsAddContactRow = showsAddContactRow,
                )
            }
        }
    }

    private fun string(resId: Int): String = composeRule.activity.getString(resId)

    private fun nodeCount(tag: String): Int =
        composeRule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().size

    // --- states ----------------------------------------------------------------------------

    @Test
    fun loadingShowsNeitherListNorEmptyState() {
        render()

        assertEquals(0, nodeCount(CONTACTS_LIST_TEST_TAG))
        assertEquals(0, nodeCount(CONTACTS_EMPTY_STATE_TEST_TAG))
    }

    @Test
    fun missingPermissionOffersToTurnItOn() {
        render()
        composeRule.runOnIdle { screenModel.mutableState.value = State.PermissionRequired }

        composeRule.onNodeWithTag(CONTACTS_EMPTY_STATE_TEST_TAG).assertIsDisplayed()
        composeRule
            .onNodeWithTag(CONTACTS_EMPTY_STATE_MESSAGE_TEST_TAG, useUnmergedTree = true)
            .assertTextEquals(string(R.string.permission_no_contacts))
        composeRule
            .onNodeWithTag(CONTACTS_EMPTY_STATE_ACTION_TEST_TAG)
            .assertTextEquals(string(R.string.permission_single_turn_on))
    }

    @Test
    fun noContactsOffersToCreateOne() {
        render()
        composeRule.runOnIdle { screenModel.mutableState.value = State.Empty }

        composeRule.onNodeWithTag(CONTACTS_EMPTY_STATE_TEST_TAG).assertIsDisplayed()
        composeRule
            .onNodeWithTag(CONTACTS_EMPTY_STATE_MESSAGE_TEST_TAG, useUnmergedTree = true)
            .assertTextEquals(string(R.string.all_contacts_empty))
    }

    @Test
    fun loadedShowsTheContacts() {
        render()
        composeRule.runOnIdle {
            screenModel.mutableState.value = State.Loaded(
                rows = persistentListOf(row(id = 1L), row(id = 2L)),
            )
        }

        composeRule.onNodeWithTag(CONTACTS_LIST_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(contactRowTestTag(contactId = 1L)).assertIsDisplayed()
        composeRule.onNodeWithTag(contactRowTestTag(contactId = 2L)).assertIsDisplayed()
        assertEquals(0, nodeCount(CONTACTS_EMPTY_STATE_TEST_TAG))
    }

    // --- actions ---------------------------------------------------------------------------

    @Test
    fun resumingAsksForAReload() {
        render()

        assertTrue(
            "expected ScreenResumed, got ${screenModel.actions}",
            screenModel.actions.contains(Action.ScreenResumed),
        )
    }

    @Test
    fun turnOnReportsThePermissionRequest() {
        render()
        composeRule.runOnIdle { screenModel.mutableState.value = State.PermissionRequired }

        composeRule.onNodeWithTag(CONTACTS_EMPTY_STATE_ACTION_TEST_TAG).performClick()

        assertTrue(screenModel.actions.contains(Action.GrantPermissionClicked))
    }

    @Test
    fun createContactFromTheEmptyStateReportsAddContact() {
        render()
        composeRule.runOnIdle { screenModel.mutableState.value = State.Empty }

        composeRule.onNodeWithTag(CONTACTS_EMPTY_STATE_ACTION_TEST_TAG).performClick()

        assertTrue(screenModel.actions.contains(Action.AddContactClicked))
    }

    @Test
    fun tappingAContactReportsItWithAnAnchorForTheCard() {
        render()
        composeRule.runOnIdle {
            screenModel.mutableState.value = State.Loaded(rows = persistentListOf(row(id = 5L)))
        }

        composeRule.onNodeWithTag(contactRowTestTag(contactId = 5L)).performClick()

        val clicked = screenModel.actions.filterIsInstance<Action.ContactClicked>().single()
        assertEquals(5L, clicked.contactId)
        assertTrue("expected real anchor bounds", clicked.anchorBounds.width > 0f)
    }

    @Test
    fun tappingTheAddContactRowReportsAddContact() {
        render()
        composeRule.runOnIdle {
            screenModel.mutableState.value = State.Loaded(rows = persistentListOf(row(id = 1L)))
        }

        composeRule.onNodeWithTag(CONTACTS_ADD_CONTACT_ROW_TEST_TAG).performClick()

        assertTrue(screenModel.actions.contains(Action.AddContactClicked))
    }

    @Test
    fun theAddContactRowIsOmittedWhenTheScreenIsConfiguredWithoutIt() {
        render(showsAddContactRow = false)
        composeRule.runOnIdle {
            screenModel.mutableState.value = State.Loaded(rows = persistentListOf(row(id = 1L)))
        }

        assertEquals(0, nodeCount(CONTACTS_ADD_CONTACT_ROW_TEST_TAG))
    }

    // --- effects ---------------------------------------------------------------------------

    @Test
    fun effectsReachTheHandler() {
        render()

        composeRule.runOnIdle { screenModel.mutableEffects.tryEmit(Effect.LaunchAddContact) }
        composeRule.waitForIdle()

        assertEquals(listOf(Effect.LaunchAddContact), effectHandler.handled)
    }
}
