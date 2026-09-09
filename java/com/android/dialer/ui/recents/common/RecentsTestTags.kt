package com.android.dialer.ui.recents.common

import com.android.dialer.data.recents.model.CallLogEntryId

internal const val RECENTS_SCREEN_TEST_TAG = "recents_screen"
internal const val RECENTS_SNACKBAR_TEST_TAG = "recents_snackbar"
internal const val RECENTS_EMPTY_STATE_TEST_TAG = "recents_empty_state"
internal const val RECENTS_PERMISSION_STATE_TEST_TAG = "recents_permission_state"
internal const val RECENTS_PERMISSION_ACTION_TEST_TAG = "recents_permission_action"
internal const val RECENTS_LIST_TEST_TAG = "recents_list"
internal const val RECENTS_DAY_HEADER_TEST_TAG = "recents_day_header"
internal const val RECENTS_SHEET_TEST_TAG = "recents_sheet"
internal const val RECENTS_SHEET_CONTENT_TEST_TAG = "recents_sheet_content"
internal const val RECENTS_SHEET_TITLE_TEST_TAG = "recents_sheet_title"
internal const val RECENTS_SHEET_SUBTITLE_TEST_TAG = "recents_sheet_subtitle"
internal const val RECENTS_SHEET_AVATAR_TEST_TAG = "recents_sheet_avatar"
internal const val RECENTS_SHEET_CALL_TEST_TAG = "recents_sheet_call"
internal const val RECENTS_SHEET_VIDEO_CALL_TEST_TAG = "recents_sheet_video_call"
internal const val RECENTS_SHEET_MESSAGE_TEST_TAG = "recents_sheet_message"
internal const val RECENTS_SHEET_CREATE_CONTACT_TEST_TAG = "recents_sheet_create_contact"
internal const val RECENTS_SHEET_ADD_CONTACT_TEST_TAG = "recents_sheet_add_contact"
internal const val RECENTS_SHEET_EDIT_NUMBER_TEST_TAG = "recents_sheet_edit_number"
internal const val RECENTS_SHEET_BLOCK_TEST_TAG = "recents_sheet_block"
internal const val RECENTS_SHEET_COPY_NUMBER_TEST_TAG = "recents_sheet_copy_number"
internal const val RECENTS_SHEET_CALL_DETAILS_TEST_TAG = "recents_sheet_call_details"
internal const val RECENTS_SHEET_DELETE_TEST_TAG = "recents_sheet_delete"

internal fun recentsItemTestTag(entryId: CallLogEntryId): String {
    return "recents_item_${entryId.value}"
}

internal fun recentsItemCallButtonTestTag(entryId: CallLogEntryId): String {
    return "recents_item_call_button_${entryId.value}"
}

internal fun recentsItemAvatarTestTag(entryId: CallLogEntryId): String {
    return "recents_item_avatar_${entryId.value}"
}
