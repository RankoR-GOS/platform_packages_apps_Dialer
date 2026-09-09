package com.android.dialer.calldetails.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.dialer.calldetails.CallDetailsEntries
import com.android.dialer.calldetails.data.CallDetailsRepository
import com.android.dialer.calldetails.model.CallDetailsData
import com.android.dialer.calldetails.model.CallDetailsDeleteDialogState
import com.android.dialer.calldetails.model.CallDetailsUiState
import com.android.dialer.common.LogUtil
import com.android.dialer.dialercontact.DialerContact
import com.android.dialer.inject.IoDispatcher
import com.google.protobuf.InvalidProtocolBufferException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
internal class CallDetailsViewModel @Inject constructor(
    private val repository: CallDetailsRepository,
    private val actionHandler: CallDetailsActionHandler,
    private val uiMapper: CallDetailsUiMapper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel(),
    CallDetailsScreenModel {

    private val _uiState = MutableStateFlow<CallDetailsUiState>(CallDetailsUiState.Loading)
    override val uiState: StateFlow<CallDetailsUiState> = _uiState.asStateFlow()

    private var currentCallData: CallDetailsData? = null

    init {
        loadCallDetails()
    }

    fun loadCallDetails() {
        viewModelScope.launch {
            _uiState.value = CallDetailsUiState.Loading

            val data = withContext(ioDispatcher) {
                loadDataFromSource()
            }
            currentCallData = data
            _uiState.value = if (data.entries.isEmpty()) {
                CallDetailsUiState.Unavailable
            } else {
                uiMapper.mapToUiState(data)
            }
        }
    }

    private suspend fun loadDataFromSource(): CallDetailsData {
        val contactProto = parseContactProto()
        val entriesProto = parseEntriesProto()
        val canReportCallerId =
            savedStateHandle.get<Boolean>(EXTRA_CAN_REPORT_CALLER_ID) ?: false
        val canSupportAssistedDialing =
            savedStateHandle.get<Boolean>(EXTRA_CAN_SUPPORT_ASSISTED_DIALING) ?: false
        val phoneNumberExtra =
            savedStateHandle.get<String>(EXTRA_PHONE_NUMBER).orEmpty()

        if (contactProto != null || (entriesProto != null && entriesProto.entriesCount > 0)) {
            return repository.createFromProto(
                contact = contactProto,
                entries = entriesProto,
                fallbackNumber = phoneNumberExtra,
                canReportCallerId = canReportCallerId,
                canSupportAssistedDialing = canSupportAssistedDialing,
            )
        }

        val ids = savedStateHandle.get<LongArray>(EXTRA_CALL_LOG_IDS)
            ?.toList()
            .orEmpty()

        return repository.getCallDetails(
            callLogIds = ids.distinct(),
            fallbackNumber = phoneNumberExtra,
        )
    }

    private fun parseContactProto(): DialerContact? {
        val bytes = savedStateHandle.get<ByteArray>(EXTRA_CONTACT) ?: return null
        return try {
            DialerContact.parseFrom(bytes)
        } catch (e: InvalidProtocolBufferException) {
            LogUtil.e(TAG, "Error parsing contact proto", e)
            null
        }
    }

    private fun parseEntriesProto(): CallDetailsEntries? {
        val bytes = savedStateHandle.get<ByteArray>(EXTRA_CALL_DETAILS_ENTRIES) ?: return null
        return try {
            CallDetailsEntries.parseFrom(bytes)
        } catch (e: InvalidProtocolBufferException) {
            LogUtil.e(TAG, "Error parsing call details entries proto", e)
            null
        }
    }

    override fun onPlaceVoiceCall(
        phoneNumber: String,
        postDialDigits: String,
    ) {
        actionHandler.placeVoiceCall(phoneNumber, postDialDigits)
    }

    override fun onPlaceVideoCall(
        phoneNumber: String,
    ) {
        actionHandler.placeVideoCall(phoneNumber)
    }

    override fun onSendSms(
        phoneNumber: String,
    ) {
        actionHandler.sendSms(phoneNumber)
    }

    override fun onCopyNumber(
        phoneNumber: String,
    ) {
        actionHandler.copyNumber(phoneNumber)
    }

    override fun onEditNumber(
        phoneNumber: String,
    ) {
        actionHandler.editNumber(phoneNumber)
    }

    override fun onDeleteCall(
        callId: Long,
    ) {
        _uiState.update { current ->
            if (current is CallDetailsUiState.Content) {
                current.copy(
                    deleteDialogState = CallDetailsDeleteDialogState.DeleteEntry(callId),
                )
            } else {
                current
            }
        }
    }

    override fun onDeleteAllEntries() {
        _uiState.update { current ->
            if (current is CallDetailsUiState.Content) {
                current.copy(
                    deleteDialogState = CallDetailsDeleteDialogState.DeleteAll,
                )
            } else {
                current
            }
        }
    }

    override fun onConfirmDelete(
        state: CallDetailsDeleteDialogState,
    ) {
        viewModelScope.launch {
            when (state) {
                is CallDetailsDeleteDialogState.DeleteEntry -> {
                    repository.deleteCalls(listOf(state.callId))
                    _uiState.update { current ->
                        if (current is CallDetailsUiState.Content) {
                            val remaining = current.entries.filter { it.callId != state.callId }
                            if (remaining.isEmpty()) {
                                CallDetailsUiState.Unavailable
                            } else {
                                current.copy(
                                    entries = remaining.toImmutableList(),
                                    deleteDialogState = null,
                                )
                            }
                        } else {
                            current
                        }
                    }
                }
                is CallDetailsDeleteDialogState.DeleteAll -> {
                    val allIds = currentCallData?.entries?.map { it.callId }.orEmpty()
                    repository.deleteCalls(allIds)
                    _uiState.value = CallDetailsUiState.Unavailable
                }
            }
        }
    }

    override fun onDismissDeleteDialog() {
        _uiState.update { current ->
            if (current is CallDetailsUiState.Content) {
                current.copy(deleteDialogState = null)
            } else {
                current
            }
        }
    }

    override fun onOpenContact() {
        val current = _uiState.value as? CallDetailsUiState.Content ?: return
        val contactUri = current.header.contactUri ?: return
        actionHandler.openContact(contactUri)
    }

    internal companion object {
        private const val TAG = "CallDetailsViewModel"
        const val EXTRA_CALL_DETAILS_ENTRIES = "call_details_entries"
        const val EXTRA_CONTACT = "contact"
        const val EXTRA_PHONE_NUMBER = "phone_number"
        const val EXTRA_CALL_LOG_IDS = "call_log_ids"
        const val EXTRA_CAN_REPORT_CALLER_ID = "can_report_caller_id"
        const val EXTRA_CAN_SUPPORT_ASSISTED_DIALING = "can_support_assisted_dialing"
    }
}
