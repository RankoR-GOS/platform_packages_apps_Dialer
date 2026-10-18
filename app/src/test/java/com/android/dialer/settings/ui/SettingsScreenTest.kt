package com.android.dialer.settings.ui

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.DisplayOrder
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.SortOrder
import com.android.dialer.settings.model.CallRecordingAudioSource
import com.android.dialer.settings.model.CallRecordingOutputFormat
import com.android.dialer.settings.model.CallRecordingUiState
import com.android.dialer.settings.model.DisplayOptionsUiState
import com.android.dialer.settings.model.SettingsAction as Action
import com.android.dialer.settings.model.SettingsDialog
import com.android.dialer.settings.model.SettingsUiState
import com.android.dialer.theme.compose.DialerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TALL_SCREEN = "w411dp-h2000dp"

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = TALL_SCREEN)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val actions = mutableListOf<Action>()
    private var navigateBackCount = 0

    @Test
    fun loading_showsIndicatorInsteadOfList() {
        setContent(SettingsUiState(isLoading = true))

        composeTestRule.onNodeWithTag(SETTINGS_LOADING_TEST_TAG).assertExists()
        composeTestRule.onNodeWithTag(SETTINGS_LIST_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun loaded_showsListWithoutIndicator() {
        setContent(loadedState())

        composeTestRule.onNodeWithTag(SETTINGS_LIST_TEST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SETTINGS_LOADING_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun backButton_navigatesBack() {
        setContent(loadedState())

        composeTestRule.onNodeWithTag(SETTINGS_BACK_BUTTON_TEST_TAG).performClick()

        composeTestRule.runOnIdle { assertEquals(1, navigateBackCount) }
    }

    @Test
    fun displayOptions_whenAvailable_forwardClicks() {
        setContent(loadedState())

        onItem(SETTINGS_SORT_ORDER_ITEM_TEST_TAG).assertIsDisplayed().performClick()
        onItem(SETTINGS_DISPLAY_ORDER_ITEM_TEST_TAG).assertIsDisplayed().performClick()

        assertActions(Action.SortOrderClicked, Action.DisplayOrderClicked)
    }

    @Test
    fun displayOptions_whenUnavailable_areHidden() {
        setContent(loadedState().copy(displayOptions = null))

        composeTestRule.onNodeWithTag(SETTINGS_SORT_ORDER_ITEM_TEST_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SETTINGS_DISPLAY_ORDER_ITEM_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun generalItems_forwardClicks() {
        setContent(loadedState())

        onItem(SETTINGS_SOUND_AND_VIBRATION_ITEM_TEST_TAG).performClick()
        onItem(SETTINGS_QUICK_RESPONSES_ITEM_TEST_TAG).performClick()
        onItem(SETTINGS_CALL_ACCOUNTS_ITEM_TEST_TAG).performClick()
        onItem(SETTINGS_ACCESSIBILITY_ITEM_TEST_TAG).performClick()

        assertActions(
            Action.SoundAndVibrationClicked,
            Action.QuickResponsesClicked,
            Action.CallAccountsClicked,
            Action.AccessibilityClicked,
        )
    }

    @Test
    fun accessibility_whenNotVisible_isHidden() {
        setContent(loadedState().copy(isAccessibilityVisible = false))

        composeTestRule.onNodeWithTag(SETTINGS_ACCESSIBILITY_ITEM_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun callRecordingV2Switch_reflectsStateAndForwardsToggle() {
        setContent(loadedState())

        onItem(SETTINGS_CALL_RECORDING_V2_ITEM_TEST_TAG).assertIsOff().performClick()

        assertActions(Action.CallRecordingV2Changed(enabled = true))
    }

    @Test
    fun callRecordingV2Switch_whenEnabled_isOn() {
        setContent(loadedState(callRecording = CallRecordingUiState(isV2Enabled = true)))

        onItem(SETTINGS_CALL_RECORDING_V2_ITEM_TEST_TAG).assertIsOn().performClick()

        assertActions(Action.CallRecordingV2Changed(enabled = false))
    }

    @Test
    fun callRecordingItems_forwardClicks() {
        setContent(loadedState())

        onItem(SETTINGS_OUTPUT_FORMAT_ITEM_TEST_TAG).performClick()
        onItem(SETTINGS_AUDIO_SOURCE_ITEM_TEST_TAG).performClick()

        assertActions(Action.OutputFormatClicked, Action.AudioSourceClicked)
    }

    @Test
    fun informationalItems_areNotClickable() {
        setContent(loadedState())

        onItem(SETTINGS_RECORDING_NOTICE_ITEM_TEST_TAG).assertHasNoClickAction()
        onItem(SETTINGS_BUILD_VERSION_ITEM_TEST_TAG).assertHasNoClickAction()
    }

    @Test
    fun aboutItems_forwardClicks() {
        setContent(loadedState())

        onItem(SETTINGS_LICENSES_ITEM_TEST_TAG).assertHasClickAction().performClick()
        onItem(SETTINGS_PRIVACY_POLICY_ITEM_TEST_TAG).performClick()
        onItem(SETTINGS_TERMS_OF_SERVICE_ITEM_TEST_TAG).performClick()

        assertActions(
            Action.LicensesClicked,
            Action.PrivacyPolicyClicked,
            Action.TermsOfServiceClicked
        )
    }

    @Test
    fun noDialogOpen_showsNoDialog() {
        setContent(loadedState())

        composeTestRule.onNodeWithTag(SETTINGS_CHOICE_DIALOG_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun sortOrderDialog_marksCurrentAndForwardsSelection() {
        setContent(loadedState(openDialog = SettingsDialog.SORT_ORDER))

        composeTestRule.onNodeWithTag(SETTINGS_CHOICE_DIALOG_TEST_TAG).assertIsDisplayed()
        onOption(SortOrder.BY_PRIMARY).assertIsSelected()
        onOption(SortOrder.BY_ALTERNATIVE).assertIsNotSelected().performClick()

        assertActions(Action.SortOrderSelected(SortOrder.BY_ALTERNATIVE))
    }

    @Test
    fun displayOrderDialog_forwardsSelection() {
        setContent(loadedState(openDialog = SettingsDialog.DISPLAY_ORDER))

        onOption(DisplayOrder.PRIMARY).assertIsSelected()
        onOption(DisplayOrder.ALTERNATIVE).performClick()

        assertActions(Action.DisplayOrderSelected(DisplayOrder.ALTERNATIVE))
    }

    @Test
    fun outputFormatDialog_withV1_offersNoWav() {
        setContent(loadedState(openDialog = SettingsDialog.OUTPUT_FORMAT))

        onOption(CallRecordingOutputFormat.AAC_MPEG4).assertIsSelected()
        onOption(CallRecordingOutputFormat.AMR_WB).assertIsDisplayed()
        onOption(CallRecordingOutputFormat.WAV).assertDoesNotExist()
    }

    @Test
    fun outputFormatDialog_withV2_offersWav() {
        setContent(
            loadedState(
                callRecording = CallRecordingUiState(
                    isV2Enabled = true,
                    availableOutputFormats = CallRecordingOutputFormat.V2_FORMATS,
                ),
                openDialog = SettingsDialog.OUTPUT_FORMAT,
            ),
        )

        onOption(CallRecordingOutputFormat.WAV).performClick()

        assertActions(Action.OutputFormatSelected(CallRecordingOutputFormat.WAV))
    }

    @Test
    fun audioSourceDialog_forwardsSelection() {
        setContent(loadedState(openDialog = SettingsDialog.AUDIO_SOURCE))

        onOption(CallRecordingAudioSource.VOICE_CALL).assertIsSelected()
        onOption(CallRecordingAudioSource.MIC).performClick()

        assertActions(Action.AudioSourceSelected(CallRecordingAudioSource.MIC))
    }

    @Test
    fun dialogCancel_forwardsDismiss() {
        setContent(loadedState(openDialog = SettingsDialog.AUDIO_SOURCE))

        composeTestRule.onNodeWithTag(SETTINGS_CHOICE_DIALOG_CANCEL_BUTTON_TEST_TAG).performClick()

        assertActions(Action.DialogDismissed)
    }

    private fun setContent(uiState: SettingsUiState) {
        composeTestRule.setContent {
            DialerTheme {
                SettingsScreen(
                    uiState = uiState,
                    onAction = { actions += it },
                    onNavigateBack = { navigateBackCount++ },
                )
            }
        }
    }

    private fun onItem(tag: String): SemanticsNodeInteraction = composeTestRule.onNodeWithTag(tag)

    private fun onOption(option: Enum<*>): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(settingsChoiceOptionTestTag(option))

    private fun assertActions(vararg expected: Action) {
        composeTestRule.runOnIdle { assertEquals(expected.toList(), actions) }
    }

    private fun loadedState(
        callRecording: CallRecordingUiState = CallRecordingUiState(),
        openDialog: SettingsDialog? = null,
    ) = SettingsUiState(
        isLoading = false,
        displayOptions = DisplayOptionsUiState(
            sortOrder = SortOrder.BY_PRIMARY,
            displayOrder = DisplayOrder.PRIMARY,
        ),
        isAccessibilityVisible = true,
        callRecording = callRecording,
        versionName = "23.0",
        openDialog = openDialog,
    )
}
