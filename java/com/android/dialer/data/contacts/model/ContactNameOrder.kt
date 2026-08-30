package com.android.dialer.data.contacts.model

/**
 * Which of a contact's two name forms to use.
 *
 * [PRIMARY] is "Given Family",
 * [ALTERNATIVE] is "Family, Given".
 */
internal enum class ContactNameOrder {
    PRIMARY,
    ALTERNATIVE,
}
