package io.github.mkhytarmkhoian.herald.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import kotlinx.coroutines.launch

/**
 * Tracks [event] every time the host lifecycle reaches [on].
 *
 * With the default, `ON_RESUME`, that is every time the screen becomes visible: when it is first
 * shown, when the user comes back to it, and when the app returns to the foreground with it on
 * top. This matches what Firebase's automatic screen tracking counts.
 *
 * To count a screen once per open instead, track from the ViewModel when it is created.
 *
 * Does nothing in previews and in the layout inspector.
 */
@Composable
public fun TrackScreenView(event: ScreenViewEvent, on: Lifecycle.Event = Lifecycle.Event.ON_RESUME) {
    TrackOnLifecycleEvent(event, on = on)
}

/**
 * Tracks [event] every time the host lifecycle reaches [on]. The default, `ON_RESUME`, is every
 * time the screen becomes visible; `ON_PAUSE` or `ON_STOP` is every time the user leaves it.
 * `ON_DESTROY` is not supported.
 *
 * Does nothing in previews and in the layout inspector.
 */
@Composable
public fun TrackOnLifecycleEvent(event: Event, on: Lifecycle.Event = Lifecycle.Event.ON_RESUME) {
    if (LocalInspectionMode.current) return
    val tracker = LocalEventTrackerService.current
    val scope = rememberCoroutineScope()
    LifecycleEventEffect(on) {
        scope.launch { tracker.track(event) }
    }
}

/**
 * A function that tracks an event, for click handlers and other callbacks in a composable that
 * has no ViewModel:
 *
 * ```kotlin
 * val track = rememberTracker()
 * Button(onClick = { track(PromoBannerClicked(id)) }) { ... }
 * ```
 *
 * Does nothing in previews and in the layout inspector.
 */
@Composable
public fun rememberTracker(): (Event) -> Unit {
    if (LocalInspectionMode.current) return remember { {} }
    val tracker = LocalEventTrackerService.current
    val scope = rememberCoroutineScope()
    return remember(tracker, scope) { { event -> scope.launch { tracker.track(event) } } }
}
