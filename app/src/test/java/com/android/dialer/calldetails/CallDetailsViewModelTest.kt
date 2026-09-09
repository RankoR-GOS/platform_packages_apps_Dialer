package com.android.dialer.calldetails

import android.os.Build
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.dialer.calldetails.data.CallDetailsRepository
import com.android.dialer.calldetails.model.CallDetailsData
import com.android.dialer.calldetails.model.CallDetailsDeleteDialogState
import com.android.dialer.calldetails.model.CallDetailsEntryData
import com.android.dialer.calldetails.model.CallDetailsHeaderData
import com.android.dialer.calldetails.model.CallDetailsUiState
import com.android.dialer.calldetails.ui.CallDetailsActionHandler
import com.android.dialer.calldetails.ui.CallDetailsUiMapper
import com.android.dialer.calldetails.ui.CallDetailsViewModel
import com.android.dialer.dialercontact.DialerContact
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
class CallDetailsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: CallDetailsRepository = mockk(relaxed = true)
    private val actionHandler: CallDetailsActionHandler = mockk(relaxed = true)
    private lateinit var uiMapper: CallDetailsUiMapper

    private val sampleData = CallDetailsData(
        header = CallDetailsHeaderData(
            primaryText = "Alice Smith",
            secondaryText = "+15550100",
            number = "+15550100",
        ),
        entries = listOf(
            CallDetailsEntryData(
                callId = 101L,
                callType = 1,
                timestamp = 1700000000000L,
                durationSeconds = 60L,
            ),
            CallDetailsEntryData(
                callId = 102L,
                callType = 2,
                timestamp = 1700001000000L,
                durationSeconds = 120L,
            ),
        ),
        canReportCallerId = true,
        canSupportAssistedDialing = false,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        uiMapper = CallDetailsUiMapper(RuntimeEnvironment.getApplication(), testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadCallDetails_fromCallLogIds_emitsContentState() = runTest {
        coEvery { repository.getCallDetails(any(), any()) } returns sampleData

        val savedStateHandle = SavedStateHandle(
            mapOf(
                CallDetailsViewModel.EXTRA_PHONE_NUMBER to "+15550100",
                CallDetailsViewModel.EXTRA_CALL_LOG_IDS to longArrayOf(101L, 102L),
            ),
        )

        val viewModel = CallDetailsViewModel(
            repository = repository,
            actionHandler = actionHandler,
            uiMapper = uiMapper,
            ioDispatcher = testDispatcher,
            savedStateHandle = savedStateHandle,
        )

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is CallDetailsUiState.Content)
            val content = state as CallDetailsUiState.Content
            assertEquals("Alice Smith", content.header.primaryText)
            assertEquals("+15550100", content.header.secondaryText)
            assertEquals(2, content.entries.size)
        }
    }

    @Test
    fun loadCallDetails_fromProtoInSavedState_constructsFromProtoDirectly() = runTest {
        val contactProto = DialerContact.newBuilder()
            .setNameOrNumber("Bob Jones")
            .setNumber("+15559999")
            .build()
        val entriesProto = CallDetailsEntries.newBuilder()
            .addEntries(
                CallDetailsEntries.CallDetailsEntry.newBuilder()
                    .setCallId(301L)
                    .setCallType(1)
                    .setDate(1700000000000L)
                    .setDuration(45L)
                    .build(),
            )
            .build()

        every {
            repository.createFromProto(any(), any(), any(), any(), any())
        } returns CallDetailsData(
            header = CallDetailsHeaderData(
                primaryText = "Bob Jones",
                secondaryText = "+15559999",
                number = "+15559999",
            ),
            entries = listOf(
                CallDetailsEntryData(
                    callId = 301L,
                    callType = 1,
                    timestamp = 1700000000000L,
                    durationSeconds = 45L,
                ),
            ),
        )

        val savedStateHandle = SavedStateHandle(
            mapOf(
                CallDetailsViewModel.EXTRA_CONTACT to contactProto.toByteArray(),
                CallDetailsViewModel.EXTRA_CALL_DETAILS_ENTRIES to entriesProto.toByteArray(),
            ),
        )

        val viewModel = CallDetailsViewModel(
            repository = repository,
            actionHandler = actionHandler,
            uiMapper = uiMapper,
            ioDispatcher = testDispatcher,
            savedStateHandle = savedStateHandle,
        )

        verify(exactly = 1) {
            repository.createFromProto(any(), any(), any(), any(), any())
        }

        val content = viewModel.uiState.value as CallDetailsUiState.Content
        assertEquals("Bob Jones", content.header.primaryText)
        assertEquals(1, content.entries.size)
        assertEquals(301L, content.entries[0].callId)
    }

    @Test
    fun userActions_delegateToActionHandler() {
        val viewModel = CallDetailsViewModel(
            repository = repository,
            actionHandler = actionHandler,
            uiMapper = uiMapper,
            ioDispatcher = testDispatcher,
            savedStateHandle = SavedStateHandle(),
        )

        viewModel.onPlaceVoiceCall("+15550100", "")
        verify(exactly = 1) { actionHandler.placeVoiceCall("+15550100", "") }

        viewModel.onPlaceVideoCall("+15550100")
        verify(exactly = 1) { actionHandler.placeVideoCall("+15550100") }

        viewModel.onSendSms("+15550100")
        verify(exactly = 1) { actionHandler.sendSms("+15550100") }

        viewModel.onCopyNumber("+15550100")
        verify(exactly = 1) { actionHandler.copyNumber("+15550100") }

        viewModel.onEditNumber("+15550100")
        verify(exactly = 1) { actionHandler.editNumber("+15550100") }
    }

    @Test
    fun deleteSingleEntry_removesEntryFromState() = runTest {
        coEvery { repository.getCallDetails(any(), any()) } returns sampleData

        val viewModel = CallDetailsViewModel(
            repository = repository,
            actionHandler = actionHandler,
            uiMapper = uiMapper,
            ioDispatcher = testDispatcher,
            savedStateHandle = SavedStateHandle(),
        )

        viewModel.onDeleteCall(101L)
        val withDialog = viewModel.uiState.value as CallDetailsUiState.Content
        assertEquals(CallDetailsDeleteDialogState.DeleteEntry(101L), withDialog.deleteDialogState)

        viewModel.onConfirmDelete(CallDetailsDeleteDialogState.DeleteEntry(101L))
        coVerify { repository.deleteCalls(listOf(101L)) }

        val afterDelete = viewModel.uiState.value as CallDetailsUiState.Content
        assertEquals(1, afterDelete.entries.size)
        assertEquals(102L, afterDelete.entries[0].callId)
        assertNull(afterDelete.deleteDialogState)
    }

    @Test
    fun deleteAllEntries_setsUnavailable() = runTest {
        coEvery { repository.getCallDetails(any(), any()) } returns sampleData

        val viewModel = CallDetailsViewModel(
            repository = repository,
            actionHandler = actionHandler,
            uiMapper = uiMapper,
            ioDispatcher = testDispatcher,
            savedStateHandle = SavedStateHandle(),
        )

        viewModel.onDeleteAllEntries()
        val withDialog = viewModel.uiState.value as CallDetailsUiState.Content
        assertEquals(CallDetailsDeleteDialogState.DeleteAll, withDialog.deleteDialogState)

        viewModel.onConfirmDelete(CallDetailsDeleteDialogState.DeleteAll)
        coVerify { repository.deleteCalls(listOf(101L, 102L)) }
        assertEquals(CallDetailsUiState.Unavailable, viewModel.uiState.value)
    }

    @Test
    fun dismissDeleteDialog_clearsDialogState() = runTest {
        coEvery { repository.getCallDetails(any(), any()) } returns sampleData

        val viewModel = CallDetailsViewModel(
            repository = repository,
            actionHandler = actionHandler,
            uiMapper = uiMapper,
            ioDispatcher = testDispatcher,
            savedStateHandle = SavedStateHandle(),
        )

        viewModel.onDeleteAllEntries()
        val withDialog = viewModel.uiState.value as CallDetailsUiState.Content
        assertEquals(CallDetailsDeleteDialogState.DeleteAll, withDialog.deleteDialogState)

        viewModel.onDismissDeleteDialog()
        val afterDismiss = viewModel.uiState.value as CallDetailsUiState.Content
        assertNull(afterDismiss.deleteDialogState)
    }

    @Test
    fun placeVoiceCall_forwardsPhoneNumberAndPostDialDigitsToActionHandler() = runTest {
        coEvery { repository.getCallDetails(any(), any()) } returns sampleData

        val viewModel = CallDetailsViewModel(
            repository = repository,
            actionHandler = actionHandler,
            uiMapper = uiMapper,
            ioDispatcher = testDispatcher,
            savedStateHandle = SavedStateHandle(),
        )

        viewModel.onPlaceVoiceCall("+15550100", ";1234")
        verify(exactly = 1) { actionHandler.placeVoiceCall("+15550100", ";1234") }
    }

    @Test
    fun loadCallDetails_emptyEntries_emitsUnavailableState() = runTest {
        coEvery { repository.getCallDetails(any(), any()) } returns CallDetailsData(
            header = CallDetailsHeaderData(
                primaryText = "+15550100",
                number = "+15550100",
            ),
            entries = emptyList(),
        )

        val viewModel = CallDetailsViewModel(
            repository = repository,
            actionHandler = actionHandler,
            uiMapper = uiMapper,
            ioDispatcher = testDispatcher,
            savedStateHandle = SavedStateHandle(),
        )

        assertEquals(CallDetailsUiState.Unavailable, viewModel.uiState.value)
    }

    @Test
    fun onOpenContact_withContactUri_triggersActionHandler() = runTest {
        val contactUri = "content://com.android.contacts/contacts/lookup/123/456"
        val dataWithContact = sampleData.copy(
            header = sampleData.header.copy(contactUri = contactUri),
        )
        coEvery { repository.getCallDetails(any(), any()) } returns dataWithContact

        val savedStateHandle = SavedStateHandle(
            mapOf(
                CallDetailsViewModel.EXTRA_PHONE_NUMBER to "+15550100",
                CallDetailsViewModel.EXTRA_CALL_LOG_IDS to longArrayOf(101L),
            ),
        )

        val viewModel = CallDetailsViewModel(
            repository = repository,
            actionHandler = actionHandler,
            uiMapper = uiMapper,
            ioDispatcher = testDispatcher,
            savedStateHandle = savedStateHandle,
        )

        viewModel.onOpenContact()

        verify(exactly = 1) { actionHandler.openContact(contactUri) }
    }

    @Test
    fun onOpenContact_withoutContactUri_doesNotTriggerActionHandler() = runTest {
        coEvery { repository.getCallDetails(any(), any()) } returns sampleData

        val savedStateHandle = SavedStateHandle(
            mapOf(
                CallDetailsViewModel.EXTRA_PHONE_NUMBER to "+15550100",
                CallDetailsViewModel.EXTRA_CALL_LOG_IDS to longArrayOf(101L),
            ),
        )

        val viewModel = CallDetailsViewModel(
            repository = repository,
            actionHandler = actionHandler,
            uiMapper = uiMapper,
            ioDispatcher = testDispatcher,
            savedStateHandle = savedStateHandle,
        )

        viewModel.onOpenContact()

        verify(exactly = 0) { actionHandler.openContact(any()) }
    }

    @Test
    fun mapToUiState_withEntryAccountLabel_mapsToUiModel() = runTest {
        val dataWithAccount = sampleData.copy(
            entries = listOf(
                CallDetailsEntryData(
                    callId = 101L,
                    callType = 1,
                    timestamp = 1700000000000L,
                    durationSeconds = 60L,
                    accountLabel = "SIM 1",
                ),
            ),
        )
        val uiState = uiMapper.mapToUiState(dataWithAccount)
        assertEquals("SIM 1", uiState.entries[0].accountLabel)
    }
}
