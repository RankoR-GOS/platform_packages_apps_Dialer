package com.android.dialer.ui.recents.screen

import android.content.Intent
import android.os.Build
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import com.android.dialer.ui.recents.model.RecentsEffect
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsEffectHandlerImplTest {

    private val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
    private val handler = RecentsEffectHandlerImpl(context = activity)

    @Test
    fun sendMessage_startsTheLegacySmsIntentForThatNumber() {
        handler.handle(effect = RecentsEffect.SendMessage(number = NUMBER))

        val intent = shadowOf(activity).nextStartedActivity
        assertEquals(Intent.ACTION_SENDTO, intent.action)
        assertEquals("sms:$NUMBER", intent.dataString)
    }

    @Test
    fun addContact_startsTheLegacyInsertOrEditIntentWithThatNumber() {
        handler.handle(effect = RecentsEffect.AddContact(number = NUMBER))

        val intent = shadowOf(activity).nextStartedActivity
        assertEquals(Intent.ACTION_INSERT_OR_EDIT, intent.action)
        assertEquals(ContactsContract.Contacts.CONTENT_ITEM_TYPE, intent.type)
        assertEquals(NUMBER, intent.getStringExtra(ContactsContract.Intents.Insert.PHONE))
    }

    private companion object {
        const val NUMBER = "+15550001"
    }
}
