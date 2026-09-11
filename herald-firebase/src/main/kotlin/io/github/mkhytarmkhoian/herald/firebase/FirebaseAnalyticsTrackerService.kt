package io.github.mkhytarmkhoian.herald.firebase

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.EventTrackerService
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.PropertyTrackerService

public class FirebaseAnalyticsTrackerService(
    private val eventTrackerFactory: FirebaseEventTrackerFactory,
    private val propertySetterFactory: FirebasePropertySetterFactory,
) : EventTrackerService, PropertyTrackerService {

    // Unclaimed events and properties are ignored. End the chain with RequireMappedFirebaseEventTrackerFactory
    // to make them throw instead.

    override suspend fun track(event: Event) {
        eventTrackerFactory.create(event).handlers.forEach { it.track() }
    }

    override suspend fun set(property: Property) {
        propertySetterFactory.create(property).handlers.forEach { it.set() }
    }
}
