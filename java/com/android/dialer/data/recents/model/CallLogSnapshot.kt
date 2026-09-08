package com.android.dialer.data.recents.model

import kotlinx.collections.immutable.ImmutableList

internal data class CallLogSnapshot(
    val entries: ImmutableList<CallLogEntry>,
    val isPermissionGranted: Boolean,
)
