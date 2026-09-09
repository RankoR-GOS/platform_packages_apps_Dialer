package com.android.dialer.ui.recents.component

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.android.dialer.testutil.RobolectricComposeActivityRule
import com.android.dialer.ui.core.DialerTheme
import com.android.dialer.ui.recents.model.RecentsItemUiModel
import org.junit.Rule

@Suppress("AbstractClassCanBeConcreteClass")
internal abstract class BaseRecentsItemRowTest {

    @get:Rule(order = 0)
    val componentActivityRule = RobolectricComposeActivityRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    protected fun setContent(
        item: RecentsItemUiModel,
        onClick: () -> Unit = {},
        onCallClick: (() -> Unit)? = null,
        onVideoCallClick: (() -> Unit)? = null,
    ) {
        setContent(
            item = { item },
            onClick = onClick,
            onCallClick = onCallClick,
            onVideoCallClick = onVideoCallClick,
        )
    }

    protected fun setContent(
        item: () -> RecentsItemUiModel,
        onClick: () -> Unit = {},
        onCallClick: (() -> Unit)? = null,
        onVideoCallClick: (() -> Unit)? = null,
    ) {
        composeTestRule.setContent {
            DialerTheme {
                val currentItem = item()

                RecentsItemRow(
                    item = currentItem,
                    onClick = onClick,
                    onCallClick = onCallClick.takeIf { currentItem.canCallBack },
                    onVideoCallClick = onVideoCallClick.takeIf { currentItem.canVideoCall },
                )
            }
        }
    }
}
