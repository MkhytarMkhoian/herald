package io.github.mkhytarmkhoian.herald.mixpanel.trackers

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.mixpanel.MixpanelEventTracker
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.mixpanel.toMixpanelProperties

// Top-level and private, not a companion: a `const` in a companion is a public static field even
// when the companion itself is private, so it would surface in the checked-in API dump.
private const val SCREEN_VIEW = "screen_view"
private const val SCREEN_NAME = "screen_name"

public class ScreenViewEventTracker(
    private val event: ScreenViewEvent,
    private val mixpanel: MixpanelAPI,
) : MixpanelEventTracker {

    override fun track() {
        val params = event.parameters.toMixpanelProperties() + (SCREEN_NAME to event.screenName)
        mixpanel.trackMap(SCREEN_VIEW, params)
    }
}
