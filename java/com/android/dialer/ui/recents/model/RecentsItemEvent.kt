package com.android.dialer.ui.recents.model

import androidx.compose.runtime.Immutable
import com.android.dialer.data.recents.model.CallLogEntryId

@Immutable
internal sealed interface RecentsItemEvent {

    @Immutable
    data class Clicked(
        val entryId: CallLogEntryId,
    ) : RecentsItemEvent

    @Immutable
    data class CallClicked(
        val number: String,
    ) : RecentsItemEvent

    @Immutable
    data class VideoCallClicked(
        val number: String,
    ) : RecentsItemEvent
}
