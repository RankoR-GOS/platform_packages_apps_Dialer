package com.android.dialer.ui.recents.screen

import android.content.Context
import android.os.Build
import com.android.dialer.callintent.CallIntentBuilder
import com.android.dialer.precall.PreCall
import com.android.dialer.ui.recents.model.RecentsEffect
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsVideoEffectTest {

    private val context = mockk<Context>()
    private val builder = slot<CallIntentBuilder>()
    private val handler = RecentsEffectHandlerImpl(context)

    @Before
    fun setUp() {
        mockkStatic(PreCall::class)
        every { PreCall.start(context, capture(builder)) } just runs
    }

    @After
    fun tearDown() {
        unmockkStatic(PreCall::class)
    }

    @Test
    fun handle_withARecordedVideoAccount_passesItToPreCall() {
        handler.handle(RecentsEffect.PlaceVideoCall("123", "example/.Service", "sim2"))

        assertTrue(builder.captured.isVideoCall)
        assertEquals("sim2", builder.captured.phoneAccountHandle?.id)
        assertEquals(
            "example/.Service",
            builder.captured.phoneAccountHandle?.componentName?.flattenToShortString(),
        )
        assertEquals("tel:123", builder.captured.uri.toString())
    }

    @Test
    fun handle_withAMalformedVideoAccount_leavesAccountSelectionToPreCall() {
        handler.handle(RecentsEffect.PlaceVideoCall("123", "not-a-component", "sim2"))

        assertTrue(builder.captured.isVideoCall)
        assertNull(builder.captured.phoneAccountHandle)
    }

    @Test
    fun handle_withAVoiceCallback_preservesExtensionsAndDoesNotPinAnAccount() {
        handler.handle(RecentsEffect.PlaceCall("123,45;67"))

        assertFalse(builder.captured.isVideoCall)
        assertEquals("123,45;67", builder.captured.uri.schemeSpecificPart)
        assertNull(builder.captured.phoneAccountHandle)
    }
}
