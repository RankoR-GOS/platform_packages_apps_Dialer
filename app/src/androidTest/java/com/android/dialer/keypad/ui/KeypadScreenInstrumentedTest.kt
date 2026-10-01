package com.android.dialer.keypad.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.dialer.R
import com.android.dialer.keypad.model.KeypadAction
import com.android.dialer.keypad.model.KeypadError
import com.android.dialer.keypad.model.KeypadKey
import com.android.dialer.keypad.model.KeypadUiState
import com.android.dialer.theme.compose.DialerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Only what needs the app's real resources, which unit tests lack: the strings [keypadStrings]
 * resolves, and the overflow menu and error dialog, which cannot open without them.
 */
@RunWith(AndroidJUnit4::class)
class KeypadScreenInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val actions = mutableListOf<KeypadAction>()
    private var uiState by mutableStateOf(KeypadUiState())

    @Test
    fun theCallButtonAndBackspaceUseTheirResources() {
        uiState = KeypadUiState(digits = "5", isDeleteEnabled = true)
        render()

        composeRule.onNodeWithTag(KEYPAD_CALL_TEST_TAG)
            .assert(hasText(context.getString(R.string.call)))
            .assertIsDisplayed()
        composeRule.onNodeWithTag(KEYPAD_DELETE_TEST_TAG)
            .assertContentDescriptionEquals(context.getString(R.string.description_delete_button))
    }

    @Test
    fun keysOneAndZeroOfferTheirLongPressToAccessibility() {
        render()

        composeRule.onNodeWithTag(keypadKeyTestTag(KeypadKey.ONE))
            .assert(hasLongClickLabel(context.getString(R.string.description_voicemail_button)))
        composeRule.onNodeWithTag(keypadKeyTestTag(KeypadKey.ZERO))
            .assert(hasLongClickLabel(context.getString(R.string.description_image_button_plus)))
        composeRule.onNodeWithTag(keypadKeyTestTag(KeypadKey.TWO))
            .assertContentDescriptionEquals("2, A B C")
    }

    @Test
    fun theOverflowMenuOffersPauseAndWait() {
        uiState = KeypadUiState(digits = "5", isDeleteEnabled = true, isOverflowVisible = true)
        render()

        val overflow = context.getString(R.string.description_dialpad_overflow)
        composeRule.onNodeWithTag(KEYPAD_OVERFLOW_TEST_TAG)
            .assertContentDescriptionEquals(overflow)
            .performClick()

        composeRule.onNode(menuItem(KEYPAD_OVERFLOW_PAUSE_TEST_TAG, R.string.add_2sec_pause))
            .assertIsDisplayed()
        composeRule.onNode(menuItem(KEYPAD_OVERFLOW_WAIT_TEST_TAG, R.string.add_wait))
            .assertIsDisplayed()
        // Only offered when an account supports call subjects.
        composeRule.onNodeWithTag(KEYPAD_OVERFLOW_CALL_WITH_NOTE_TEST_TAG).assertDoesNotExist()

        composeRule.onNodeWithTag(KEYPAD_OVERFLOW_WAIT_TEST_TAG).performClick()

        assertEquals(listOf(KeypadAction.WaitClicked), actions)
    }

    @Test
    fun theOverflowMenuOffersCallWithANoteWhenAvailable() {
        uiState = KeypadUiState(
            digits = "5",
            isDeleteEnabled = true,
            isOverflowVisible = true,
            isCallWithNoteAvailable = true,
        )
        render()

        composeRule.onNodeWithTag(KEYPAD_OVERFLOW_TEST_TAG).performClick()
        composeRule.onNode(
            menuItem(KEYPAD_OVERFLOW_CALL_WITH_NOTE_TEST_TAG, R.string.call_with_a_note),
        ).performClick()

        assertEquals(listOf(KeypadAction.CallWithNoteClicked), actions)
    }

    @Test
    fun eachErrorShowsItsTitleAndMessage() {
        render()

        listOf(
            KeypadError.VOICEMAIL_AIRPLANE_MODE to listOf(
                R.string.keypad_voicemail_unavailable_title,
                R.string.dialog_voicemail_airplane_mode_message,
            ),
            KeypadError.VOICEMAIL_NOT_READY to listOf(
                R.string.keypad_voicemail_unavailable_title,
                R.string.dialog_voicemail_not_ready_message,
            ),
            KeypadError.PROHIBITED_NUMBER to listOf(R.string.dialog_phone_call_prohibited_message),
        ).forEach { (error, texts) ->
            uiState = KeypadUiState(error = error)

            texts.forEach { text ->
                composeRule.onNode(inErrorDialog(text)).assertIsDisplayed()
            }
        }
    }

    @Test
    fun theAirplaneModeErrorOffersItsSettings() {
        uiState = KeypadUiState(error = KeypadError.VOICEMAIL_AIRPLANE_MODE)
        render()

        composeRule.onNodeWithTag(KEYPAD_ERROR_DIALOG_CONFIRM_TEST_TAG)
            .assert(hasText(context.getString(R.string.voicemail_action_turn_off_airplane_mode)))
            .performClick()

        assertEquals(listOf(KeypadAction.AirplaneModeSettingsClicked), actions)
    }

    @Test
    fun theVoicemailErrorOffersItsSettings() {
        uiState = KeypadUiState(error = KeypadError.VOICEMAIL_NOT_READY)
        render()

        composeRule.onNodeWithTag(KEYPAD_ERROR_DIALOG_CONFIRM_TEST_TAG)
            .assert(hasText(context.getString(R.string.dialer_settings_label)))
            .performClick()

        assertEquals(listOf(KeypadAction.VoicemailSettingsClicked), actions)
    }

    @Test
    fun cancelDismissesAVoicemailError() {
        uiState = KeypadUiState(error = KeypadError.VOICEMAIL_NOT_READY)
        render()

        composeRule.onNodeWithTag(KEYPAD_ERROR_DIALOG_DISMISS_TEST_TAG)
            .assert(hasText(context.getString(android.R.string.cancel)))
            .performClick()

        assertEquals(listOf(KeypadAction.ErrorDismissed), actions)
    }

    @Test
    fun okDismissesTheProhibitedNumberError() {
        uiState = KeypadUiState(error = KeypadError.PROHIBITED_NUMBER)
        render()

        composeRule.onNodeWithTag(KEYPAD_ERROR_DIALOG_DISMISS_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithTag(KEYPAD_ERROR_DIALOG_CONFIRM_TEST_TAG)
            .assert(hasText(context.getString(android.R.string.ok)))
            .performClick()

        assertEquals(listOf(KeypadAction.ErrorDismissed), actions)
    }

    private fun render() {
        composeRule.setContent {
            DialerTheme {
                KeypadScreen(
                    uiState = uiState,
                    strings = keypadStrings(),
                    onAction = { action -> actions += action },
                )
            }
        }
    }

    private fun inErrorDialog(text: Int): SemanticsMatcher {
        return hasText(context.getString(text)) and
            hasAnyAncestor(hasTestTag(KEYPAD_ERROR_DIALOG_TEST_TAG))
    }

    private fun menuItem(tag: String, label: Int): SemanticsMatcher {
        return hasTestTag(tag) and hasText(context.getString(label))
    }

    private fun hasLongClickLabel(label: String): SemanticsMatcher {
        return SemanticsMatcher("long click label is $label") {
            it.config.getOrNull(SemanticsActions.OnLongClick)?.label == label
        }
    }
}
