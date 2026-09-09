package com.android.dialer.calldetails.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.dialer.R
import com.android.dialer.calldetails.CALL_DETAILS_ENTRIES_LIST_TEST_TAG
import com.android.dialer.calldetails.CALL_DETAILS_LOADING_INDICATOR_TAG
import com.android.dialer.calldetails.CALL_DETAILS_NAV_BACK_TEST_TAG
import com.android.dialer.calldetails.CALL_DETAILS_SCREEN_TEST_TAG
import com.android.dialer.calldetails.CALL_DETAILS_TOP_BAR_TEST_TAG
import com.android.dialer.calldetails.CALL_DETAILS_UNAVAILABLE_TEXT_TAG
import com.android.dialer.calldetails.model.CallDetailsDeleteDialogState
import com.android.dialer.calldetails.model.CallDetailsUiState
import com.android.dialer.theme.DialerPreviewTheme

@Composable
internal fun CallDetailsScreen(
    screenModel: CallDetailsScreenModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()

    CallDetailsScaffold(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onPlaceVoiceCall = screenModel::onPlaceVoiceCall,
        onPlaceVideoCall = screenModel::onPlaceVideoCall,
        onSendSms = screenModel::onSendSms,
        onCopyNumber = screenModel::onCopyNumber,
        onEditNumber = screenModel::onEditNumber,
        onDeleteAllEntries = screenModel::onDeleteAllEntries,
        onConfirmDelete = screenModel::onConfirmDelete,
        onDismissDeleteDialog = screenModel::onDismissDeleteDialog,
        onOpenContact = screenModel::onOpenContact,
        modifier = modifier,
    )
}

@Composable
internal fun CallDetailsScaffold(
    uiState: CallDetailsUiState,
    onNavigateBack: () -> Unit,
    onPlaceVoiceCall: (phoneNumber: String, postDialDigits: String) -> Unit,
    onPlaceVideoCall: (phoneNumber: String) -> Unit,
    onSendSms: (phoneNumber: String) -> Unit,
    onCopyNumber: (phoneNumber: String) -> Unit,
    onEditNumber: (phoneNumber: String) -> Unit,
    onDeleteAllEntries: () -> Unit,
    onConfirmDelete: (state: CallDetailsDeleteDialogState) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenContact: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.testTag(CALL_DETAILS_SCREEN_TEST_TAG),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CallDetailsTopAppBar(onNavigateBack = onNavigateBack)
        },
    ) { paddingValues ->
        when (uiState) {
            is CallDetailsUiState.Loading -> CallDetailsLoadingContent(
                paddingValues = paddingValues,
            )
            is CallDetailsUiState.Unavailable -> CallDetailsUnavailableContent(
                paddingValues = paddingValues,
            )
            is CallDetailsUiState.Content -> {
                CallDetailsContent(
                    content = uiState,
                    onPlaceVoiceCall = onPlaceVoiceCall,
                    onPlaceVideoCall = onPlaceVideoCall,
                    onSendSms = onSendSms,
                    onCopyNumber = onCopyNumber,
                    onEditNumber = onEditNumber,
                    onDeleteAllEntries = onDeleteAllEntries,
                    onOpenContact = onOpenContact,
                    modifier = Modifier.padding(paddingValues),
                )
                CallDetailsDeleteDialog(
                    state = uiState.deleteDialogState,
                    onConfirm = onConfirmDelete,
                    onDismiss = onDismissDeleteDialog,
                )
            }
        }
    }
}

@Composable
internal fun CallDetailsContent(
    content: CallDetailsUiState.Content,
    onPlaceVoiceCall: (phoneNumber: String, postDialDigits: String) -> Unit,
    onPlaceVideoCall: (phoneNumber: String) -> Unit,
    onSendSms: (phoneNumber: String) -> Unit,
    onCopyNumber: (phoneNumber: String) -> Unit,
    onEditNumber: (phoneNumber: String) -> Unit,
    onDeleteAllEntries: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenContact: () -> Unit = {},
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 16.dp,
        ),
        modifier = modifier
            .fillMaxSize()
            .testTag(CALL_DETAILS_ENTRIES_LIST_TEST_TAG),
    ) {
        item(key = "header") {
            CallDetailsHeader(
                header = content.header,
                onCallClick = {
                    onPlaceVoiceCall(content.header.number, content.header.postDialDigits)
                },
                onVideoCallClick = { onPlaceVideoCall(content.header.number) },
                onSendSmsClick = { onSendSms(content.header.number) },
                onAvatarClick = onOpenContact,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(
            items = content.entries,
            key = { it.callId },
        ) { entry ->
            CallDetailsEntryRow(entry = entry)
        }

        item(key = "actions") {
            Spacer(modifier = Modifier.height(16.dp))
            CallDetailsActionList(
                onCopyNumberClick = { onCopyNumber(content.header.number) },
                onEditNumberClick = { onEditNumber(content.header.number) },
                onDeleteCallLogClick = onDeleteAllEntries,
                canCopy = content.header.number.isNotBlank(),
                canEdit = content.header.number.isNotBlank(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallDetailsTopAppBar(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Text(text = stringResource(R.string.call_details))
        },
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag(CALL_DETAILS_NAV_BACK_TEST_TAG),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = modifier.testTag(CALL_DETAILS_TOP_BAR_TEST_TAG),
    )
}

@Composable
private fun CallDetailsLoadingContent(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.testTag(CALL_DETAILS_LOADING_INDICATOR_TAG),
        )
    }
}

@Composable
private fun CallDetailsUnavailableContent(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.call_details_unavailable),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag(CALL_DETAILS_UNAVAILABLE_TEXT_TAG),
        )
    }
}

@PreviewLightDark
@Composable
private fun CallDetailsScreenContentPreview() {
    DialerPreviewTheme {
        CallDetailsScaffold(
            uiState = CallDetailsUiState.Content(
                header = CallDetailsPreviewData.sampleHeader,
                entries = CallDetailsPreviewData.sampleEntries,
            ),
            onNavigateBack = {},
            onPlaceVoiceCall = { _, _ -> },
            onPlaceVideoCall = {},
            onSendSms = {},
            onCopyNumber = {},
            onEditNumber = {},
            onDeleteAllEntries = {},
            onConfirmDelete = {},
            onDismissDeleteDialog = {},
        )
    }
}
