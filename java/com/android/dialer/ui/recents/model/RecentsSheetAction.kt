package com.android.dialer.ui.recents.model

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface RecentsSheetAction {

    @Immutable
    data object Call : RecentsSheetAction

    @Immutable
    data object VideoCall : RecentsSheetAction

    @Immutable
    data object Message : RecentsSheetAction

    @Immutable
    data object CreateContact : RecentsSheetAction

    @Immutable
    data object AddContact : RecentsSheetAction

    @Immutable
    data object CopyNumber : RecentsSheetAction

    @Immutable
    data object EditNumberBeforeCall : RecentsSheetAction

    @Immutable
    data object Delete : RecentsSheetAction
}
