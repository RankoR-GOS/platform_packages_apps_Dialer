package com.android.dialer.ui.recents.screen

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.dialer.ui.core.DialerPreviewTheme
import com.android.dialer.ui.recents.common.RECENTS_EMPTY_ACTION_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_EMPTY_STATE_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_PERMISSION_ACTION_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_PERMISSION_STATE_TEST_TAG
import com.android.dialer.ui.recents.common.previewRecentsListItems
import com.android.dialer.ui.recents.component.RecentsItems
import com.android.dialer.ui.recents.component.RecentsStatusMessage
import com.android.dialer.ui.recents.model.RecentsContentUiState
import com.android.dialer.ui.recents.model.RecentsItemEvent

@Composable
internal fun RecentsContent(
    content: RecentsContentUiState,
    listState: LazyListState,
    onItemEvent: (RecentsItemEvent) -> Unit,
    onGrantPermissionClick: () -> Unit,
    onMakeCallClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (content) {
        RecentsContentUiState.Loading -> Unit

        is RecentsContentUiState.Empty -> RecentsStatusMessage(
            message = content.message,
            icon = Icons.Outlined.History,
            modifier = modifier.testTag(tag = RECENTS_EMPTY_STATE_TEST_TAG),
            actionLabel = content.actionLabel,
            actionTestTag = RECENTS_EMPTY_ACTION_TEST_TAG,
            onActionClick = onMakeCallClick,
        )

        is RecentsContentUiState.PermissionRequired -> RecentsStatusMessage(
            message = content.message,
            icon = Icons.Outlined.Lock,
            modifier = modifier.testTag(tag = RECENTS_PERMISSION_STATE_TEST_TAG),
            actionLabel = content.actionLabel,
            actionTestTag = RECENTS_PERMISSION_ACTION_TEST_TAG,
            onActionClick = onGrantPermissionClick,
        )

        is RecentsContentUiState.Entries -> RecentsItems(
            items = content.items,
            listState = listState,
            onItemEvent = onItemEvent,
            modifier = modifier,
        )
    }
}

@PreviewLightDark
@Composable
private fun RecentsContentEntriesPreview() {
    DialerPreviewTheme {
        RecentsContent(
            content = RecentsContentUiState.Entries(items = previewRecentsListItems()),
            listState = rememberLazyListState(),
            onItemEvent = {},
            onGrantPermissionClick = {},
            onMakeCallClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun RecentsContentPermissionPreview() {
    DialerPreviewTheme {
        RecentsContent(
            content = RecentsContentUiState.PermissionRequired(
                message = "To see your call history, turn on the Phone permission",
                actionLabel = "Turn on",
            ),
            listState = rememberLazyListState(),
            onItemEvent = {},
            onGrantPermissionClick = {},
            onMakeCallClick = {},
        )
    }
}
