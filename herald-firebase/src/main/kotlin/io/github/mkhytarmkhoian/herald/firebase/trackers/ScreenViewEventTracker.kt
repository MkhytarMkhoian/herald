package io.github.mkhytarmkhoian.herald.firebase.trackers

import com.google.firebase.analytics.FirebaseAnalytics
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.firebase.FirebaseEventTracker
import io.github.mkhytarmkhoian.herald.firebase.toBundle

/**
 * Logs [event] as GA4's `screen_view`, with its name as `screen_name`.
 *
 * Throws an [IllegalArgumentException] if the event has its own `screen_name` parameter, because it
 * would replace the screen's name.
 */
public class ScreenViewEventTracker(
    private val event: ScreenViewEvent,
    private val firebaseAnalytics: FirebaseAnalytics,
) : FirebaseEventTracker {

    override suspend fun track() {
        require(FirebaseAnalytics.Param.SCREEN_NAME !in event.parameters) {
            "Screen view '${event.name}' can't have a 'screen_name' parameter: " +
                "GA4 uses it for the screen's name"
        }
        val bundle = event.parameters.toBundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, event.name)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}
