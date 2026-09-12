package com.android.dialer.data.recents.model

internal sealed interface RecentsWriteResult {

    data object Completed : RecentsWriteResult

    data class Failed(
        val cause: RecentsWriteFailure,
    ) : RecentsWriteResult
}

internal enum class RecentsWriteFailure {

    PermissionRevoked,
    Storage,
    ProviderAbsent,
}
