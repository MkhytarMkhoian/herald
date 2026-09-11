package io.github.mkhytarmkhoian.herald.mixpanel

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.mixpanel.trackers.ScreenViewEventTracker

/**
 * Claims every [ScreenViewEvent] and declines everything else, which is
 * [GenericMixpanelEventTrackerFactory]'s business.
 *
 * A screen view does not go through the generic path because it is sent under one shared
 * `screen_view` name with the screen as a `screen_name` property, so all screens land in a single
 * Mixpanel event that can be broken down by screen.
 *
 * To handle screen views differently, write a factory and place it before this one in the chain.
 */
public class ScreenViewMixpanelEventTrackerFactory(
    private val mixpanel: MixpanelAPI,
) : MixpanelEventTrackerFactory {

    override fun create(event: Event): Resolution<MixpanelEventTracker> = when (event) {
        is ScreenViewEvent -> Resolution.Claimed(ScreenViewEventTracker(event, mixpanel))
        else -> Resolution.Declined
    }
}
