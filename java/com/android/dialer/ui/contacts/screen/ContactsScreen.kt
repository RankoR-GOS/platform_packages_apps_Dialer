package com.android.dialer.ui.contacts.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.dialer.R
import com.android.dialer.domain.contacts.usecase.BuildContactLookupUri
import com.android.dialer.ui.contacts.component.ContactsEmptyState
import com.android.dialer.ui.contacts.component.ContactsList
import com.android.dialer.ui.contacts.screen.model.ContactsAction as Action
import com.android.dialer.ui.contacts.screen.model.ContactsUiState as State
import com.android.dialer.ui.core.CollectEvents
import com.android.dialer.util.PermissionsUtil

@Composable
internal fun ContactsRoute(
    screenModel: ContactsScreenModel,
    buildContactLookupUri: BuildContactLookupUri,
    showsAddContactRow: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        results.filterValues { isGranted -> isGranted }.keys.forEach { permission ->
            PermissionsUtil.notifyPermissionGranted(context, permission)
        }
    }

    ContactsScreen(
        screenModel = screenModel,
        effectHandler = rememberContactsEffectHandler(
            buildContactLookupUri = buildContactLookupUri,
            onRequestPermissions = { permissions ->
                permissionLauncher.launch(permissions.toTypedArray())
            },
        ),
        showsAddContactRow = showsAddContactRow,
        modifier = modifier,
    )
}

@Composable
internal fun ContactsScreen(
    screenModel: ContactsScreenModel,
    effectHandler: ContactsEffectHandler,
    showsAddContactRow: Boolean,
    modifier: Modifier = Modifier,
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()

    CollectEvents(events = screenModel.effects, onEvent = effectHandler::handle)

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        screenModel.onAction(Action.ScreenResumed)
    }

    ContactsContent(
        uiState = uiState,
        showsAddContactRow = showsAddContactRow,
        onAction = screenModel::onAction,
        modifier = modifier,
    )
}

@Composable
private fun ContactsContent(
    uiState: State,
    showsAddContactRow: Boolean,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        when (uiState) {
            State.Loading -> Unit

            State.PermissionRequired -> ContactsEmptyState(
                message = stringResource(id = R.string.permission_no_contacts),
                actionLabel = stringResource(id = R.string.permission_single_turn_on),
                onAction = { onAction(Action.GrantPermissionClicked) },
                icon = Icons.Outlined.Lock,
            )

            State.Empty -> ContactsEmptyState(
                message = stringResource(id = R.string.all_contacts_empty),
                actionLabel = stringResource(
                    id = R.string.all_contacts_empty_add_contact_action,
                ),
                onAction = { onAction(Action.AddContactClicked) },
                icon = Icons.Outlined.Contacts,
            )

            is State.Loaded -> ContactsList(
                rows = uiState.rows,
                showsAddContactRow = showsAddContactRow,
                onContactClick = { contactId, bounds ->
                    onAction(Action.ContactClicked(contactId = contactId, anchorBounds = bounds))
                },
                onAddContactClick = { onAction(Action.AddContactClicked) },
            )
        }
    }
}
