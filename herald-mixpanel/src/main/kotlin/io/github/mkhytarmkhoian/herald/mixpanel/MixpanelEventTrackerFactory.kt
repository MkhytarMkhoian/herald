package io.github.mkhytarmkhoian.herald.mixpanel

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution

/**
 * Turns an event into the Mixpanel calls it should produce — or declines it.
 *
 * [Resolution.Declined] passes the event to the next factory; [Resolution.Claimed] and
 * [Resolution.Dropped] both end the search, the first sending its handlers and the second sending
 * nothing.
 */
public fun interface MixpanelEventTrackerFactory {
    public fun create(event: Event): Resolution<MixpanelEventTracker>
}

/** Asks each factory in order and takes the first answer that is not a decline. */
public class CompositeMixpanelEventTrackerFactory(
    private val factories: List<MixpanelEventTrackerFactory>,
) : MixpanelEventTrackerFactory {

    public constructor(vararg factories: MixpanelEventTrackerFactory) : this(factories.toList())

    override fun create(event: Event): Resolution<MixpanelEventTracker> {
        for (factory in factories) {
            val resolution = factory.create(event)
            if (resolution !is Resolution.Declined) return resolution
        }
        return Resolution.Declined
    }
}
