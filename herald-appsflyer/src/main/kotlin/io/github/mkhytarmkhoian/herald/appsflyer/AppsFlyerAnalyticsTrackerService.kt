package io.github.mkhytarmkhoian.herald.appsflyer

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.EventTrackerService

/**
 * Events only. AppsFlyer keeps no attributes of a user or a session, so there is nothing for a
 * `Property` to be set on; register this provider without `properties`.
 */
public class AppsFlyerAnalyticsTrackerService(
    private val eventTrackerFactory: AppsFlyerEventTrackerFactory,
) : EventTrackerService {

    // Unclaimed events are ignored. End the chain with RequireMappedAppsFlyerEventTrackerFactory to
    // make them throw instead.

    override suspend fun track(event: Event) {
        eventTrackerFactory.create(event).handlers.forEach { it.track() }
    }
}
