package com.android.dialer.ui.recents.screen

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.dialer.data.recents.model.CallLogEntryId
import com.android.dialer.ui.core.CollectEvents
import com.android.dialer.ui.recents.common.RECENTS_SCREEN_TEST_TAG
import com.android.dialer.ui.recents.common.RECENTS_SNACKBAR_TEST_TAG
import com.android.dialer.ui.recents.common.RecentsFabBottomReserve
import com.android.dialer.ui.recents.component.RecentsActionsSheet
import com.android.dialer.ui.recents.model.RecentsAction as Action
import com.android.dialer.ui.recents.model.RecentsContentUiState
import com.android.dialer.ui.recents.model.RecentsEffect as Effect
import com.android.dialer.ui.recents.model.RecentsItemEvent
import com.android.dialer.ui.recents.model.RecentsItemUiModel
import com.android.dialer.ui.recents.model.RecentsListItemUiModel
import com.android.dialer.ui.recents.model.RecentsSheetAction
import com.android.dialer.util.PermissionsUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
internal fun RecentsRoute(
    screenModel: RecentsScreenModel,
    onShowDialpad: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        results.filterValues { isGranted -> isGranted }.keys.forEach { permission ->
            PermissionsUtil.notifyPermissionGranted(context, permission)
        }

        if (results[Manifest.permission.READ_CALL_LOG] == true) {
            screenModel.onAction(Action.CallLogPermissionGranted)
        }
    }

    RecentsScreen(
        screenModel = screenModel,
        effectHandler = rememberRecentsEffectHandler(),
        onRequestPermission = { permissionLauncher.launch(deniedPhonePermissions(context)) },
        onShowDialpad = onShowDialpad,
        modifier = modifier,
    )
}

@Composable
internal fun RecentsScreen(
    screenModel: RecentsScreenModel,
    effectHandler: RecentsEffectHandler,
    onRequestPermission: () -> Unit,
    onShowDialpad: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var sheetTargetId by rememberSaveable { mutableStateOf<Long?>(value = null) }

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        screenModel.onAction(Action.ScreenResumed)
    }

    RecentsEffects(
        effects = screenModel.effects,
        effectHandler = effectHandler,
        onRequestPermission = onRequestPermission,
        onShowDialpad = onShowDialpad,
        onWriteFailure = {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = uiState.writeFailedMessage)
            }
        },
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag(tag = RECENTS_SCREEN_TEST_TAG),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            RecentsContent(
                content = uiState.content,
                listState = listState,
                onItemEvent = { event ->
                    handleItemEvent(
                        event = event,
                        content = uiState.content,
                        onAction = screenModel::onAction,
                        onOpenSheet = { entryId -> sheetTargetId = entryId.value },
                    )
                },
                onGrantPermissionClick = { screenModel.onAction(Action.GrantPermissionClicked) },
                onMakeCallClick = { screenModel.onAction(Action.MakeCallClicked) },
            )

            RecentsSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(alignment = Alignment.BottomCenter),
            )
        }
    }

    val sheetTarget = uiState.content.entryOrNull(entryId = sheetTargetId)

    RecentsActionsSheet(
        target = sheetTarget,
        labels = uiState.actionLabels,
        onAction = { sheetAction ->
            sheetTargetId = null
            sheetTarget?.let { item -> screenModel.onAction(sheetAction.toAction(item = item)) }
        },
        onDismissRequest = { sheetTargetId = null },
    )
}

@Composable
private fun RecentsSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
            .padding(bottom = RecentsFabBottomReserve)
            .testTag(tag = RECENTS_SNACKBAR_TEST_TAG)
            .semantics { liveRegion = LiveRegionMode.Polite },
    )
}

@Composable
internal fun RecentsEffects(
    effects: Flow<Effect>,
    effectHandler: RecentsEffectHandler,
    onRequestPermission: () -> Unit,
    onShowDialpad: () -> Unit,
    onWriteFailure: () -> Unit,
) {
    CollectEvents(events = effects) { effect ->
        when (effect) {
            Effect.RequestCallLogPermission -> onRequestPermission()
            Effect.ShowDialpad -> onShowDialpad()
            Effect.WriteFailed -> onWriteFailure()
            is Effect.PlaceCall,
            is Effect.PlaceVideoCall,
            is Effect.SendMessage,
            is Effect.CreateContact,
            is Effect.AddContact,
            is Effect.CopyNumber,
            is Effect.EditNumberBeforeCall,
            -> effectHandler.handle(effect = effect)
        }
    }
}

internal fun handleItemEvent(
    event: RecentsItemEvent,
    content: RecentsContentUiState,
    onAction: (Action) -> Unit,
    onOpenSheet: (CallLogEntryId) -> Unit,
) {
    when (event) {
        is RecentsItemEvent.Clicked -> {
            content.markViewed(entryId = event.entryId, onAction = onAction)
            onOpenSheet(event.entryId)
        }
        is RecentsItemEvent.CallClicked -> {
            content.markViewed(entryId = event.entryId, onAction = onAction)
            onAction(Action.CallBackClicked(number = event.number))
        }
        is RecentsItemEvent.VideoCallClicked -> {
            content.markViewed(entryId = event.entryId, onAction = onAction)
            onAction(Action.VideoCallClicked(number = event.number))
        }
    }
}

private fun RecentsContentUiState.markViewed(entryId: CallLogEntryId, onAction: (Action) -> Unit) {
    val item = entryOrNull(entryId = entryId.value) ?: return

    if (item.isUnreadMissedCall) {
        onAction(Action.EntryViewed(entryIds = item.groupedEntryIds))
    }
}

internal fun RecentsSheetAction.toAction(item: RecentsItemUiModel): Action {
    return when (this) {
        RecentsSheetAction.Call -> Action.CallBackClicked(number = item.number)
        RecentsSheetAction.VideoCall -> Action.VideoCallClicked(number = item.number)
        RecentsSheetAction.Message -> Action.MessageClicked(number = item.number)
        RecentsSheetAction.CreateContact -> Action.CreateContactClicked(number = item.number)
        RecentsSheetAction.AddContact -> Action.AddContactClicked(number = item.number)
        RecentsSheetAction.CopyNumber -> Action.CopyNumberClicked(number = item.number)
        RecentsSheetAction.EditNumberBeforeCall -> {
            Action.EditNumberBeforeCallClicked(number = item.number)
        }
        RecentsSheetAction.Delete -> Action.DeleteConfirmed(entryIds = item.groupedEntryIds)
    }
}

internal fun RecentsContentUiState.entryOrNull(entryId: Long?): RecentsItemUiModel? {
    if (entryId == null || this !is RecentsContentUiState.Entries) {
        return null
    }

    return items
        .filterIsInstance<RecentsListItemUiModel.Entry>()
        .firstOrNull { entry -> entry.item.entryId.value == entryId }
        ?.item
}

private fun deniedPhonePermissions(context: Context): Array<String> {
    return PermissionsUtil.getPermissionsCurrentlyDenied(
        context,
        PermissionsUtil.allPhoneGroupPermissionsUsedInDialer,
    )
}
