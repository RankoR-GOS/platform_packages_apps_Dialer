package com.android.dialer.data.recents.model

@JvmInline
internal value class CallLogEntryId(
    internal val value: Long,
) {
    companion object {
        fun fromOrNull(value: Long): CallLogEntryId? {
            return when {
                value > 0L -> CallLogEntryId(value = value)
                else -> null
            }
        }
    }
}
