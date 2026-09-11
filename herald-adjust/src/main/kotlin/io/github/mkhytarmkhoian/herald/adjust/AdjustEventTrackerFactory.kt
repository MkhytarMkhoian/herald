package io.github.mkhytarmkhoian.herald.adjust

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution

/**
 * Turns an event into the Adjust calls it should produce — or declines it.
 *
 * [Resolution.Declined] passes the event to the next factory; [Resolution.Claimed] and
 * [Resolution.Dropped] both end the search, the first sending its handlers and the second sending
 * nothing.
 *
 * Adjust events need a token issued in its dashboard, so most events go unclaimed here.
 */
public fun interface AdjustEventTrackerFactory {
    public fun create(event: Event): Resolution<AdjustEventTracker>
}

/** Asks each factory in order and takes the first answer that is not a decline. */
public class CompositeAdjustEventTrackerFactory(
    private val factories: List<AdjustEventTrackerFactory>,
) : AdjustEventTrackerFactory {

    public constructor(vararg factories: AdjustEventTrackerFactory) : this(factories.toList())

    override fun create(event: Event): Resolution<AdjustEventTracker> {
        for (factory in factories) {
            val resolution = factory.create(event)
            if (resolution !is Resolution.Declined) return resolution
        }
        return Resolution.Declined
    }
}
