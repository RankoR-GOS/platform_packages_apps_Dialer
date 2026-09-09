package com.android.dialer.ui.recents.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.ui.common.components.bottomBarInsets
import com.android.dialer.ui.core.DialerPreviewTheme
import com.android.dialer.ui.recents.common.RECENTS_SHEET_ADD_CONTACT_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_BLOCK_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_CALL_DETAILS_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_CALL_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_CONTENT_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_COPY_NUMBER_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_CREATE_CONTACT_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_DELETE_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_EDIT_NUMBER_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_MESSAGE_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_SUBTITLE_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_TITLE_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SHEET_VIDEO_CALL_TEST_TAG
import com.android.dialer.ui.recents.common.previewActionLabels
import com.android.dialer.ui.recents.common.previewRecentsItem
import com.android.dialer.ui.recents.model.RecentsActionLabelsUiModel
import com.android.dialer.ui.recents.model.RecentsItemUiModel
import com.android.dialer.ui.recents.model.RecentsSheetAction

private const val DISABLED_CONTENT_ALPHA = 0.38f

private val SheetHorizontalPadding = 24.dp
private val SheetBottomPadding = 8.dp
private val SheetHeaderVerticalPadding = 16.dp
private val SheetHeaderSpacing = 16.dp
private val SheetRowMinHeight = 56.dp
private val SheetRowVerticalPadding = 12.dp
private val SheetRowSpacing = 16.dp
private val SheetIconSize = 24.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecentsActionsSheet(
    target: RecentsItemUiModel?,
    labels: RecentsActionLabelsUiModel,
    onAction: (RecentsSheetAction) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var shownTarget by remember { mutableStateOf(value = target) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(target) {
        when (target) {
            null -> {
                sheetState.hide()
                shownTarget = null
            }
            else -> shownTarget = target
        }
    }

    val item = shownTarget ?: return

    ModalBottomSheet(
        modifier = Modifier.testTag(tag = RECENTS_SHEET_TEST_TAG),
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        RecentsActionsSheetContent(item = item, labels = labels, onAction = onAction)
    }
}

@Composable
internal fun RecentsActionsSheetContent(
    item: RecentsItemUiModel,
    labels: RecentsActionLabelsUiModel,
    onAction: (RecentsSheetAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(tag = RECENTS_SHEET_CONTENT_TEST_TAG)
            .verticalScroll(state = rememberScrollState())
            .windowInsetsPadding(insets = bottomBarInsets())
            .padding(bottom = SheetBottomPadding),
    ) {
        RecentsActionsSheetHeader(item = item)

        HorizontalDivider()

        RecentsSheetContactActions(item = item, labels = labels, onAction = onAction)

        if (item.canCallBack) {
            HorizontalDivider()
        }

        RecentsSheetNumberActions(item = item, labels = labels, onAction = onAction)

        if (item.canCallBack) {
            HorizontalDivider()
        }

        RecentsSheetEntryActions(item = item, labels = labels, onAction = onAction)
    }
}

@Composable
private fun RecentsSheetContactActions(
    item: RecentsItemUiModel,
    labels: RecentsActionLabelsUiModel,
    onAction: (RecentsSheetAction) -> Unit,
) {
    Column {
        if (item.canCallBack) {
            RecentsSheetActionRow(
                icon = Icons.Filled.Call,
                label = labels.call,
                testTag = RECENTS_SHEET_CALL_TEST_TAG,
                onClick = { onAction(RecentsSheetAction.Call) },
            )
        }

        if (item.canVideoCall) {
            RecentsSheetActionRow(
                icon = Icons.Filled.Videocam,
                label = labels.videoCall,
                testTag = RECENTS_SHEET_VIDEO_CALL_TEST_TAG,
                onClick = { onAction(RecentsSheetAction.VideoCall) },
            )
        }

        if (item.canMessage) {
            RecentsSheetActionRow(
                icon = Icons.AutoMirrored.Filled.Message,
                label = labels.message,
                testTag = RECENTS_SHEET_MESSAGE_TEST_TAG,
                onClick = { onAction(RecentsSheetAction.Message) },
            )
        }
    }
}

@Composable
private fun RecentsSheetNumberActions(
    item: RecentsItemUiModel,
    labels: RecentsActionLabelsUiModel,
    onAction: (RecentsSheetAction) -> Unit,
) {
    Column {
        if (item.canAddContact) {
            RecentsSheetActionRow(
                icon = Icons.Filled.PersonAdd,
                label = labels.createContact,
                testTag = RECENTS_SHEET_CREATE_CONTACT_TEST_TAG,
                onClick = { onAction(RecentsSheetAction.CreateContact) },
            )

            RecentsSheetActionRow(
                icon = Icons.Filled.Person,
                label = labels.addContact,
                testTag = RECENTS_SHEET_ADD_CONTACT_TEST_TAG,
                onClick = { onAction(RecentsSheetAction.AddContact) },
            )
        }

        if (item.canCallBack) {
            RecentsSheetActionRow(
                icon = Icons.Filled.ContentCopy,
                label = labels.copyNumber,
                testTag = RECENTS_SHEET_COPY_NUMBER_TEST_TAG,
                onClick = { onAction(RecentsSheetAction.CopyNumber) },
            )
        }

        if (item.canEditNumberBeforeCall) {
            RecentsSheetActionRow(
                icon = Icons.Filled.Dialpad,
                label = labels.editNumberBeforeCall,
                testTag = RECENTS_SHEET_EDIT_NUMBER_TEST_TAG,
                onClick = { onAction(RecentsSheetAction.EditNumberBeforeCall) },
            )
        }
    }
}

@Composable
private fun RecentsSheetEntryActions(
    item: RecentsItemUiModel,
    labels: RecentsActionLabelsUiModel,
    onAction: (RecentsSheetAction) -> Unit,
) {
    Column {
        if (item.canCallBack) {
            RecentsSheetActionRow(
                icon = Icons.Filled.Block,
                label = labels.block,
                testTag = RECENTS_SHEET_BLOCK_TEST_TAG,
                onClick = {},
                isEnabled = false,
            )
        }

        RecentsSheetActionRow(
            icon = Icons.Outlined.Info,
            label = labels.callDetails,
            testTag = RECENTS_SHEET_CALL_DETAILS_TEST_TAG,
            onClick = {},
            isEnabled = false,
        )

        RecentsSheetActionRow(
            icon = Icons.Filled.Delete,
            label = labels.delete,
            testTag = RECENTS_SHEET_DELETE_TEST_TAG,
            onClick = { onAction(RecentsSheetAction.Delete) },
        )
    }
}

@Composable
private fun RecentsActionsSheetHeader(
    item: RecentsItemUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SheetHorizontalPadding, vertical = SheetHeaderVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(space = SheetHeaderSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecentsItemAvatar(
            avatar = item.avatar,
            colorSeed = avatarColorSeed(number = item.number),
        )

        val spokenNumber = Modifier.semantics { contentDescription = item.spokenDisplayNumber }
        val titleSemantics = when {
            item.isPrimaryTextTheNumber -> spokenNumber
            else -> Modifier
        }

        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = item.primaryText,
                modifier = Modifier
                    .testTag(tag = RECENTS_SHEET_TITLE_TEST_TAG)
                    .then(other = titleSemantics),
                style = MaterialTheme.typography.titleMedium.copy(
                    textDirection = recentsItemTextDirection(
                        isNumber = item.isPrimaryTextTheNumber,
                    ),
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (item.displayNumber.isNotBlank() && !item.isPrimaryTextTheNumber) {
                Text(
                    text = item.displayNumber,
                    modifier = Modifier
                        .testTag(tag = RECENTS_SHEET_SUBTITLE_TEST_TAG)
                        .then(other = spokenNumber),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDirection = TextDirection.Ltr,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RecentsSheetActionRow(
    icon: ImageVector,
    label: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    val contentColor = rowContentColor(isEnabled = isEnabled)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = SheetRowMinHeight)
            .clickable(enabled = isEnabled, onClick = onClick)
            .testTag(tag = testTag)
            .padding(horizontal = SheetHorizontalPadding, vertical = SheetRowVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(space = SheetRowSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size = SheetIconSize),
            tint = contentColor,
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor,
        )
    }
}

@Composable
private fun rowContentColor(isEnabled: Boolean): Color {
    val onSurface = MaterialTheme.colorScheme.onSurface

    return when {
        isEnabled -> onSurface
        else -> onSurface.copy(alpha = DISABLED_CONTENT_ALPHA)
    }
}

@PreviewLightDark
@Composable
private fun RecentsActionsSheetContentPreview() {
    DialerPreviewTheme {
        RecentsActionsSheetContent(
            item = previewRecentsItem(
                entryId = CallLogEntryId(value = 1L),
                primaryText = "Ada Lovelace",
                canVideoCall = true,
            ),
            labels = previewActionLabels(),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun RecentsActionsSheetContentPrivateNumberPreview() {
    DialerPreviewTheme {
        RecentsActionsSheetContent(
            item = previewRecentsItem(
                entryId = CallLogEntryId(value = 2L),
                primaryText = "Private number",
                displayNumber = "",
                canCallBack = false,
            ),
            labels = previewActionLabels(),
            onAction = {},
        )
    }
}
