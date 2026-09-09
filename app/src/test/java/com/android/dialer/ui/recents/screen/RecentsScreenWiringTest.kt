package com.android.dialer.ui.recents.screen

import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.testutil.recentsItemUiModel
import com.android.dialer.ui.recents.model.RecentsAction as Action
import com.android.dialer.ui.recents.model.RecentsContentUiState
import com.android.dialer.ui.recents.model.RecentsItemEvent
import com.android.dialer.ui.recents.model.RecentsItemUiModel
import com.android.dialer.ui.recents.model.RecentsListItemUiModel
import com.android.dialer.ui.recents.model.RecentsSheetAction
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class RecentsScreenWiringTest {

    @Test
    fun itemEvents_openTheSheetForATapAndDispatchTheCallActionsWithTheNumber() {
        val actions = mutableListOf<Action>()
        val openedSheets = mutableListOf<CallLogEntryId>()
        val content = entries(recentsItemUiModel(id = ENTRY_ID.value, number = NUMBER))

        listOf(
            RecentsItemEvent.Clicked(entryId = ENTRY_ID),
            RecentsItemEvent.CallClicked(entryId = ENTRY_ID, number = NUMBER),
            RecentsItemEvent.VideoCallClicked(entryId = ENTRY_ID, number = NUMBER),
        ).forEach { event ->
            handleItemEvent(
                event = event,
                content = content,
                onAction = actions::add,
                onOpenSheet = openedSheets::add,
            )
        }

        assertEquals(listOf(ENTRY_ID), openedSheets)
        assertEquals(
            listOf(
                Action.CallBackClicked(number = NUMBER),
                Action.VideoCallClicked(number = NUMBER),
            ),
            actions,
        )
    }

    @Test
    fun itemEvents_onAnUnreadMissedRow_markTheGroupViewedBeforeTheAction() {
        val actions = mutableListOf<Action>()
        val groupIds = persistentListOf(ENTRY_ID, CallLogEntryId(value = 6L))
        val unread = recentsItemUiModel(id = ENTRY_ID.value, number = NUMBER)
            .copy(isUnreadMissedCall = true, groupedEntryIds = groupIds)
        val content = entries(unread)

        handleItemEvent(
            event = RecentsItemEvent.Clicked(entryId = ENTRY_ID),
            content = content,
            onAction = actions::add,
            onOpenSheet = {},
        )
        handleItemEvent(
            event = RecentsItemEvent.CallClicked(entryId = ENTRY_ID, number = NUMBER),
            content = content,
            onAction = actions::add,
            onOpenSheet = {},
        )

        assertEquals(
            listOf(
                Action.EntryViewed(entryIds = groupIds),
                Action.EntryViewed(entryIds = groupIds),
                Action.CallBackClicked(number = NUMBER),
            ),
            actions,
        )
    }

    @Test
    fun sheetActions_mapToTheScreenActionForThatRowWithEveryCallOfTheGroupForDelete() {
        val groupIds = persistentListOf(ENTRY_ID, CallLogEntryId(value = 6L))
        val item = recentsItemUiModel(id = ENTRY_ID.value, number = NUMBER)
            .copy(groupedEntryIds = groupIds)

        assertEquals(
            listOf(
                Action.CallBackClicked(number = NUMBER),
                Action.VideoCallClicked(number = NUMBER),
                Action.MessageClicked(number = NUMBER),
                Action.AddContactClicked(number = NUMBER),
                Action.CopyNumberClicked(number = NUMBER),
                Action.DeleteConfirmed(entryIds = groupIds),
            ),
            listOf(
                RecentsSheetAction.Call,
                RecentsSheetAction.VideoCall,
                RecentsSheetAction.Message,
                RecentsSheetAction.AddContact,
                RecentsSheetAction.CopyNumber,
                RecentsSheetAction.Delete,
            ).map { sheetAction -> sheetAction.toAction(item = item) },
        )
    }

    @Test
    fun sheetTarget_isResolvedAgainstTheCurrentEntriesById() {
        val wanted = recentsItemUiModel(id = 7L)
        val content = entries(recentsItemUiModel(id = 1L), wanted)

        assertEquals(wanted, content.entryOrNull(entryId = 7L))
    }

    @Test
    fun sheetTarget_whenTheEntryIsGoneOrTheContentHasNoEntries_isNull() {
        assertNull(entries(recentsItemUiModel(id = 1L)).entryOrNull(entryId = 7L))
        assertNull(RecentsContentUiState.Loading.entryOrNull(entryId = 7L))
        assertNull(entries(recentsItemUiModel(id = 7L)).entryOrNull(entryId = null))
    }

    private fun entries(vararg items: RecentsItemUiModel): RecentsContentUiState.Entries {
        return RecentsContentUiState.Entries(
            items = items.map { item -> RecentsListItemUiModel.Entry(item = item) }
                .toImmutableList(),
        )
    }

    private companion object {
        val ENTRY_ID = CallLogEntryId(value = 7L)
        const val NUMBER = "+15550007"
    }
}
