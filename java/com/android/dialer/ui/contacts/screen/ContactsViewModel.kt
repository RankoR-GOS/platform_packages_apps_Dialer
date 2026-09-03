package com.android.dialer.ui.contacts.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.dialer.domain.contacts.usecase.GetDeniedContactsPermissions
import com.android.dialer.ui.contacts.screen.delegate.ContactsDelegate
import com.android.dialer.ui.contacts.screen.model.ContactRowUiModel
import com.android.dialer.ui.contacts.screen.model.ContactsAction as Action
import com.android.dialer.ui.contacts.screen.model.ContactsScreenEffect as Effect
import com.android.dialer.ui.contacts.screen.model.ContactsUiState as State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow

internal interface ContactsScreenModel {
    val uiState: StateFlow<State>
    val effects: Flow<Effect>

    fun onAction(action: Action)
}

@HiltViewModel
internal class ContactsViewModel @Inject constructor(
    private val delegate: ContactsDelegate,
    private val getDeniedContactsPermissions: GetDeniedContactsPermissions,
) : ViewModel(),
    ContactsScreenModel {

    private val mutableEffects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    override val effects: Flow<Effect> = mutableEffects.asSharedFlow()

    override val uiState: StateFlow<State> = delegate.state

    init {
        delegate.bind(viewModelScope)
    }

    override fun onAction(action: Action) {
        when (action) {
            is Action.ContactClicked -> showContactCard(action)
            Action.AddContactClicked -> emitEffect(Effect.LaunchAddContact)
            Action.GrantPermissionClicked -> requestContactsPermissions()
            Action.ScreenResumed -> delegate.refresh()
            is Action.FilterChanged -> delegate.updateFilter(action.filter)
        }
    }

    private fun showContactCard(action: Action.ContactClicked) {
        val row = rowFor(contactId = action.contactId) ?: return

        emitEffect(
            Effect.ShowContactCard(
                contactId = row.id,
                lookupKey = row.lookupKey,
                anchorBounds = action.anchorBounds,
            ),
        )
    }

    private fun requestContactsPermissions() {
        val denied = getDeniedContactsPermissions()

        when {
            denied.isEmpty() -> delegate.refresh()
            else -> emitEffect(Effect.RequestPermissions(permissions = denied))
        }
    }

    private fun rowFor(contactId: Long): ContactRowUiModel? =
        (uiState.value as? State.Loaded)?.rows?.firstOrNull { row -> row.id == contactId }

    private fun emitEffect(effect: Effect) {
        mutableEffects.tryEmit(effect)
    }
}
