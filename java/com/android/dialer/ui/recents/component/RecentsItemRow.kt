package com.android.dialer.ui.recents.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.dialer.R
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.ui.core.DialerPreviewTheme
import com.android.dialer.ui.recents.common.RECENTS_ASSISTED_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_CALL_TYPE_ICON_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_HD_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_RTT_TEST_TAG
import com.android.dialer.ui.recents.common.previewRecentsItems
import com.android.dialer.ui.recents.common.recentsItemAccountTestTag
import com.android.dialer.ui.recents.common.recentsItemAvatarTestTag
import com.android.dialer.ui.recents.common.recentsItemCallButtonTestTag
import com.android.dialer.ui.recents.common.recentsItemPrimaryTextTestTag
import com.android.dialer.ui.recents.common.recentsItemSecondaryTextTestTag
import com.android.dialer.ui.recents.common.recentsItemTestTag
import com.android.dialer.ui.recents.model.RecentsCallTypeIcon
import com.android.dialer.ui.recents.model.RecentsItemUiModel

private val RowShape = RoundedCornerShape(percent = 50)
private val ItemHorizontalPadding = 8.dp
private val ItemVerticalPadding = 8.dp
private val ItemCallTypeIconSize = 18.dp
private val ItemSecondarySpacing = 4.dp
private val ItemCallButtonSize = 48.dp
private val ItemCallButtonIconSize = 24.dp
private val PreviewRowSpacing = 2.dp
private const val SECONDARY_TEXT_MAX_LINES = 2

@Composable
internal fun RecentsItemRow(
    item: RecentsItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCallClick: (() -> Unit)? = null,
    onVideoCallClick: (() -> Unit)? = null,
) {
    val onCallActionClick = onVideoCallClick ?: onCallClick
    val callActionLabel = item.callActionLabel

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RowShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .clickable(onClickLabel = item.clickActionLabel, onClick = onClick)
                .recentsItemSemantics(item = item, onCallActionClick = onCallActionClick)
                .testTag(tag = recentsItemTestTag(entryId = item.entryId))
                .padding(horizontal = ItemHorizontalPadding, vertical = ItemVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RecentsItemAvatar(
                avatar = item.avatar,
                colorSeed = avatarColorSeed(number = item.number),
                modifier = Modifier.clearAndSetSemantics {
                    testTag = recentsItemAvatarTestTag(entryId = item.entryId)
                },
            )

            Spacer(modifier = Modifier.width(width = ItemHorizontalPadding))

            RecentsItemText(
                item = item,
                modifier = Modifier
                    .weight(weight = 1f)
                    .padding(horizontal = ItemHorizontalPadding)
                    .clearAndSetSemantics {},
            )

            if (onCallActionClick != null && callActionLabel != null) {
                RecentsItemCallButton(
                    entryId = item.entryId,
                    label = callActionLabel,
                    isVideoCall = onVideoCallClick != null,
                    onClick = onCallActionClick,
                )
            }
        }
    }
}

internal fun recentsItemFontWeight(isUnreadMissedCall: Boolean): FontWeight {
    return when {
        isUnreadMissedCall -> FontWeight.Medium
        else -> FontWeight.Normal
    }
}

internal fun recentsItemTextDirection(isNumber: Boolean): TextDirection {
    return when {
        isNumber -> TextDirection.Ltr
        else -> TextDirection.Content
    }
}

private fun Modifier.recentsItemSemantics(
    item: RecentsItemUiModel,
    onCallActionClick: (() -> Unit)?,
): Modifier {
    val callActionLabel = item.callActionLabel

    return semantics {
        contentDescription = item.contentDescription

        if (onCallActionClick != null && callActionLabel != null) {
            customActions = listOf(
                CustomAccessibilityAction(label = callActionLabel) {
                    onCallActionClick()
                    true
                },
            )
        }
    }
}

