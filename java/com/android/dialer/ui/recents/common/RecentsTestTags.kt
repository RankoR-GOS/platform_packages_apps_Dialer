package com.android.dialer.ui.recents.common

import com.android.dialer.data.recents.model.CallLogEntryId

internal const val RECENTS_LIST_TEST_TAG = "recents_list"
internal const val RECENTS_DAY_HEADER_TEST_TAG = "recents_day_header"

internal fun recentsItemTestTag(entryId: CallLogEntryId): String {
    return "recents_item_${entryId.value}"
}

internal fun recentsItemCallButtonTestTag(entryId: CallLogEntryId): String {
    return "recents_item_call_button_${entryId.value}"
}

internal fun recentsItemAvatarTestTag(entryId: CallLogEntryId): String {
    return "recents_item_avatar_${entryId.value}"
}
