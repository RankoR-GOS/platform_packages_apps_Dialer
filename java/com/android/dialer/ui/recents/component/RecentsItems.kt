package com.android.dialer.ui.recents.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.android.dialer.ui.common.components.safeDrawingContentPadding
import com.android.dialer.ui.core.DialerPreviewTheme
import com.android.dialer.ui.recents.common.RECENTS_LIST_TEST_TAG
import com.android.dialer.ui.recents.common.previewRecentsListItems
import com.android.dialer.ui.recents.model.RecentsItemEvent
import com.android.dialer.ui.recents.model.RecentsItemUiModel
import com.android.dialer.ui.recents.model.RecentsListItemUiModel
import kotlinx.collections.immutable.ImmutableList

private const val RECENTS_ROW_CONTENT_TYPE = "recents_row"
private const val RECENTS_DAY_HEADER_CONTENT_TYPE = "recents_day_header"
private const val DAY_HEADER_KEY_PREFIX = "day_header_"

private val ItemPlacementSpec = spring(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow,
    visibilityThreshold = IntOffset.VisibilityThreshold,
)

private val ListVerticalSpacing = 2.dp
private val ListContentPadding = 8.dp
internal val RecentsFabBottomReserve = 88.dp

@Composable
internal fun RecentsItems(
    items: ImmutableList<RecentsListItemUiModel>,
    listState: LazyListState,
    onItemEvent: (RecentsItemEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(tag = RECENTS_LIST_TEST_TAG),
        state = listState,
        contentPadding = safeDrawingContentPadding(
            top = ListContentPadding,
            bottom = ListContentPadding + RecentsFabBottomReserve,
            horizontal = ListContentPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(space = ListVerticalSpacing),
    ) {
        items(
            items = items,
            key = { listItem -> listItem.listKey() },
            contentType = { listItem -> listItem.contentType() },
        ) { listItem ->
            when (listItem) {
                is RecentsListItemUiModel.DayHeader -> RecentsDayHeader(
                    label = listItem.label,
                    modifier = Modifier.animateItem(placementSpec = ItemPlacementSpec),
                )

                is RecentsListItemUiModel.Entry -> RecentsEntry(
                    item = listItem.item,
                    onItemEvent = onItemEvent,
                    modifier = Modifier.animateItem(placementSpec = ItemPlacementSpec),
                )
            }
        }
    }
}

@Composable
private fun RecentsEntry(
    item: RecentsItemUiModel,
    onItemEvent: (RecentsItemEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    RecentsItemRow(
        item = item,
        onClick = { onItemEvent(RecentsItemEvent.Clicked(entryId = item.entryId)) },
        modifier = modifier,
        onCallClick = {
            onItemEvent(RecentsItemEvent.CallClicked(entryId = item.entryId, number = item.number))
        }.takeIf { item.canCallBack },
        onVideoCallClick = {
            onItemEvent(
                RecentsItemEvent.VideoCallClicked(entryId = item.entryId, number = item.number),
            )
        }.takeIf { item.canVideoCall },
    )
}

private fun RecentsListItemUiModel.listKey(): Any {
    return when (this) {
        is RecentsListItemUiModel.DayHeader -> DAY_HEADER_KEY_PREFIX + key
        is RecentsListItemUiModel.Entry -> item.entryId.value
    }
}

private fun RecentsListItemUiModel.contentType(): String {
    return when (this) {
        is RecentsListItemUiModel.DayHeader -> RECENTS_DAY_HEADER_CONTENT_TYPE
        is RecentsListItemUiModel.Entry -> RECENTS_ROW_CONTENT_TYPE
    }
}

@PreviewLightDark
@Composable
private fun RecentsItemsPreview() {
    DialerPreviewTheme {
        RecentsItems(
            items = previewRecentsListItems(),
            listState = rememberLazyListState(),
            onItemEvent = {},
        )
    }
}
