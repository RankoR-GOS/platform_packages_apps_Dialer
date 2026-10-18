package com.android.dialer.settings.ui

internal const val SETTINGS_LIST_TEST_TAG = "settings_list"
internal const val SETTINGS_LOADING_TEST_TAG = "settings_loading"
internal const val SETTINGS_BACK_BUTTON_TEST_TAG = "settings_back_button"

internal const val SETTINGS_SORT_ORDER_ITEM_TEST_TAG = "settings_sort_order_item"
internal const val SETTINGS_DISPLAY_ORDER_ITEM_TEST_TAG = "settings_display_order_item"

internal const val SETTINGS_SOUND_AND_VIBRATION_ITEM_TEST_TAG = "settings_sound_and_vibration_item"
internal const val SETTINGS_QUICK_RESPONSES_ITEM_TEST_TAG = "settings_quick_responses_item"
internal const val SETTINGS_CALL_ACCOUNTS_ITEM_TEST_TAG = "settings_call_accounts_item"
internal const val SETTINGS_ACCESSIBILITY_ITEM_TEST_TAG = "settings_accessibility_item"

internal const val SETTINGS_CALL_RECORDING_V2_ITEM_TEST_TAG = "settings_call_recording_v2_item"
internal const val SETTINGS_OUTPUT_FORMAT_ITEM_TEST_TAG = "settings_output_format_item"
internal const val SETTINGS_AUDIO_SOURCE_ITEM_TEST_TAG = "settings_audio_source_item"
internal const val SETTINGS_RECORDING_NOTICE_ITEM_TEST_TAG = "settings_recording_notice_item"

internal const val SETTINGS_BUILD_VERSION_ITEM_TEST_TAG = "settings_build_version_item"
internal const val SETTINGS_LICENSES_ITEM_TEST_TAG = "settings_licenses_item"
internal const val SETTINGS_PRIVACY_POLICY_ITEM_TEST_TAG = "settings_privacy_policy_item"
internal const val SETTINGS_TERMS_OF_SERVICE_ITEM_TEST_TAG = "settings_terms_of_service_item"

internal const val SETTINGS_CHOICE_DIALOG_TEST_TAG = "settings_choice_dialog"
internal const val SETTINGS_CHOICE_DIALOG_CANCEL_BUTTON_TEST_TAG = "settings_choice_dialog_cancel"

internal fun settingsChoiceOptionTestTag(
    option: Enum<*>,
): String = "settings_choice_option_${option.name}"
