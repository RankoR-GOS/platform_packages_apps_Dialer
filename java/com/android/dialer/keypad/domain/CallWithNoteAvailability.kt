package com.android.dialer.keypad.domain

import android.content.Context
import com.android.dialer.di.core.IoDispatcher
import com.android.dialer.util.CallUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal interface CallWithNoteAvailability {
    suspend fun isAvailable(): Boolean
}

internal class CallWithNoteAvailabilityImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CallWithNoteAvailability {

    override suspend fun isAvailable(): Boolean {
        return withContext(ioDispatcher) {
            CallUtil.isCallWithSubjectSupported(context)
        }
    }
}
