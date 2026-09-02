package com.android.dialer.ui.contacts.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.dialer.ui.contacts.common.CONTACTS_LIST_TEST_TAG
import com.android.dialer.ui.contacts.common.CONTACTS_PINNED_SECTION_TEST_TAG
import com.android.dialer.ui.contacts.common.ContactRowHeight
import com.android.dialer.ui.contacts.common.ContactSectionGutterWidth
import com.android.dialer.ui.contacts.screen.model.ContactRowUiModel
import com.android.dialer.ui.core.DialerPreviewTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ContactsList(
    rows: ImmutableList<ContactRowUiModel>,
    showsAddContactRow: Boolean,
    onContactClick: (Long, Rect) -> Unit,
    onAddContactClick: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag(CONTACTS_LIST_TEST_TAG),
        ) {
            if (showsAddContactRow) {
                item(key = ADD_CONTACT_ROW_KEY) {
                    AddContactRow(onClick = onAddContactClick)
                }
            }

            items(items = rows, key = ContactRowUiModel::id) { row ->
                ContactRow(
                    row = row,
                    onClick = { bounds -> onContactClick(row.id, bounds) },
                )
            }
        }

        PinnedSectionLabel(
            rows = rows,
            listState = listState,
            hasAddContactRow = showsAddContactRow,
        )
    }
}

@Composable
private fun PinnedSectionLabel(
    rows: ImmutableList<ContactRowUiModel>,
    listState: LazyListState,
    hasAddContactRow: Boolean,
) {
    val label by remember(rows, hasAddContactRow) {
        derivedStateOf {
            pinnedSectionLabel(
                rows = rows,
                firstVisibleIndex = listState.firstVisibleItemIndex,
                firstVisibleOffset = listState.firstVisibleItemScrollOffset,
                hasAddContactRow = hasAddContactRow,
            )
        }
    }

    val pinned = label ?: return

    Box(
        modifier = Modifier
            .width(ContactSectionGutterWidth)
            .height(ContactRowHeight),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = pinned,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag(CONTACTS_PINNED_SECTION_TEST_TAG),
        )
    }
}

internal fun pinnedSectionLabel(
    rows: List<ContactRowUiModel>,
    firstVisibleIndex: Int,
    firstVisibleOffset: Int,
    hasAddContactRow: Boolean,
): String? {
    val rowIndex = firstVisibleIndex - if (hasAddContactRow) 1 else 0
    val row = rows.getOrNull(rowIndex) ?: return null

    return when {
        row.sectionLabel.isEmpty() -> null
        row.isSectionStart && firstVisibleOffset == 0 -> null
        else -> row.sectionLabel
    }
}

private const val ADD_CONTACT_ROW_KEY = "add_contact_row"

@PreviewLightDark
@Composable
private fun ContactsListPreview() {
    DialerPreviewTheme {
        ContactsList(
            rows = persistentListOf(
                previewRow(id = 1L, name = "Ada Lovelace", label = "A", isStart = true),
                previewRow(id = 2L, name = "Alan Turing", label = "A", isStart = false),
                previewRow(id = 3L, name = "Grace Hopper", label = "G", isStart = true),
            ),
            showsAddContactRow = true,
            onContactClick = { _, _ -> },
            onAddContactClick = {},
        )
    }
}

private fun previewRow(id: Long, name: String, label: String, isStart: Boolean) =
    ContactRowUiModel(
        id = id,
        lookupKey = "lookup-$id",
        displayName = name,
        photoId = 0L,
        photoUri = null,
        sectionLabel = label,
        isSectionStart = isStart,
    )
