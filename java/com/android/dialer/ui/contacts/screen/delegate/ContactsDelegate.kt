package com.android.dialer.ui.contacts.screen.delegate

import com.android.dialer.data.contacts.model.ContactsQuery
import com.android.dialer.data.contacts.repository.ContactsRepository
import com.android.dialer.di.core.DefaultDispatcher
import com.android.dialer.domain.contacts.usecase.IsReadContactsPermissionGranted
import com.android.dialer.domain.contacts.usecase.ObserveContactsPermissionGrants
import com.android.dialer.ui.contacts.screen.mapper.ContactsUiStateMapper
import com.android.dialer.ui.contacts.screen.model.ContactsUiState
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

/**
 * Owns the contacts screen's state.
 *
 * The view model routes actions; this is what actually holds a `StateFlow` and mutates it, so the
 * state machine can be tested as plain Kotlin without a `ViewModel` lifecycle.
 */
internal interface ContactsDelegate {

    val state: StateFlow<ContactsUiState>

    fun bind(scope: CoroutineScope)

    fun refresh()

    fun updateFilter(filter: String)
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class ContactsDelegateImpl @Inject constructor(
    private val repository: ContactsRepository,
    private val mapper: ContactsUiStateMapper,
    private val isReadContactsPermissionGranted: IsReadContactsPermissionGranted,
    private val observeContactsPermissionGrants: ObserveContactsPermissionGrants,
    @param:DefaultDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
) : ContactsDelegate {

    private val mutableState = MutableStateFlow<ContactsUiState>(ContactsUiState.Loading)
    override val state: StateFlow<ContactsUiState> = mutableState.asStateFlow()

    private val filter = MutableStateFlow("")
    private val refreshRequests = MutableStateFlow(0)

    private var isBound = false

    override fun bind(scope: CoroutineScope) {
        if (isBound) return
        isBound = true

        scope.launch(defaultDispatcher) {
            combine(filter, reloadSignals()) { currentFilter, _ -> currentFilter }
                .flatMapLatest { currentFilter -> contactsState(filter = currentFilter) }
                .collect { newState -> mutableState.value = newState }
        }
    }

    override fun refresh() {
        refreshRequests.value += 1
    }

    override fun updateFilter(filter: String) {
        this.filter.value = filter
    }

    private fun contactsState(filter: String): Flow<ContactsUiState> =
        when {
            !isReadContactsPermissionGranted() -> flowOf(ContactsUiState.PermissionRequired)

            else ->
                repository
                    .observeContacts(query = ContactsQuery(filter = filter))
                    .map { snapshot -> mapper.map(snapshot = snapshot) }
        }

    private fun reloadSignals(): Flow<Any> =
        merge(refreshRequests, observeContactsPermissionGrants())
}
