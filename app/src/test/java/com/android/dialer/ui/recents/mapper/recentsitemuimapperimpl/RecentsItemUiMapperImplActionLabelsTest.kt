package com.android.dialer.ui.recents.mapper.recentsitemuimapperimpl

import android.os.Build
import android.provider.CallLog.Calls
import com.android.dialer.R
import com.android.dialer.testutil.callLogEntry
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsItemUiMapperImplActionLabelsTest : BaseRecentsItemUiMapperImplTest() {

    @Test
    fun map_labelsTheRowClickWithTheExpandMenuAction() {
        val model = map(callLogEntry(id = 1L))

        assertEquals(string(R.string.a11y_new_call_log_entry_tap_action), model.clickActionLabel)
    }

    @Test
    fun map_withACallableNumber_describesTheCallButtonWithThePrimaryText() {
        val model = map(callLogEntry(id = 1L, cachedName = "Ada"))

        assertEquals("text-${R.string.description_call_action} Ada", model.callActionLabel)
    }

    @Test
    fun map_withAVideoCall_describesTheVideoCallButton() {
        val model = map(callLogEntry(id = 1L, cachedName = "Ada", features = Calls.FEATURES_VIDEO))

        assertEquals("text-${R.string.description_video_call_action} Ada", model.callActionLabel)
    }

    @Test
    fun map_whenTheNumberCannotBeCalled_hasNoCallActionLabel() {
        every { canPlaceCall(any(), any()) } returns false

        val model = map(callLogEntry(id = 1L, features = Calls.FEATURES_VIDEO))

        assertNull(model.callActionLabel)
    }
}
