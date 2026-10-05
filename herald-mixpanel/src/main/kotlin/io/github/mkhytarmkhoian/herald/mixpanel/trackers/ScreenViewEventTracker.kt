package io.github.mkhytarmkhoian.herald.mixpanel.trackers

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.mixpanel.MixpanelEventTracker
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.mixpanel.toMixpanelProperties

// Top-level and private, not a companion: a `const` in a companion is a public static field even
// when the companion itself is private, so it would surface in the checked-in API dump.
private const val SCREEN_VIEW = "screen_view"
private const val SCREEN_NAME = "screen_name"

/**
 * Tracks [event] as a `screen_view` event, with its name as `screen_name`: the names GA4 uses, since
 * Mixpanel has none of its own.
 *
 * Throws an [IllegalArgumentException] if the event has its own `screen_name` parameter, because it
 * would replace the screen's name.
 */
public class ScreenViewEventTracker(
    private val event: ScreenViewEvent,
    private val mixpanel: MixpanelAPI,
) : MixpanelEventTracker {

    override suspend fun track() {
        require(SCREEN_NAME !in event.parameters) {
            "Screen view '${event.name}' can't have a '$SCREEN_NAME' parameter: " +
                "it holds the screen's name"
        }
        val params = event.parameters.toMixpanelProperties() + (SCREEN_NAME to event.name)
        mixpanel.trackMap(SCREEN_VIEW, params)
    }
}
