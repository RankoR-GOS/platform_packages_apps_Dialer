package com.android.dialer.settings.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.dialer.R
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.DisplayOrder
import com.android.dialer.contacts.displaypreference.ContactDisplayPreferences.SortOrder
import com.android.dialer.settings.model.CallRecordingUiState
import com.android.dialer.settings.model.DisplayOptionsUiState
import com.android.dialer.settings.model.SettingsAction as Action
import com.android.dialer.settings.model.SettingsUiState
import com.android.dialer.theme.compose.DialerTheme

@Composable
internal fun SettingsScreen(
    uiState: SettingsUiState,
    onAction: (Action) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(text = stringResource(R.string.dialer_settings_label)) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag(SETTINGS_BACK_BUTTON_TEST_TAG),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.description_dialpad_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        if (uiState.isLoading) {
            SettingsLoading(contentPadding = contentPadding)
        } else {
            SettingsList(
                uiState = uiState,
                onAction = onAction,
                contentPadding = contentPadding,
            )
        }
    }

    SettingsDialogs(uiState = uiState, onAction = onAction)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsLoading(contentPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator(modifier = Modifier.testTag(SETTINGS_LOADING_TEST_TAG))
    }
}

@Composable
private fun SettingsList(
    uiState: SettingsUiState,
    onAction: (Action) -> Unit,
    contentPadding: PaddingValues,
) {
    val spacing = DialerTheme.spacing
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(SETTINGS_LIST_TEST_TAG),
        contentPadding = PaddingValues(
            start = spacing.medium,
            end = spacing.medium,
            top = contentPadding.calculateTopPadding() + spacing.small,
            bottom = contentPadding.calculateBottomPadding() + spacing.medium,
        ),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        generalSection(uiState = uiState, onAction = onAction)
        uiState.displayOptions?.let { displayOptions ->
            displayOptionsSection(displayOptions = displayOptions, onAction = onAction)
        }
        callRecordingSection(callRecording = uiState.callRecording, onAction = onAction)
        aboutSection(versionName = uiState.versionName, onAction = onAction)
    }
}

internal class SettingsSectionScope {
    val rows = mutableListOf<Pair<String, @Composable (ListItemShapes) -> Unit>>()

    fun row(key: String, content: @Composable (shapes: ListItemShapes) -> Unit) {
        rows += key to content
    }
}

internal fun LazyListScope.settingsSection(
    key: String,
    @StringRes titleRes: Int?,
    build: SettingsSectionScope.() -> Unit,
) {
    val rows = SettingsSectionScope().apply(build).rows
    if (titleRes != null) {
        item(key = "${key}_header", contentType = "header") {
            SettingsSectionHeader(title = stringResource(titleRes))
        }
    }
    rows.forEachIndexed { index, (rowKey, content) ->
        item(key = rowKey, contentType = "row") {
            content(ListItemDefaults.segmentedShapes(index = index, count = rows.size))
        }
    }
}

@PreviewLightDark
@Composable
private fun SettingsScreenPreview() {
    DialerTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                isLoading = false,
                displayOptions = DisplayOptionsUiState(
                    sortOrder = SortOrder.BY_PRIMARY,
                    displayOrder = DisplayOrder.PRIMARY,
                ),
                isAccessibilityVisible = true,
                callRecording = CallRecordingUiState(isV2Enabled = true),
                versionName = "23.0",
            ),
            onAction = {},
            onNavigateBack = {},
        )
    }
}
