package com.android.dialer.ui.recents.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class RecentsUiState(
    val content: RecentsContentUiState = RecentsContentUiState.Loading,
    val actionLabels: RecentsActionLabelsUiModel = RecentsActionLabelsUiModel(),
    val writeFailedMessage: String = "",
)

@Immutable
internal sealed interface RecentsContentUiState {

    @Immutable
    data object Loading : RecentsContentUiState

    @Immutable
    data class PermissionRequired(
        val message: String,
        val actionLabel: String,
    ) : RecentsContentUiState

    @Immutable
    data class Empty(
        val message: String,
    ) : RecentsContentUiState

    @Immutable
    data class Entries(
        val items: ImmutableList<RecentsListItemUiModel>,
    ) : RecentsContentUiState
}

@Immutable
internal sealed interface RecentsListItemUiModel {

    @Immutable
    data class DayHeader(
        val key: String,
        val label: String,
    ) : RecentsListItemUiModel

    @Immutable
    data class Entry(
        val item: RecentsItemUiModel,
    ) : RecentsListItemUiModel
}
