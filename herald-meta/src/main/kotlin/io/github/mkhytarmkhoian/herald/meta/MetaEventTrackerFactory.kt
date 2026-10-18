package io.github.mkhytarmkhoian.herald.meta

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.requireFallbackLast

/**
 * Turns an event into the Meta calls it should produce, or declines it.
 *
 * A conversion usually goes to Meta under one of its standard names, from `AppEventsConstants`.
 * Give it a tracker that makes the call, and a factory that claims the event by its type:
 *
 * ```kotlin
 * class SignUpEventTracker(
 *     private val event: SignUpFinished,
 *     private val logger: AppEventsLogger,
 * ) : MetaEventTracker {
 *     override suspend fun track() {
 *         logger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, event.parameters.toMetaParameters())
 *     }
 * }
 * ```
 */
public fun interface MetaEventTrackerFactory {
    public fun create(event: Event): Resolution<MetaEventTracker>
}

/** Asks each factory in order and takes the first answer that is not a decline. */
public class CompositeMetaEventTrackerFactory(
    private val factories: List<MetaEventTrackerFactory>,
) : MetaEventTrackerFactory {

    init {
        requireFallbackLast(factories)
    }

    public constructor(vararg factories: MetaEventTrackerFactory) : this(factories.toList())

    override fun create(event: Event): Resolution<MetaEventTracker> {
        for (factory in factories) {
            val resolution = factory.create(event)
            if (resolution !is Resolution.Declined) return resolution
        }
        return Resolution.Declined
    }
}
