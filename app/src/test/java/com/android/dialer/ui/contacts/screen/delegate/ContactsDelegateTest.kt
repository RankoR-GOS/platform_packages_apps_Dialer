package com.android.dialer.ui.contacts.screen.delegate

import com.android.dialer.data.contacts.model.Contact
import com.android.dialer.data.contacts.model.ContactsIndex
import com.android.dialer.data.contacts.model.ContactsQuery
import com.android.dialer.data.contacts.model.ContactsSnapshot
import com.android.dialer.data.contacts.repository.ContactsRepository
import com.android.dialer.domain.contacts.usecase.IsReadContactsPermissionGranted
import com.android.dialer.domain.contacts.usecase.ObserveContactsPermissionGrants
import com.android.dialer.ui.contacts.screen.mapper.ContactsUiStateMapperImpl
import com.android.dialer.ui.contacts.screen.model.ContactRowUiModel
import com.android.dialer.ui.contacts.screen.model.ContactsUiState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsDelegateTest {

    private class FakeContactsRepository : ContactsRepository {
        val queries = mutableListOf<ContactsQuery>()
        val snapshots = MutableSharedFlow<ContactsSnapshot>(replay = 1)

        val subscriptionCount: Int get() = queries.size

        override fun observeContacts(query: ContactsQuery): Flow<ContactsSnapshot> = flow {
            queries += query
            emitAll(snapshots)
        }
    }

    private class FakePermission : IsReadContactsPermissionGranted {
        var granted = true

        override fun invoke(): Boolean = granted
    }

    private class FakeGrants : ObserveContactsPermissionGrants {
        val signals = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

        override fun invoke(): Flow<Unit> = signals
    }

    private val repository = FakeContactsRepository()
    private val permission = FakePermission()
    private val grants = FakeGrants()

    private fun TestScope.newDelegate() = ContactsDelegateImpl(
        repository = repository,
        mapper = ContactsUiStateMapperImpl(),
        isReadContactsPermissionGranted = permission,
        observeContactsPermissionGrants = grants,
        defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
    )

    private fun snapshotOf(vararg names: String) = ContactsSnapshot(
        contacts = names.mapIndexed { i, name ->
            Contact(
                id = i.toLong(),
                lookupKey = "lookup-$i",
                displayName = name,
                photoId = 0L,
                photoUri = null,
            )
        }.let { contacts -> persistentListOf(*contacts.toTypedArray()) },
        index = ContactsIndex.EMPTY,
    )

    private fun TestScope.boundDelegate(): ContactsDelegateImpl {
        val delegate = newDelegate()
        delegate.bind(scope = backgroundScope)
        runCurrent()

        return delegate
    }

    private fun ContactsDelegate.names(): List<String> =
        (state.value as ContactsUiState.Loaded).rows.map(ContactRowUiModel::displayName)

    @Test
    fun startsInLoadingBeforeAnythingIsRead() = runTest {
        assertEquals(ContactsUiState.Loading, newDelegate().state.value)
    }

    @Test
    fun bindingTwiceDoesNotStackCollectors() = runTest {
        repository.snapshots.tryEmit(snapshotOf("Ada"))

        val delegate = boundDelegate()
        delegate.bind(scope = backgroundScope)
        runCurrent()

        assertEquals(1, repository.subscriptionCount)
    }

    @Test
    fun reportsPermissionRequiredWithoutContactsAccess() = runTest {
        permission.granted = false

        val delegate = boundDelegate()

        assertEquals(ContactsUiState.PermissionRequired, delegate.state.value)
        assertEquals(0, repository.subscriptionCount)
    }

    @Test
    fun reportsEmptyWhenTheProviderHasNothing() = runTest {
        repository.snapshots.tryEmit(ContactsSnapshot.EMPTY)

        val delegate = boundDelegate()

        assertEquals(ContactsUiState.Empty, delegate.state.value)
    }

    @Test
    fun reportsLoadedContacts() = runTest {
        repository.snapshots.tryEmit(snapshotOf("Ada", "Grace"))

        val delegate = boundDelegate()

        assertEquals(listOf("Ada", "Grace"), delegate.names())
    }

    @Test
    fun followsLaterProviderEmissions() = runTest {
        repository.snapshots.tryEmit(snapshotOf("Ada"))
        val delegate = boundDelegate()

        repository.snapshots.tryEmit(snapshotOf("Ada", "Grace"))
        runCurrent()

        assertEquals(listOf("Ada", "Grace"), delegate.names())
    }

    // Grant contacts access from the call log, come back, and the list is already populated.
    @Test
    fun recoversWhenAnotherScreenAnnouncesTheGrant() = runTest {
        permission.granted = false
        repository.snapshots.tryEmit(snapshotOf("Ada"))
        val delegate = boundDelegate()
        assertEquals(ContactsUiState.PermissionRequired, delegate.state.value)

        permission.granted = true
        grants.signals.tryEmit(Unit)
        runCurrent()

        assertEquals(listOf("Ada"), delegate.names())
    }

    // Granting from system settings fires no in-app broadcast; only the resume-driven refresh
    // notices.
    @Test
    fun recoversOnRefreshWhenPermissionWasGrantedOutsideTheApp() = runTest {
        permission.granted = false
        repository.snapshots.tryEmit(snapshotOf("Ada"))
        val delegate = boundDelegate()
        assertEquals(ContactsUiState.PermissionRequired, delegate.state.value)

        permission.granted = true
        delegate.refresh()
        runCurrent()

        assertEquals(listOf("Ada"), delegate.names())
    }

    @Test
    fun refreshRebuildsTheSubscription() = runTest {
        repository.snapshots.tryEmit(snapshotOf("Ada"))
        val delegate = boundDelegate()
        assertEquals(1, repository.subscriptionCount)

        delegate.refresh()
        runCurrent()

        assertEquals(2, repository.subscriptionCount)
    }

    @Test
    fun refreshDoesNotFallBackToLoading() = runTest {
        repository.snapshots.tryEmit(snapshotOf("Ada"))
        val delegate = boundDelegate()

        delegate.refresh()
        runCurrent()

        assertEquals(listOf("Ada"), delegate.names())
    }

    @Test
    fun filterChangeQueriesTheProviderAgainWithTheNewFilter() = runTest {
        repository.snapshots.tryEmit(snapshotOf("Ada"))
        val delegate = boundDelegate()

        delegate.updateFilter("ada")
        runCurrent()

        assertEquals(
            listOf(ContactsQuery(filter = ""), ContactsQuery(filter = "ada")),
            repository.queries,
        )
    }

    @Test
    fun repeatingTheSameFilterDoesNotRequery() = runTest {
        repository.snapshots.tryEmit(snapshotOf("Ada"))
        val delegate = boundDelegate()

        delegate.updateFilter("ada")
        runCurrent()
        delegate.updateFilter("ada")
        runCurrent()

        assertEquals(2, repository.subscriptionCount)
    }
}
