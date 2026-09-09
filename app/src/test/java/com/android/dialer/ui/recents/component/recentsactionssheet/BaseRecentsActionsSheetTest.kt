package com.android.dialer.ui.recents.component.recentsactionssheet

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.testutil.RobolectricComposeActivityRule
import com.android.dialer.ui.core.DialerTheme
import com.android.dialer.ui.recents.common.previewActionLabels
import com.android.dialer.ui.recents.common.previewRecentsItem
import com.android.dialer.ui.recents.component.RecentsActionsSheetContent
import com.android.dialer.ui.recents.model.RecentsItemUiModel
import com.android.dialer.ui.recents.model.RecentsSheetAction
import org.junit.Rule

@Suppress("AbstractClassCanBeConcreteClass")
internal abstract class BaseRecentsActionsSheetTest {

    @get:Rule(order = 0)
    val componentActivityRule = RobolectricComposeActivityRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    protected val emittedActions = mutableListOf<RecentsSheetAction>()

    protected fun setContent(
        item: RecentsItemUiModel,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    ) {
        composeTestRule.setContent {
            DialerTheme {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    RecentsActionsSheetContent(
                        item = item,
                        labels = previewActionLabels(),
                        onAction = { action -> emittedActions.add(action) },
                    )
                }
            }
        }
    }

    protected fun item(
        primaryText: String = PRIMARY_TEXT,
        displayNumber: String = DISPLAY_NUMBER,
        canCallBack: Boolean = true,
        canVideoCall: Boolean = false,
        canMessage: Boolean = true,
        canAddContact: Boolean = true,
        canEditNumberBeforeCall: Boolean = canCallBack,
    ): RecentsItemUiModel {
        return previewRecentsItem(
            entryId = CallLogEntryId(value = 7L),
            primaryText = primaryText,
            displayNumber = displayNumber,
            canCallBack = canCallBack,
            canVideoCall = canVideoCall,
            canMessage = canMessage,
            canAddContact = canAddContact,
            canEditNumberBeforeCall = canEditNumberBeforeCall,
        )
    }

    protected companion object {
        const val PRIMARY_TEXT = "Caller 7"
        const val DISPLAY_NUMBER = "+1 555-0007"
    }
}
