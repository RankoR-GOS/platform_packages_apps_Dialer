package com.android.dialer.ui.contacts.common

internal const val CONTACTS_LIST_TEST_TAG = "contacts_list"
internal const val CONTACTS_EMPTY_STATE_TEST_TAG = "contacts_empty_state"
internal const val CONTACTS_EMPTY_STATE_ACTION_TEST_TAG = "contacts_empty_state_action"
internal const val CONTACTS_ADD_CONTACT_ROW_TEST_TAG = "contacts_add_contact_row"
internal const val CONTACTS_PINNED_SECTION_TEST_TAG = "contacts_pinned_section"
internal const val CONTACTS_FAST_SCROLLER_TEST_TAG = "contacts_fast_scroller"
internal const val CONTACTS_FAST_SCROLLER_LABEL_TEST_TAG = "contacts_fast_scroller_label"

internal fun contactRowTestTag(contactId: Long): String = "contact_row_$contactId"

internal fun contactAvatarTestTag(contactId: Long): String = "contact_avatar_$contactId"

internal fun contactSectionHeaderTestTag(label: String): String =
    "contact_section_header_$label"
