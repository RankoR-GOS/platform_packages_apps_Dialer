package com.android.dialer.calldetails.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.dialer.R
import com.android.dialer.calldetails.CALL_DETAILS_ACCOUNT_LABEL_TEST_TAG
import com.android.dialer.calldetails.CALL_DETAILS_AVATAR_TEST_TAG
import com.android.dialer.calldetails.CALL_DETAILS_DISPLAY_NAME_TEST_TAG
import com.android.dialer.calldetails.CALL_DETAILS_HEADER_TEST_TAG
import com.android.dialer.calldetails.CALL_DETAILS_PHONE_NUMBER_TEST_TAG
import com.android.dialer.calldetails.CALL_DETAILS_SMS_ACTION_TAG
import com.android.dialer.calldetails.CALL_DETAILS_VIDEO_CALL_ACTION_TAG
import com.android.dialer.calldetails.CALL_DETAILS_VOICE_CALL_ACTION_TAG
import com.android.dialer.calldetails.model.CallDetailsHeaderUiModel
import com.android.dialer.theme.DialerPreviewTheme

@Composable
internal fun CallDetailsHeader(
    header: CallDetailsHeaderUiModel,
    onCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onSendSmsClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAvatarClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(CALL_DETAILS_HEADER_TEST_TAG),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HeaderAvatar(
                header = header,
                onAvatarClick = onAvatarClick,
            )

            Spacer(modifier = Modifier.height(12.dp))

            HeaderContactInfo(header = header)

            Spacer(modifier = Modifier.height(20.dp))

            HeaderActionRow(
                header = header,
                onCallClick = onCallClick,
                onVideoCallClick = onVideoCallClick,
                onSendSmsClick = onSendSmsClick,
            )
        }
    }
}

@Composable
private fun HeaderAvatar(
    header: CallDetailsHeaderUiModel,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isWarning = header.isSpam || header.isBlocked
    val containerColor = if (isWarning) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (isWarning) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    val isClickable = !header.contactUri.isNullOrBlank()

    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(
                enabled = isClickable,
                onClick = onAvatarClick,
            )
            .testTag(CALL_DETAILS_AVATAR_TEST_TAG),
        contentAlignment = Alignment.Center,
    ) {
        val bitmap = header.photoBitmap
        if (bitmap != null && !isWarning) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (isWarning) {
            Icon(
                imageVector = Icons.Filled.Block,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = contentColor,
            )
        } else {
            val initial = header.primaryText.firstOrNull { it.isLetter() }?.uppercaseChar()
            if (initial != null) {
                Text(
                    text = initial.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = contentColor,
                )
            }
        }
    }
}

@Composable
private fun HeaderContactInfo(
    header: CallDetailsHeaderUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Text(
            text = header.primaryText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag(CALL_DETAILS_DISPLAY_NAME_TEST_TAG),
        )

        header.secondaryText?.let { secondary ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = secondary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag(CALL_DETAILS_PHONE_NUMBER_TEST_TAG),
            )
        }

        header.accountLabel?.let { accountLabel ->
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = accountLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag(CALL_DETAILS_ACCOUNT_LABEL_TEST_TAG),
            )
        }
    }
}

@Composable
private fun HeaderActionRow(
    header: CallDetailsHeaderUiModel,
    onCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onSendSmsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasNumber = header.number.isNotBlank()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderActionButton(
            icon = Icons.Filled.Call,
            label = stringResource(R.string.call),
            testTag = CALL_DETAILS_VOICE_CALL_ACTION_TAG,
            onClick = onCallClick,
            enabled = hasNumber,
        )

        HeaderActionButton(
            icon = Icons.Filled.Videocam,
            label = stringResource(R.string.video),
            testTag = CALL_DETAILS_VIDEO_CALL_ACTION_TAG,
            onClick = onVideoCallClick,
            enabled = hasNumber,
        )

        HeaderActionButton(
            icon = Icons.AutoMirrored.Filled.Chat,
            label = stringResource(R.string.message),
            testTag = CALL_DETAILS_SMS_ACTION_TAG,
            onClick = onSendSmsClick,
            enabled = hasNumber,
        )
    }
}

@Composable
private fun HeaderActionButton(
    icon: ImageVector,
    label: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.testTag(testTag),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@PreviewLightDark
@Composable
private fun CallDetailsHeaderPreview() {
    DialerPreviewTheme {
        CallDetailsHeader(
            header = CallDetailsPreviewData.sampleHeader,
            onCallClick = {},
            onVideoCallClick = {},
            onSendSmsClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
