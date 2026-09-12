package com.android.dialer.ui.recents.screen.recentsscreen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.android.dialer.ui.core.DialerTheme
import com.android.dialer.ui.recents.common.previewActionLabels
import com.android.dialer.ui.recents.model.RecentsContentUiState
import com.android.dialer.ui.recents.model.RecentsEffect
import com.android.dialer.ui.recents.model.RecentsItemUiModel
import com.android.dialer.ui.recents.model.RecentsListItemUiModel
import com.android.dialer.ui.recents.model.RecentsUiState
import com.android.dialer.ui.recents.screen.RecentsEffectHandler
import com.android.dialer.ui.recents.screen.RecentsScreen
import com.android.dialer.ui.recents.screen.RecentsScreenModel
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Before
import org.junit.Rule

@Suppress("AbstractClassCanBeConcreteClass")
internal abstract class BaseRecentsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    protected val uiState = MutableStateFlow(RecentsUiState())
    protected val effects = Channel<RecentsEffect>(capacity = Channel.BUFFERED)
    protected val screenModel = mockk<RecentsScreenModel>()
    protected val effectHandler = mockk<RecentsEffectHandler>(relaxed = true)
    protected var permissionRequests = 0
    protected var dialpadRequests = 0

    @Before
    fun stubScreenModel() {
        every { screenModel.uiState } returns uiState
        every { screenModel.effects } returns effects.receiveAsFlow()
        every { screenModel.onAction(any()) } just runs
    }

    protected fun setContent(state: RecentsUiState = RecentsUiState()) {
        uiState.value = state

        composeTestRule.setContent {
            DialerTheme {
                RecentsScreen(
                    screenModel = screenModel,
                    effectHandler = effectHandler,
                    onRequestPermission = { permissionRequests++ },
                    onShowDialpad = { dialpadRequests++ },
                )
            }
        }
    }

    protected fun scrollToAndClick(tag: String) {
        val node = composeTestRule.onNodeWithTag(testTag = tag)
        node.performScrollTo()
        composeTestRule.waitForIdle()
        node.performClick()
    }

    protected fun entriesState(vararg items: RecentsItemUiModel): RecentsUiState {
        return RecentsUiState(
            content = RecentsContentUiState.Entries(
                items = items.map { item -> RecentsListItemUiModel.Entry(item = item) }
                    .toImmutableList(),
            ),
            actionLabels = previewActionLabels(),
            writeFailedMessage = WRITE_FAILED_MESSAGE,
        )
    }

    protected companion object {
        const val WRITE_FAILED_MESSAGE = "Couldn't update call history"
    }
}
