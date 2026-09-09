package com.android.dialer.calldetails

import android.content.ComponentName
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.android.dialer.calldetails.model.CallDetailsDeleteDialogState
import com.android.dialer.calldetails.model.CallDetailsUiState
import com.android.dialer.calldetails.ui.CallDetailsScaffold
import com.android.dialer.calldetails.ui.CallDetailsScreen
import com.android.dialer.calldetails.ui.CallDetailsScreenModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
class CallDetailsContentTest {

    @get:Rule(order = 0)
    val componentActivityRule = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                val application = RuntimeEnvironment.getApplication()
                shadowOf(application.packageManager).addActivityIfNotPresent(
                    ComponentName(application, TestCallDetailsActivity::class.java),
                )
                base.evaluate()
            }
        }
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<TestCallDetailsActivity>()

    @Test
    fun screenModelPattern_collectsStateAndForwardsEvents() {
        val uiStateFlow = MutableStateFlow<CallDetailsUiState>(
            CallDetailsUiState.Content(
                header = syntheticCallDetailsHeader(),
                entries = syntheticCallDetailsEntriesList(),
            ),
        )
        val mockScreenModel = mockk<CallDetailsScreenModel>(relaxed = true) {
            every { uiState } returns uiStateFlow
        }
        var backClicked = false

        composeRule.setContent {
            CallDetailsTestTheme {
                CallDetailsScreen(
                    screenModel = mockScreenModel,
                    onNavigateBack = { backClicked = true },
                )
            }
        }

        composeRule.onNodeWithTag(CALL_DETAILS_SCREEN_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(CALL_DETAILS_NAV_BACK_TEST_TAG).performClick()
        assertTrue(backClicked)

        composeRule.onNodeWithTag(CALL_DETAILS_VOICE_CALL_ACTION_TAG).performClick()
        verify(exactly = 1) { mockScreenModel.onPlaceVoiceCall(any(), any()) }
    }

    @Test
    fun contentStateRendersHeaderAndTestTags() {
        val header = syntheticCallDetailsHeader()
        val entries = syntheticCallDetailsEntriesList()

        composeRule.setContent {
            CallDetailsTestTheme {
                CallDetailsScaffold(
                    uiState = CallDetailsUiState.Content(
                        header = header,
                        entries = entries,
                    ),
                    onNavigateBack = {},
                    onPlaceVoiceCall = { _, _ -> },
                    onPlaceVideoCall = {},
                    onSendSms = {},
                    onCopyNumber = {},
                    onEditNumber = {},
                    onDeleteAllEntries = {},
                    onConfirmDelete = {},
                    onDismissDeleteDialog = {},
                )
            }
        }

        composeRule.onNodeWithTag(CALL_DETAILS_SCREEN_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(CALL_DETAILS_TOP_BAR_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(CALL_DETAILS_HEADER_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(CALL_DETAILS_AVATAR_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(CALL_DETAILS_DISPLAY_NAME_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(CALL_DETAILS_PHONE_NUMBER_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(CALL_DETAILS_VOICE_CALL_ACTION_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(CALL_DETAILS_VIDEO_CALL_ACTION_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(CALL_DETAILS_SMS_ACTION_TAG).assertIsDisplayed()
    }

    @Test
    fun headerActionsTriggerCallbacks() {
        var dialedNumber = ""
        var dialedPostDigits = ""
        var videoClicked = false
        var smsClicked = false

        composeRule.setContent {
            CallDetailsTestTheme {
                CallDetailsScaffold(
                    uiState = CallDetailsUiState.Content(
                        header = syntheticCallDetailsHeader(postDialDigits = ";1234"),
                        entries = syntheticCallDetailsEntriesList(),
                    ),
                    onNavigateBack = {},
                    onPlaceVoiceCall = { phoneNumber, postDialDigits ->
                        dialedNumber = phoneNumber
                        dialedPostDigits = postDialDigits
                    },
                    onPlaceVideoCall = { videoClicked = true },
                    onSendSms = { smsClicked = true },
                    onCopyNumber = {},
                    onEditNumber = {},
                    onDeleteAllEntries = {},
                    onConfirmDelete = {},
                    onDismissDeleteDialog = {},
                )
            }
        }

        composeRule.onNodeWithTag(CALL_DETAILS_VOICE_CALL_ACTION_TAG).performClick()
        assertEquals(SYNTHETIC_NORMALIZED_NUMBER, dialedNumber)
        assertEquals(";1234", dialedPostDigits)

        composeRule.onNodeWithTag(CALL_DETAILS_VIDEO_CALL_ACTION_TAG).performClick()
        assertTrue(videoClicked)

        composeRule.onNodeWithTag(CALL_DETAILS_SMS_ACTION_TAG).performClick()
        assertTrue(smsClicked)
    }

    @Test
    fun entriesAndActionRowsAreRendered() {
        val entries = syntheticCallDetailsEntriesList()
        val firstEntry = entries.first()

        composeRule.setContent {
            CallDetailsTestTheme {
                CallDetailsScaffold(
                    uiState = CallDetailsUiState.Content(
                        header = syntheticCallDetailsHeader(),
                        entries = entries,
                    ),
                    onNavigateBack = {},
                    onPlaceVoiceCall = { _, _ -> },
                    onPlaceVideoCall = {},
                    onSendSms = {},
                    onCopyNumber = {},
                    onEditNumber = {},
                    onDeleteAllEntries = {},
                    onConfirmDelete = {},
                    onDismissDeleteDialog = {},
                )
            }
        }

        val rowTag = callDetailsEntryTag(firstEntry.callId)
        val iconTag = callDetailsEntryTypeIconTag(firstEntry.callId)
        val typeTag = callDetailsEntryTypeTextTag(firstEntry.callId)
        val dateTag = callDetailsEntryDateTextTag(firstEntry.callId)

        composeRule.onNodeWithTag(CALL_DETAILS_ENTRIES_LIST_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(CALL_DETAILS_ENTRIES_LIST_TEST_TAG)
            .performScrollToNode(hasTestTag(rowTag))
        composeRule.onNodeWithTag(rowTag).assertIsDisplayed()
        composeRule.onNodeWithTag(iconTag, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag(typeTag, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag(dateTag, useUnmergedTree = true).assertIsDisplayed()

        composeRule.onNodeWithTag(CALL_DETAILS_ENTRIES_LIST_TEST_TAG)
            .performScrollToNode(hasTestTag(CALL_DETAILS_COPY_ACTION_TAG))
        composeRule.onNodeWithTag(CALL_DETAILS_COPY_ACTION_TAG).assertIsDisplayed()

        composeRule.onNodeWithTag(CALL_DETAILS_ENTRIES_LIST_TEST_TAG)
            .performScrollToNode(hasTestTag(CALL_DETAILS_EDIT_NUMBER_ACTION_TAG))
        composeRule.onNodeWithTag(CALL_DETAILS_EDIT_NUMBER_ACTION_TAG).assertIsDisplayed()

        composeRule.onNodeWithTag(CALL_DETAILS_ENTRIES_LIST_TEST_TAG)
            .performScrollToNode(hasTestTag(CALL_DETAILS_DELETE_MENU_ITEM_TAG))
        composeRule.onNodeWithTag(CALL_DETAILS_DELETE_MENU_ITEM_TAG).assertIsDisplayed()
    }

    @Test
    fun loadingStateRendersIndicator() {
        composeRule.setContent {
            CallDetailsTestTheme {
                CallDetailsScaffold(
                    uiState = CallDetailsUiState.Loading,
                    onNavigateBack = {},
                    onPlaceVoiceCall = { _, _ -> },
                    onPlaceVideoCall = {},
                    onSendSms = {},
                    onCopyNumber = {},
                    onEditNumber = {},
                    onDeleteAllEntries = {},
                    onConfirmDelete = {},
                    onDismissDeleteDialog = {},
                )
            }
        }

        composeRule.onNodeWithTag(CALL_DETAILS_LOADING_INDICATOR_TAG).assertIsDisplayed()
    }

    @Test
    fun unavailableStateRendersText() {
        composeRule.setContent {
            CallDetailsTestTheme {
                CallDetailsScaffold(
                    uiState = CallDetailsUiState.Unavailable,
                    onNavigateBack = {},
                    onPlaceVoiceCall = { _, _ -> },
                    onPlaceVideoCall = {},
                    onSendSms = {},
                    onCopyNumber = {},
                    onEditNumber = {},
                    onDeleteAllEntries = {},
                    onConfirmDelete = {},
                    onDismissDeleteDialog = {},
                )
            }
        }

        composeRule.onNodeWithTag(CALL_DETAILS_UNAVAILABLE_TEXT_TAG).assertIsDisplayed()
    }

    @Test
    fun deleteDialogDisplaysAndConfirms() {
        var confirmedState: CallDetailsDeleteDialogState? = null

        composeRule.setContent {
            CallDetailsTestTheme {
                CallDetailsScaffold(
                    uiState = CallDetailsUiState.Content(
                        header = syntheticCallDetailsHeader(),
                        entries = syntheticCallDetailsEntriesList(),
                        deleteDialogState = CallDetailsDeleteDialogState.DeleteAll,
                    ),
                    onNavigateBack = {},
                    onPlaceVoiceCall = { _, _ -> },
                    onPlaceVideoCall = {},
                    onSendSms = {},
                    onCopyNumber = {},
                    onEditNumber = {},
                    onDeleteAllEntries = {},
                    onConfirmDelete = { confirmedState = it },
                    onDismissDeleteDialog = {},
                )
            }
        }

        composeRule.onNodeWithTag(CALL_DETAILS_DELETE_DIALOG_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(CALL_DETAILS_DELETE_CONFIRM_BUTTON_TAG).performClick()
        assertEquals(CallDetailsDeleteDialogState.DeleteAll, confirmedState)
    }

    @Test
    fun avatarClick_triggersOnOpenContact() {
        val mockScreenModel = mockk<CallDetailsScreenModel>(relaxed = true) {
            every { uiState } returns MutableStateFlow(
                CallDetailsUiState.Content(
                    header = syntheticCallDetailsHeader(
                        contactUri = "content://com.android.contacts/contacts/lookup/123/456",
                    ),
                    entries = syntheticCallDetailsEntriesList(),
                ),
            )
        }

        composeRule.setContent {
            CallDetailsTestTheme {
                CallDetailsScreen(
                    screenModel = mockScreenModel,
                    onNavigateBack = {},
                )
            }
        }

        composeRule.onNodeWithTag(CALL_DETAILS_AVATAR_TEST_TAG).performClick()
        verify(exactly = 1) { mockScreenModel.onOpenContact() }
    }
}
