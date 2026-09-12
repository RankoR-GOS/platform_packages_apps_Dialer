package com.android.dialer.ui.recents.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.dialer.ui.core.DialerPreviewTheme

private val StatusMessageIconSize = 96.dp
private val StatusMessageSpacing = 16.dp
private val StatusMessageHorizontalPadding = 32.dp

@Composable
internal fun RecentsStatusMessage(
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    actionTestTag: String? = null,
    onActionClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(horizontal = StatusMessageHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size = StatusMessageIconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = message,
            modifier = Modifier.padding(top = StatusMessageSpacing),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        actionLabel?.let { label ->
            TextButton(
                onClick = onActionClick,
                modifier = Modifier
                    .padding(top = StatusMessageSpacing)
                    .optionalTestTag(tag = actionTestTag),
            ) {
                Text(text = label)
            }
        }
    }
}

private fun Modifier.optionalTestTag(tag: String?): Modifier {
    return when (tag) {
        null -> this
        else -> testTag(tag = tag)
    }
}

@PreviewLightDark
@Composable
private fun RecentsStatusMessageEmptyPreview() {
    DialerPreviewTheme {
        RecentsStatusMessage(
            message = "Your call history is empty",
            icon = Icons.Outlined.History,
        )
    }
}

@PreviewLightDark
@Composable
private fun RecentsStatusMessagePermissionPreview() {
    DialerPreviewTheme {
        RecentsStatusMessage(
            message = "To see your call history, allow access to your call log",
            icon = Icons.Outlined.Lock,
            actionLabel = "Turn on",
            onActionClick = {},
        )
    }
}
