package io.github.mkhytarmkhoian.herald.meta

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.EventTrackerService

/**
 * Events only: Meta keeps no attributes of a user or a session, so register this provider without
 * `properties`.
 */
public class MetaAnalyticsTrackerService(
    private val eventTrackerFactory: MetaEventTrackerFactory,
) : EventTrackerService {

    override suspend fun track(event: Event) {
        eventTrackerFactory.create(event).handlers.forEach { it.track() }
    }
}
