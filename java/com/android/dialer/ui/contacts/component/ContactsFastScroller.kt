package com.android.dialer.ui.contacts.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import com.android.dialer.ui.contacts.common.CONTACTS_FAST_SCROLLER_LABEL_TEST_TAG
import com.android.dialer.ui.contacts.common.CONTACTS_FAST_SCROLLER_TEST_TAG
import com.android.dialer.ui.contacts.common.FastScrollerBubbleGap
import com.android.dialer.ui.contacts.common.FastScrollerBubbleSize
import com.android.dialer.ui.contacts.common.FastScrollerThumbHeight
import com.android.dialer.ui.contacts.common.FastScrollerThumbWidth
import com.android.dialer.ui.contacts.common.FastScrollerTouchTargetWidth
import com.android.dialer.ui.contacts.common.FastScrollerTrackInset
import com.android.dialer.ui.contacts.screen.model.ContactRowUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

@Composable
internal fun ContactsFastScroller(
    listState: LazyListState,
    rows: ImmutableList<ContactRowUiModel>,
    hasAddContactRow: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!rememberIsFastScrollerVisible(listState = listState)) return

    val scope = rememberCoroutineScope()
    var trackHeightPx by remember { mutableIntStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val progress = when {
        isDragging -> dragProgress
        else -> rememberListScrollProgress(listState = listState)
    }

    val totalItems by remember { derivedStateOf { listState.layoutInfo.totalItemsCount } }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(FastScrollerTouchTargetWidth)
            .onSizeChanged { size -> trackHeightPx = size.height }
            .testTag(CONTACTS_FAST_SCROLLER_TEST_TAG)
            .fastScrollerDrag(
                onDragStateChange = { dragging -> isDragging = dragging },
                onPositionChange = { positionY ->
                    dragProgress =
                        trackProgress(positionY = positionY, trackHeightPx = trackHeightPx)
                    scope.launch { listState.scrollToItem(targetIndex(listState, dragProgress)) }
                },
            ),
    ) {
        val thumbOffset = with(LocalDensity.current) {
            ((trackHeightPx - FastScrollerThumbHeight.toPx()) * progress).toDp()
        }

        FastScrollerThumb(offsetY = thumbOffset, modifier = Modifier.align(Alignment.TopEnd))

        if (isDragging) {
            FastScrollerBubble(
                label = fastScrollerLabel(
                    rows = rows,
                    progress = progress,
                    totalItems = totalItems,
                    hasAddContactRow = hasAddContactRow,
                ),
                offsetY = thumbOffset,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }
}

@Composable
private fun rememberIsFastScrollerVisible(listState: LazyListState): Boolean {
    val isVisible by remember {
        derivedStateOf {
            isFastScrollerVisible(
                totalItems = listState.layoutInfo.totalItemsCount,
                visibleItems = listState.layoutInfo.visibleItemsInfo.size,
            )
        }
    }

    return isVisible
}

@Composable
private fun rememberListScrollProgress(listState: LazyListState): Float {
    val progress by remember {
        derivedStateOf {
            listScrollProgress(
                firstVisibleIndex = listState.firstVisibleItemIndex,
                totalItems = listState.layoutInfo.totalItemsCount,
                visibleItems = listState.layoutInfo.visibleItemsInfo.size,
            )
        }
    }

    return progress
}

private fun trackProgress(positionY: Float, trackHeightPx: Int): Float =
    when {
        trackHeightPx <= 0 -> 0f
        else -> (positionY / trackHeightPx).coerceIn(minimumValue = 0f, maximumValue = 1f)
    }

private fun targetIndex(listState: LazyListState, progress: Float): Int =
    fastScrollTargetIndex(progress = progress, totalItems = listState.layoutInfo.totalItemsCount)

private fun Modifier.fastScrollerDrag(
    onDragStateChange: (Boolean) -> Unit,
    onPositionChange: (Float) -> Unit,
): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        onDragStateChange(true)

        var pointer: PointerInputChange? = down
        while (pointer != null && pointer.pressed) {
            onPositionChange(pointer.position.y)
            pointer.consume()
            pointer = awaitPointerEvent().changes.firstOrNull { change -> change.id == down.id }
        }

        onDragStateChange(false)
    }
}

@Composable
private fun FastScrollerThumb(offsetY: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(end = FastScrollerTrackInset)
            .offset(y = offsetY)
            .size(width = FastScrollerThumbWidth, height = FastScrollerThumbHeight)
            .clip(RoundedCornerShape(size = FastScrollerThumbWidth))
            .background(MaterialTheme.colorScheme.primary),
    )
}

@Composable
private fun FastScrollerBubble(
    label: String,
    offsetY: Dp,
    modifier: Modifier = Modifier,
) {
    if (label.isEmpty()) return

    Box(
        modifier = modifier
            .offset(
                x = -(FastScrollerBubbleSize + FastScrollerBubbleGap),
                y = offsetY - (FastScrollerBubbleSize - FastScrollerThumbHeight) / 2,
            )
            .size(FastScrollerBubbleSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.testTag(CONTACTS_FAST_SCROLLER_LABEL_TEST_TAG),
        )
    }
}

internal fun isFastScrollerVisible(totalItems: Int, visibleItems: Int): Boolean =
    totalItems > visibleItems && visibleItems > 0

internal fun fastScrollTargetIndex(progress: Float, totalItems: Int): Int {
    if (totalItems <= 0) return 0

    return (progress.coerceIn(minimumValue = 0f, maximumValue = 1f) * totalItems)
        .toInt()
        .coerceIn(minimumValue = 0, maximumValue = totalItems - 1)
}

internal fun listScrollProgress(
    firstVisibleIndex: Int,
    totalItems: Int,
    visibleItems: Int,
): Float {
    val scrollableItems = totalItems - visibleItems
    if (scrollableItems <= 0) return 0f

    return (firstVisibleIndex.toFloat() / scrollableItems)
        .coerceIn(minimumValue = 0f, maximumValue = 1f)
}

internal fun fastScrollerLabel(
    rows: List<ContactRowUiModel>,
    progress: Float,
    totalItems: Int,
    hasAddContactRow: Boolean,
): String {
    val targetIndex = fastScrollTargetIndex(progress = progress, totalItems = totalItems)
    val rowIndex = targetIndex - if (hasAddContactRow) 1 else 0

    return rows.getOrNull(rowIndex)?.sectionLabel.orEmpty()
}
