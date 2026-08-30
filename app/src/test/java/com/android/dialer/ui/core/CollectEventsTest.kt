package com.android.dialer.ui.core

import android.os.Build
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.android.dialer.testing.robolectricComposeActivityRule
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
class CollectEventsTest {

    private class TestLifecycleOwner : LifecycleOwner {
        val registry = LifecycleRegistry.createUnsafe(this)
        override val lifecycle: Lifecycle get() = registry
    }

    @get:Rule(order = 0)
    val activityRule = robolectricComposeActivityRule()

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private val owner = TestLifecycleOwner()
    private val events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private val received = mutableListOf<String>()

    private fun setContent(onEvent: suspend (String) -> Unit = { received += it }) {
        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                CollectEvents(events = events, onEvent = onEvent)
            }
        }
    }

    private fun moveTo(state: Lifecycle.State) {
        composeRule.runOnIdle { owner.registry.currentState = state }
        composeRule.waitForIdle()
    }

    private fun emit(value: String) {
        composeRule.runOnIdle { events.tryEmit(value) }
        composeRule.waitForIdle()
    }

    @Test
    fun deliversEventsWhileStarted() {
        setContent()
        moveTo(Lifecycle.State.STARTED)

        emit("first")
        emit("second")

        assertEquals(listOf("first", "second"), received)
    }

    @Test
    fun dropsEventsEmittedBeforeStarted() {
        setContent()
        moveTo(Lifecycle.State.CREATED)

        emit("ignored")

        assertEquals(emptyList<String>(), received)
    }

    @Test
    fun dropsEventsEmittedAfterStopAndResumesAfterRestart() {
        setContent()
        moveTo(Lifecycle.State.STARTED)
        emit("while started")

        moveTo(Lifecycle.State.CREATED)
        emit("while stopped")

        moveTo(Lifecycle.State.STARTED)
        emit("after restart")

        assertEquals(listOf("while started", "after restart"), received)
    }

    @Test
    fun invokesTheLatestOnEventLambdaWithoutRestartingCollection() {
        val target = mutableStateOf(1)
        val firstTarget = mutableListOf<String>()
        val secondTarget = mutableListOf<String>()

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                val sink = if (target.value == 1) firstTarget else secondTarget
                CollectEvents(events = events) { sink += it }
            }
        }
        moveTo(Lifecycle.State.STARTED)

        emit("to first")
        composeRule.runOnIdle { target.value = 2 }
        composeRule.waitForIdle()
        emit("to second")

        assertEquals(listOf("to first"), firstTarget)
        assertEquals(listOf("to second"), secondTarget)
    }
}
