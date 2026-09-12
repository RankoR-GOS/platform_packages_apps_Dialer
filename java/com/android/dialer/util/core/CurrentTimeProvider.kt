package com.android.dialer.util.core

internal fun interface CurrentTimeProvider {
    fun currentTimeMillis(): Long
}
