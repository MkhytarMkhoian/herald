package io.github.mkhytarmkhoian.herald.firebase

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.requireFallbackLast

/**
 * Turns an event into the Firebase calls it should produce — or declines it.
 *
 * [Resolution.Declined] passes the event to the next link in a
 * [CompositeFirebaseEventTrackerFactory]; [Resolution.Claimed] and [Resolution.Dropped] both claim
 * it and end the search, the second meaning "mine, and it goes nowhere".
 *
 * A factory only answers for the events it knows, so a feature module can ship one for its own
 * events and decline the rest.
 */
public fun interface FirebaseEventTrackerFactory {
    public fun create(event: Event): Resolution<FirebaseEventTracker>
}

/**
 * Asks each factory in order and takes the first answer that is not a decline.
 *
 * Order decides the configuration: put your own factory first to override how a marker is handled,
 * and end with [GenericFirebaseEventTrackerFactory] if every event should reach Firebase somehow.
 * Leave that terminator out and an unclaimed event is simply not tracked.
 */
public class CompositeFirebaseEventTrackerFactory(
    private val factories: List<FirebaseEventTrackerFactory>,
) : FirebaseEventTrackerFactory {

    init {
        requireFallbackLast(factories)
    }

    public constructor(vararg factories: FirebaseEventTrackerFactory) : this(factories.toList())

    override fun create(event: Event): Resolution<FirebaseEventTracker> {
        for (factory in factories) {
            val resolution = factory.create(event)
            if (resolution !is Resolution.Declined) return resolution
        }
        return Resolution.Declined
    }
}
