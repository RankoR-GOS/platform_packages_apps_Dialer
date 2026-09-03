package com.android.dialer.ui.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Consumes a screen model's one-shot [events] — a snackbar, an Intent, a navigation hop — while
 * the UI is at least [minActiveState].
 *
 * The lifecycle gate is the point. A plain `LaunchedEffect { events.collect(...) }` keeps
 * collecting after the screen stops, so an effect that fires while the app is backgrounded gets
 * handled against a UI nobody is looking at: an Activity launched from behind another app, a
 * dialog shown into a destroyed window. Collection here is cancelled on STOP and restarted on
 * START.
 *
 * The trade this makes: the screen models these events come from are `MutableSharedFlow` with no
 * replay, so anything emitted while the UI is stopped is dropped rather than queued. That is the
 * right default for one-shot UI effects — a stale snackbar replayed minutes later is worse than
 * no snackbar. State that must survive belongs in the ui state, not in an effect.
 *
 * [onEvent] does not need to be stable; the latest instance is always the one invoked.
 */
@Composable
internal fun <T> CollectEvents(
    events: Flow<T>,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onEvent: suspend (T) -> Unit,
) {
    val currentOnEvent by rememberUpdatedState(newValue = onEvent)
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(events, lifecycleOwner, minActiveState) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(state = minActiveState) {
            events.collect { event -> currentOnEvent(event) }
        }
    }
}
