package com.android.dialer.ui.recents.screen

import android.os.Build
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.android.dialer.testutil.RobolectricComposeActivityRule
import com.android.dialer.ui.recents.model.RecentsEffect as Effect
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsEffectsTest {

    @get:Rule(order = 0)
    val componentActivityRule = RobolectricComposeActivityRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private val effects = Channel<Effect>(capacity = Channel.BUFFERED)
    private val effectHandler = mockk<RecentsEffectHandler>(relaxed = true)
    private var permissionRequests = 0
    private var dialpadRequests = 0
    private var writeFailures = 0

    @Test
    fun requestCallLogPermission_isInterceptedByTheScreen() {
        setContent()

        effects.trySend(Effect.RequestCallLogPermission)

        composeTestRule.runOnIdle {
            assertEquals(1, permissionRequests)
            verify(exactly = 0) { effectHandler.handle(any()) }
        }
    }

    @Test
    fun showDialpad_isInterceptedByTheScreen() {
        setContent()

        effects.trySend(Effect.ShowDialpad)

        composeTestRule.runOnIdle {
            assertEquals(1, dialpadRequests)
            verify(exactly = 0) { effectHandler.handle(any()) }
        }
    }

    @Test
    fun writeFailed_isInterceptedByTheScreen() {
        setContent()

        effects.trySend(Effect.WriteFailed)

        composeTestRule.runOnIdle {
            assertEquals(1, writeFailures)
            verify(exactly = 0) { effectHandler.handle(any()) }
        }
    }

    @Test
    fun placeCall_fallsThroughToTheHandler() {
        setContent()

        effects.trySend(Effect.PlaceCall(number = NUMBER))

        composeTestRule.runOnIdle {
            verify(exactly = 1) { effectHandler.handle(Effect.PlaceCall(number = NUMBER)) }
            assertEquals(0, permissionRequests + writeFailures)
        }
    }

    private fun setContent() {
        composeTestRule.setContent {
            RecentsEffects(
                effects = effects.receiveAsFlow(),
                effectHandler = effectHandler,
                onRequestPermission = { permissionRequests++ },
                onShowDialpad = { dialpadRequests++ },
                onWriteFailure = { writeFailures++ },
            )
        }
    }

    private companion object {
        const val NUMBER = "+15550001"
    }
}
