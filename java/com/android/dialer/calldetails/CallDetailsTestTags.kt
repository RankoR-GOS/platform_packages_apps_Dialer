package com.android.dialer.calldetails

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

internal const val CALL_DETAILS_SCREEN_TEST_TAG = "call_details_screen"
internal const val CALL_DETAILS_TOP_BAR_TEST_TAG = "call_details_top_bar"
internal const val CALL_DETAILS_NAV_BACK_TEST_TAG = "call_details_nav_back"

internal const val CALL_DETAILS_HEADER_TEST_TAG = "call_details_header"
internal const val CALL_DETAILS_AVATAR_TEST_TAG = "call_details_avatar"
internal const val CALL_DETAILS_DISPLAY_NAME_TEST_TAG = "call_details_display_name"
internal const val CALL_DETAILS_PHONE_NUMBER_TEST_TAG = "call_details_phone_number"
internal const val CALL_DETAILS_ACCOUNT_LABEL_TEST_TAG = "call_details_account_label"
internal const val CALL_DETAILS_VOICE_CALL_ACTION_TAG = "call_details_action_voice"
internal const val CALL_DETAILS_VIDEO_CALL_ACTION_TAG = "call_details_action_video"
internal const val CALL_DETAILS_SMS_ACTION_TAG = "call_details_action_sms"

internal const val CALL_DETAILS_ENTRIES_LIST_TEST_TAG = "call_details_entries_list"
internal const val CALL_DETAILS_ENTRY_ROW_PREFIX = "call_details_entry_"
internal const val CALL_DETAILS_ENTRY_TYPE_ICON_PREFIX = "call_details_entry_type_icon_"
internal const val CALL_DETAILS_ENTRY_TYPE_TEXT_PREFIX = "call_details_entry_type_text_"
internal const val CALL_DETAILS_ENTRY_DATE_TEXT_PREFIX = "call_details_entry_date_text_"
internal const val CALL_DETAILS_ENTRY_ACCOUNT_TEXT_PREFIX = "call_details_entry_account_text_"
internal const val CALL_DETAILS_ENTRY_DURATION_TEXT_PREFIX = "call_details_entry_duration_text_"

internal const val CALL_DETAILS_COPY_ACTION_TAG = "call_details_action_copy"
internal const val CALL_DETAILS_EDIT_NUMBER_ACTION_TAG = "call_details_action_edit_number"
internal const val CALL_DETAILS_DELETE_MENU_ITEM_TAG = "call_details_action_delete"

internal const val CALL_DETAILS_ACTION_COPY_TEST_TAG = CALL_DETAILS_COPY_ACTION_TAG
internal const val CALL_DETAILS_ACTION_EDIT_NUMBER_TEST_TAG = CALL_DETAILS_EDIT_NUMBER_ACTION_TAG
internal const val CALL_DETAILS_ACTION_DELETE_TEST_TAG = CALL_DETAILS_DELETE_MENU_ITEM_TAG

internal const val CALL_DETAILS_DELETE_DIALOG_TEST_TAG = "call_details_delete_dialog"
internal const val CALL_DETAILS_DELETE_CONFIRM_BUTTON_TAG = "call_details_delete_confirm_button"
internal const val CALL_DETAILS_DELETE_CANCEL_BUTTON_TAG = "call_details_delete_cancel_button"

internal const val CALL_DETAILS_LOADING_INDICATOR_TAG = "call_details_loading_indicator"
internal const val CALL_DETAILS_UNAVAILABLE_TEXT_TAG = "call_details_unavailable_text"

internal fun callDetailsEntryTag(callId: Long): String = "$CALL_DETAILS_ENTRY_ROW_PREFIX$callId"

internal fun callDetailsEntryTypeIconTag(callId: Long): String =
    "$CALL_DETAILS_ENTRY_TYPE_ICON_PREFIX$callId"

internal fun callDetailsEntryTypeTextTag(callId: Long): String =
    "$CALL_DETAILS_ENTRY_TYPE_TEXT_PREFIX$callId"

internal fun callDetailsEntryDateTextTag(callId: Long): String =
    "$CALL_DETAILS_ENTRY_DATE_TEXT_PREFIX$callId"

internal fun callDetailsEntryAccountTag(callId: Long): String =
    "$CALL_DETAILS_ENTRY_ACCOUNT_TEXT_PREFIX$callId"

internal fun callDetailsEntryDurationTextTag(callId: Long): String =
    "$CALL_DETAILS_ENTRY_DURATION_TEXT_PREFIX$callId"

internal fun Modifier.optionalTestTag(tag: String?): Modifier {
    return when {
        tag != null -> then(Modifier.testTag(tag = tag))
        else -> this
    }
}
