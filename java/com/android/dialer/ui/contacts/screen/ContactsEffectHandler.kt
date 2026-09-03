package com.android.dialer.ui.contacts.screen

import android.content.Context
import android.graphics.Rect
import android.provider.ContactsContract.QuickContact
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.compose.ui.platform.LocalContext
import com.android.dialer.R
import com.android.dialer.domain.contacts.usecase.BuildContactLookupUri
import com.android.dialer.ui.contacts.screen.model.ContactsScreenEffect as Effect
import com.android.dialer.util.DialerUtils
import com.android.dialer.util.IntentUtil
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList

internal interface ContactsEffectHandler {
    fun handle(effect: Effect)
}

@Composable
internal fun rememberContactsEffectHandler(
    buildContactLookupUri: BuildContactLookupUri,
    onRequestPermissions: (ImmutableList<String>) -> Unit,
): ContactsEffectHandler {
    val context = LocalContext.current
    val currentOnRequestPermissions by rememberUpdatedState(newValue = onRequestPermissions)

    return remember(context, buildContactLookupUri) {
        ContactsEffectHandlerImpl(
            context = context,
            buildContactLookupUri = buildContactLookupUri,
            onRequestPermissions = { permissions -> currentOnRequestPermissions(permissions) },
        )
    }
}

internal class ContactsEffectHandlerImpl(
    private val context: Context,
    private val buildContactLookupUri: BuildContactLookupUri,
    private val onRequestPermissions: (ImmutableList<String>) -> Unit,
) : ContactsEffectHandler {

    override fun handle(effect: Effect) {
        when (effect) {
            is Effect.ShowContactCard -> showContactCard(effect = effect)

            Effect.LaunchAddContact -> DialerUtils.startActivityWithErrorToast(
                context,
                IntentUtil.getNewContactIntent(),
                R.string.add_contact_not_available,
            )

            is Effect.RequestPermissions -> onRequestPermissions(effect.permissions)
        }
    }

    private fun showContactCard(effect: Effect.ShowContactCard) {
        QuickContact.showQuickContact(
            context,
            effect.anchorBounds.toAndroidRect(),
            buildContactLookupUri(contactId = effect.contactId, lookupKey = effect.lookupKey),
            QuickContact.MODE_LARGE,
            null,
        )
    }
}

private fun ComposeRect.toAndroidRect(): Rect =
    Rect(
        left.roundToInt(),
        top.roundToInt(),
        right.roundToInt(),
        bottom.roundToInt(),
    )
