package com.android.dialer.domain.contacts.usecase

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal interface ObserveContactsPermissionGrants {
    operator fun invoke(): Flow<Unit>
}

internal class ObserveContactsPermissionGrantsImpl @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
) : ObserveContactsPermissionGrants {

    override fun invoke(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(Unit)
            }
        }
        val broadcastManager = LocalBroadcastManager.getInstance(context)

        broadcastManager.registerReceiver(
            receiver,
            IntentFilter(Manifest.permission.READ_CONTACTS),
        )

        awaitClose { broadcastManager.unregisterReceiver(receiver) }
    }
}
