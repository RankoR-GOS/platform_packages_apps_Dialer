package com.android.dialer.ui.contacts.screen

import androidx.compose.ui.geometry.Rect
import app.cash.turbine.test
import com.android.dialer.domain.contacts.usecase.GetDeniedContactsPermissions
import com.android.dialer.ui.contacts.screen.delegate.ContactsDelegate
import com.android.dialer.ui.contacts.screen.model.ContactRowUiModel
import com.android.dialer.ui.contacts.screen.model.ContactsAction as Action
import com.android.dialer.ui.contacts.screen.model.ContactsScreenEffect as Effect
import com.android.dialer.ui.contacts.screen.model.ContactsUiState as State
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsViewModelTest {

    private class FakeDelegate : ContactsDelegate {
        val mutableState = MutableStateFlow<State>(State.Loading)
        override val state: StateFlow<State> = mutableState.asStateFlow()

        var bindCount = 0
        var refreshCount = 0
        val filters = mutableListOf<String>()

        override fun bind(scope: CoroutineScope) {
            bindCount++
        }

        override fun refresh() {
            refreshCount++
        }

        override fun updateFilter(filter: String) {
            filters += filter
        }
    }

    private class FakeDeniedPermissions : GetDeniedContactsPermissions {
        var denied: ImmutableList<String> = persistentListOf("android.permission.READ_CONTACTS")

        override fun invoke(): ImmutableList<String> = denied
    }

    private val delegate = FakeDelegate()
    private val deniedPermissions = FakeDeniedPermissions()

    private fun viewModel() = ContactsViewModel(
        delegate = delegate,
        getDeniedContactsPermissions = deniedPermissions,
    )

    private fun row(id: Long, lookupKey: String = "lookup-$id") = ContactRowUiModel(
        id = id,
        lookupKey = lookupKey,
        displayName = "Ada",
        photoId = 0L,
        photoUri = null,
        sectionLabel = "A",
        isSectionStart = true,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun bindsTheDelegateOnce() {
        viewModel()

        assertEquals(1, delegate.bindCount)
    }

    @Test
    fun exposesTheDelegateState() {
        val model = viewModel()
        delegate.mutableState.value = State.Loaded(rows = persistentListOf(row(id = 1L)))

        assertEquals(delegate.state.value, model.uiState.value)
    }

    @Test
    fun resumeRefreshesTheDelegate() {
        val model = viewModel()

        model.onAction(Action.ScreenResumed)

        assertEquals(1, delegate.refreshCount)
    }

    @Test
    fun filterChangeReachesTheDelegate() {
        val model = viewModel()

        model.onAction(Action.FilterChanged(filter = "ada"))

        assertEquals(listOf("ada"), delegate.filters)
    }

    @Test
    fun addContactEmitsTheLaunchEffect() = runTest {
        val model = viewModel()

        model.effects.test {
            model.onAction(Action.AddContactClicked)

            assertEquals(Effect.LaunchAddContact, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun contactTapEmitsTheCardEffectWithTheRowsLookupKey() = runTest {
        val model = viewModel()
        delegate.mutableState.value = State.Loaded(
            rows = persistentListOf(row(id = 1L), row(id = 2L, lookupKey = "key-2")),
        )
        val bounds = Rect(left = 0f, top = 10f, right = 100f, bottom = 60f)

        model.effects.test {
            model.onAction(Action.ContactClicked(contactId = 2L, anchorBounds = bounds))

            assertEquals(
                Effect.ShowContactCard(contactId = 2L, lookupKey = "key-2", anchorBounds = bounds),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun contactTapOnAnUnknownRowIsIgnored() = runTest {
        val model = viewModel()
        delegate.mutableState.value = State.Loaded(rows = persistentListOf(row(id = 1L)))

        model.effects.test {
            model.onAction(Action.ContactClicked(contactId = 99L, anchorBounds = Rect.Zero))

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun contactTapBeforeAnythingIsLoadedIsIgnored() = runTest {
        val model = viewModel()

        model.effects.test {
            model.onAction(Action.ContactClicked(contactId = 1L, anchorBounds = Rect.Zero))

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun turnOnRequestsEveryStillMissingPermission() = runTest {
        val model = viewModel()
        deniedPermissions.denied = persistentListOf(
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS",
        )

        model.effects.test {
            model.onAction(Action.GrantPermissionClicked)

            assertEquals(
                Effect.RequestPermissions(permissions = deniedPermissions.denied),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun turnOnWithNothingLeftToAskRefreshesInsteadOfAskingAgain() = runTest {
        val model = viewModel()
        deniedPermissions.denied = persistentListOf()

        model.effects.test {
            model.onAction(Action.GrantPermissionClicked)

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(1, delegate.refreshCount)
    }
}
