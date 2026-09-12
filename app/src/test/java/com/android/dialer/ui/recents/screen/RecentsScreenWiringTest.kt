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
    fun itemEvents_whenARowIsTapped_opensTheSheetForThatRowWithoutAnAction() {
        val actions = mutableListOf<Action>()
        val openedSheets = mutableListOf<CallLogEntryId>()

        handleItemEvent(
            event = RecentsItemEvent.Clicked(entryId = ENTRY_ID),
            content = entries(recentsItemUiModel(id = ENTRY_ID.value, number = NUMBER)),
            onAction = actions::add,
            onOpenSheet = openedSheets::add,
        )

        assertEquals(listOf(ENTRY_ID), openedSheets)
        assertEquals(emptyList<Action>(), actions)
    }

    @Test
    fun itemEvents_whenACallButtonIsTapped_dispatchesTheCallActionWithTheNumber() {
        val actions = mutableListOf<Action>()
        val openedSheets = mutableListOf<CallLogEntryId>()
        val content = entries(recentsItemUiModel(id = ENTRY_ID.value, number = NUMBER))

        listOf(
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

        assertEquals(emptyList<CallLogEntryId>(), openedSheets)
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
    fun videoActions_withARecordedAccount_preserveItFromBothEntryPoints() {
        val item = recentsItemUiModel(id = ENTRY_ID.value, number = NUMBER).copy(
            isVideoCall = true,
            accountComponentName = "example/.Service",
            accountId = "sim2",
        )
        val expected = Action.VideoCallClicked(
            number = NUMBER,
            accountComponentName = "example/.Service",
            accountId = "sim2",
        )
        val actions = mutableListOf<Action>()

        assertEquals(expected, RecentsSheetAction.VideoCall.toAction(item))
        handleItemEvent(
            event = RecentsItemEvent.VideoCallClicked(
                entryId = ENTRY_ID,
                number = NUMBER,
                accountComponentName = "example/.Service",
                accountId = "sim2",
            ),
            content = entries(item),
            onAction = actions::add,
            onOpenSheet = {},
        )

        assertEquals(listOf(expected), actions)
        assertEquals(
            Action.VideoCallClicked(number = NUMBER),
            RecentsSheetAction.VideoCall.toAction(item.copy(isVideoCall = false)),
        )
    }

    @Test
    fun sheetActions_mapEveryMemberToItsScreenActionForThatRow() {
        val groupIds = persistentListOf(ENTRY_ID, CallLogEntryId(value = 6L))
        val item = recentsItemUiModel(id = ENTRY_ID.value, number = NUMBER)
            .copy(groupedEntryIds = groupIds)

        assertEquals(
            listOf(
                Action.CallBackClicked(number = NUMBER),
                Action.VideoCallClicked(number = NUMBER),
                Action.MessageClicked(number = NUMBER),
                Action.CreateContactClicked(number = NUMBER),
                Action.AddContactClicked(number = NUMBER),
                Action.CopyNumberClicked(number = NUMBER),
                Action.EditNumberBeforeCallClicked(number = NUMBER),
                Action.DeleteConfirmed(entryIds = groupIds),
            ),
            listOf(
                RecentsSheetAction.Call,
                RecentsSheetAction.VideoCall,
                RecentsSheetAction.Message,
                RecentsSheetAction.CreateContact,
                RecentsSheetAction.AddContact,
                RecentsSheetAction.CopyNumber,
                RecentsSheetAction.EditNumberBeforeCall,
                RecentsSheetAction.Delete,
            ).map { sheetAction -> sheetAction.toAction(item = item) },
        )
    }

    @Test
    fun sheetActions_withPostDialDigits_appendsThemOnlyForVoiceCallback() {
        val item = recentsItemUiModel(id = ENTRY_ID.value, number = NUMBER)
            .copy(postDialDigits = ",12;34")

        assertEquals(
            Action.CallBackClicked(number = NUMBER + ",12;34"),
            RecentsSheetAction.Call.toAction(item),
        )
        assertEquals(
            Action.VideoCallClicked(number = NUMBER),
            RecentsSheetAction.VideoCall.toAction(item),
        )
        assertEquals(
            Action.MessageClicked(number = NUMBER),
            RecentsSheetAction.Message.toAction(item),
        )
        assertEquals(
            Action.CopyNumberClicked(number = NUMBER),
            RecentsSheetAction.CopyNumber.toAction(item),
        )
        assertEquals(
            Action.CreateContactClicked(number = NUMBER),
            RecentsSheetAction.CreateContact.toAction(item),
        )
        assertEquals(
            Action.AddContactClicked(number = NUMBER),
            RecentsSheetAction.AddContact.toAction(item),
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
