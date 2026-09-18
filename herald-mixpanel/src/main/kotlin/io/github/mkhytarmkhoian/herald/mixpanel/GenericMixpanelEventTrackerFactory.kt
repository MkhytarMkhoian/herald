package io.github.mkhytarmkhoian.herald.mixpanel

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.FallbackFactory
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.mixpanel.trackers.GenericEventTracker

/**
 * Tracks any event under its own name with its parameters attached. Claims everything, so it only
 * belongs last in a chain — anything after it is unreachable.
 */
public class GenericMixpanelEventTrackerFactory(
    private val mixpanel: MixpanelAPI,
) : MixpanelEventTrackerFactory, FallbackFactory {

    override fun create(event: Event): Resolution<MixpanelEventTracker> =
        Resolution.Claimed(GenericEventTracker(event, mixpanel))
}
