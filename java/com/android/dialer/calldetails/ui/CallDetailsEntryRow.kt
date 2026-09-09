package com.android.dialer.calldetails.ui

import android.provider.CallLog.Calls
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.dialer.R
import com.android.dialer.calldetails.callDetailsEntryAccountTag
import com.android.dialer.calldetails.callDetailsEntryDateTextTag
import com.android.dialer.calldetails.callDetailsEntryDurationTextTag
import com.android.dialer.calldetails.callDetailsEntryTag
import com.android.dialer.calldetails.callDetailsEntryTypeIconTag
import com.android.dialer.calldetails.callDetailsEntryTypeTextTag
import com.android.dialer.calldetails.model.CallDetailsEntryUiModel
import com.android.dialer.theme.DialerPreviewTheme

@Composable
internal fun CallDetailsEntryRow(
    entry: CallDetailsEntryUiModel,
    modifier: Modifier = Modifier,
) {
    val callTypeDisplay = resolveCallTypeDisplay(callType = entry.callType)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(callDetailsEntryTag(callId = entry.callId))
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = callTypeDisplay.icon,
            contentDescription = null,
            tint = callTypeDisplay.tint,
            modifier = Modifier
                .size(24.dp)
                .testTag(callDetailsEntryTypeIconTag(callId = entry.callId)),
        )

        Spacer(modifier = Modifier.width(16.dp))

        CallDetailsEntryTextContent(
            entry = entry,
            callTypeDisplay = callTypeDisplay,
            modifier = Modifier.weight(1f),
        )

        if (entry.formattedDuration.isNotBlank()) {
            Text(
                text = entry.formattedDuration,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag(
                    callDetailsEntryDurationTextTag(callId = entry.callId),
                ),
            )
        }
    }
}

@Composable
private fun CallDetailsEntryTextContent(
    entry: CallDetailsEntryUiModel,
    callTypeDisplay: CallTypeDisplay,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = callTypeDisplay.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag(
                    callDetailsEntryTypeTextTag(callId = entry.callId),
                ),
            )
            if (entry.isVideoCall) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.Videocam,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (entry.isRtt) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "RTT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = entry.formattedDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag(callDetailsEntryDateTextTag(callId = entry.callId)),
            )
            if (!entry.accountLabel.isNullOrBlank()) {
                Text(
                    text = " • ${entry.accountLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag(
                        callDetailsEntryAccountTag(callId = entry.callId),
                    ),
                )
            }
        }

        entry.postCallNote?.takeIf { it.isNotBlank() }?.let { note ->
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private data class CallTypeDisplay(
    val title: String,
    val icon: ImageVector,
    val tint: Color,
)

@Composable
private fun resolveCallTypeDisplay(callType: Int): CallTypeDisplay {
    return when (callType) {
        Calls.INCOMING_TYPE -> CallTypeDisplay(
            title = stringResource(R.string.type_incoming),
            icon = Icons.AutoMirrored.Filled.CallReceived,
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Calls.OUTGOING_TYPE -> CallTypeDisplay(
            title = stringResource(R.string.type_outgoing),
            icon = Icons.AutoMirrored.Filled.CallMade,
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Calls.MISSED_TYPE -> CallTypeDisplay(
            title = stringResource(R.string.type_missed),
            icon = Icons.AutoMirrored.Filled.CallMissed,
            tint = MaterialTheme.colorScheme.error,
        )
        Calls.VOICEMAIL_TYPE -> CallTypeDisplay(
            title = stringResource(R.string.type_voicemail),
            icon = Icons.Filled.Voicemail,
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Calls.REJECTED_TYPE -> CallTypeDisplay(
            title = stringResource(R.string.type_rejected),
            icon = Icons.Filled.CallEnd,
            tint = MaterialTheme.colorScheme.error,
        )
        Calls.BLOCKED_TYPE -> CallTypeDisplay(
            title = stringResource(R.string.type_blocked),
            icon = Icons.Filled.Block,
            tint = MaterialTheme.colorScheme.error,
        )
        else -> CallTypeDisplay(
            title = stringResource(R.string.call),
            icon = Icons.Filled.Phone,
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@PreviewLightDark
@Composable
private fun CallDetailsEntryRowPreview() {
    DialerPreviewTheme {
        Column {
            CallDetailsPreviewData.sampleEntries.forEach { entry ->
                CallDetailsEntryRow(entry = entry)
            }
        }
    }
}
