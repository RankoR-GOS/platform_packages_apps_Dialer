package com.android.dialer.ui.recents.screen

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import com.android.dialer.ui.recents.model.RecentsEffect
import com.android.dialer.util.CallUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

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
    fun createContact_startsTheLegacyInsertIntentWithThatNumber() {
        handler.handle(effect = RecentsEffect.CreateContact(number = NUMBER))

        val intent = shadowOf(activity).nextStartedActivity
        assertEquals(Intent.ACTION_INSERT, intent.action)
        assertEquals(ContactsContract.Contacts.CONTENT_URI, intent.data)
        assertEquals(NUMBER, intent.getStringExtra(ContactsContract.Intents.Insert.PHONE))
    }

    @Test
    fun editNumberBeforeCall_startsTheDialIntentWithTheLegacyCallUri() {
        handler.handle(effect = RecentsEffect.EditNumberBeforeCall(number = NUMBER))

        val intent = shadowOf(activity).nextStartedActivity
        assertEquals(Intent.ACTION_DIAL, intent.action)
        assertEquals(CallUtil.getCallUri(NUMBER), intent.data)
    }

    @Test
    fun copyNumber_setsASensitiveClipAndLeavesTheConfirmationToTheSystem() {
        handler.handle(effect = RecentsEffect.CopyNumber(number = NUMBER))

        val clip = activity.getSystemService(ClipboardManager::class.java).primaryClip
        val extras = clip?.description?.extras
        assertEquals(NUMBER, clip?.getItemAt(0)?.text?.toString())
        assertTrue(extras?.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE) == true)
        assertNull(ShadowToast.getLatestToast())
    }

    private companion object {
        const val NUMBER = "+15550001"
    }
}
