package com.android.dialer.ui.contacts.screen

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.ContactsContract.Contacts
import androidx.compose.ui.geometry.Rect
import com.android.dialer.domain.contacts.usecase.BuildContactLookupUriImpl
import com.android.dialer.ui.contacts.screen.model.ContactsScreenEffect as Effect
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
class ContactsEffectHandlerTest {

    private val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
    private val requestedPermissions = mutableListOf<ImmutableList<String>>()

    private val handler = ContactsEffectHandlerImpl(
        context = activity,
        buildContactLookupUri = BuildContactLookupUriImpl(),
        onRequestPermissions = { permissions -> requestedPermissions += permissions },
    )

    private fun nextStartedIntent(): Intent? = shadowOf(activity).nextStartedActivity

    @Test
    fun addContactLaunchesTheSystemInsertIntent() {
        handler.handle(Effect.LaunchAddContact)

        val intent = nextStartedIntent()

        assertNotNull(intent)
        assertEquals(Intent.ACTION_INSERT, intent?.action)
        assertEquals(Contacts.CONTENT_URI, intent?.data)
    }

    @Test
    fun contactCardOpensForTheTappedContact() {
        handler.handle(
            Effect.ShowContactCard(
                contactId = 42L,
                lookupKey = "key-42",
                anchorBounds = Rect(left = 0f, top = 10f, right = 100f, bottom = 60f),
            ),
        )

        val intent = nextStartedIntent()

        assertNotNull(intent)
        assertTrue(
            "expected a lookup uri for the tapped contact, got ${intent?.data}",
            intent?.data.toString().contains("key-42"),
        )
    }

    @Test
    fun permissionRequestsAreHandedToTheLauncher() {
        val permissions = persistentListOf(
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS",
        )

        handler.handle(Effect.RequestPermissions(permissions = permissions))

        assertEquals(listOf(permissions), requestedPermissions)
    }
}
