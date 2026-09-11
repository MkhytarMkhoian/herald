package io.github.mkhytarmkhoian.herald.log

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution

/**
 * Turns an event into the log lines it should produce — or declines it.
 *
 * [Resolution.Declined] passes the event to the next factory; [Resolution.Claimed] and
 * [Resolution.Dropped] both end the search, the first sending its handlers and the second sending
 * nothing.
 */
public fun interface LogEventTrackerFactory {
    public fun create(event: Event): Resolution<LogEventTracker>
}

/** Asks each factory in order and takes the first answer that is not a decline. */
public class CompositeLogEventTrackerFactory(
    private val factories: List<LogEventTrackerFactory>,
) : LogEventTrackerFactory {

    public constructor(vararg factories: LogEventTrackerFactory) : this(factories.toList())

    override fun create(event: Event): Resolution<LogEventTracker> {
        for (factory in factories) {
            val resolution = factory.create(event)
            if (resolution !is Resolution.Declined) return resolution
        }
        return Resolution.Declined
    }
}