@Composable
private fun RecentsItemText(
    item: RecentsItemUiModel,
    modifier: Modifier = Modifier,
) {
    val fontWeight = recentsItemFontWeight(isUnreadMissedCall = item.isUnreadMissedCall)
    val secondaryColor = recentsItemSecondaryColor(isUnreadMissedCall = item.isUnreadMissedCall)
    val twoLineHeight = with(LocalDensity.current) {
        MaterialTheme.typography.bodyLarge.lineHeight.toDp() +
            MaterialTheme.typography.bodyMedium.lineHeight.toDp()
    }

    Column(
        modifier = modifier.heightIn(min = twoLineHeight),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = item.primaryText,
            modifier = Modifier.testTag(recentsItemPrimaryTextTestTag(item.entryId)),
            style = MaterialTheme.typography.bodyLarge.copy(
                textDirection = recentsItemTextDirection(isNumber = item.isPrimaryTextTheNumber),
            ),
            fontWeight = fontWeight,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(space = ItemSecondarySpacing),
            verticalAlignment = Alignment.Top,
        ) {
            RecentsItemCallTypeIcon(item.callTypeIcon, secondaryColor)

            Text(
                text = item.secondaryText,
                modifier = Modifier
                    .weight(weight = 1f, fill = false)
                    .testTag(tag = recentsItemSecondaryTextTestTag(entryId = item.entryId)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = fontWeight,
                color = secondaryColor,
                maxLines = SECONDARY_TEXT_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )
        }

        RecentsItemAccount(item = item)

        RecentsItemFeatures(item = item)
    }
}

@Composable
private fun RecentsItemCallTypeIcon(icon: RecentsCallTypeIcon, color: Color) {
    val style = MaterialTheme.typography.bodyMedium
    val lineHeight = with(LocalDensity.current) {
        style.fontSize.toDp() * (style.lineHeight.value / style.fontSize.value)
    }

    Box(
        modifier = Modifier.size(width = ItemCallTypeIconSize, height = lineHeight),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon.toImageVector(),
            contentDescription = null,
            modifier = Modifier.size(ItemCallTypeIconSize).testTag(RECENTS_CALL_TYPE_ICON_TEST_TAG),
            tint = color,
        )
    }
}

@Composable
private fun RecentsItemAccount(item: RecentsItemUiModel) {
    val fontWeight = recentsItemFontWeight(isUnreadMissedCall = item.isUnreadMissedCall)

    item.accountLabel?.let { label ->
        Text(
            text = label,
            modifier = Modifier.testTag(recentsItemAccountTestTag(item.entryId)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = fontWeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RecentsItemFeatures(item: RecentsItemUiModel) {
    if (item.isHdCall || item.isRttCall || item.isAssistedDialing) {
        Row(horizontalArrangement = Arrangement.spacedBy(ItemSecondarySpacing)) {
            if (item.isHdCall) {
                RecentsFeatureIcon(R.drawable.quantum_ic_hd_vd_theme_24, RECENTS_HD_TEST_TAG)
            }
            if (item.isRttCall) {
                RecentsFeatureIcon(R.drawable.quantum_ic_rtt_vd_theme_24, RECENTS_RTT_TEST_TAG)
            }
            if (item.isAssistedDialing) {
                RecentsFeatureIcon(
                    R.drawable.quantum_ic_language_vd_theme_24,
                    RECENTS_ASSISTED_TEST_TAG
                )
            }
        }
    }
}

@Composable
private fun RecentsFeatureIcon(drawable: Int, tag: String) {
    Icon(
        painter = painterResource(drawable),
        contentDescription = null,
        modifier = Modifier.size(ItemCallTypeIconSize).testTag(tag),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun RecentsItemCallButton(
    entryId: CallLogEntryId,
    label: String,
    isVideoCall: Boolean,
    onClick: () -> Unit,
) {
    val icon = when {
        isVideoCall -> Icons.Filled.Videocam
        else -> Icons.Filled.Call
    }

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(size = ItemCallButtonSize)
            .testTag(tag = recentsItemCallButtonTestTag(entryId = entryId)),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(size = ItemCallButtonIconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
@ReadOnlyComposable
private fun recentsItemSecondaryColor(isUnreadMissedCall: Boolean): Color {
    return when {
        isUnreadMissedCall -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun RecentsCallTypeIcon.toImageVector(): ImageVector {
    return when (this) {
        RecentsCallTypeIcon.Incoming -> Icons.AutoMirrored.Filled.CallReceived
        RecentsCallTypeIcon.Outgoing -> Icons.AutoMirrored.Filled.CallMade
        RecentsCallTypeIcon.Missed -> Icons.AutoMirrored.Filled.CallMissed
        RecentsCallTypeIcon.Blocked -> Icons.Filled.Block
        RecentsCallTypeIcon.Voicemail -> Icons.Filled.Voicemail
    }
}

@PreviewLightDark
@Composable
private fun RecentsItemRowPreview() {
    DialerPreviewTheme {
        Column(
            modifier = Modifier.padding(horizontal = ItemHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(space = PreviewRowSpacing),
        ) {
            previewRecentsItems().forEach { item ->
                RecentsItemRow(
                    item = item,
                    onClick = {},
                    onCallClick = {}.takeIf { item.canCallBack },
                    onVideoCallClick = {}.takeIf { item.canVideoCall && item.isVideoCall },
                )
            }
        }
    }
}
