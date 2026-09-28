package io.github.mkhytarmkhoian.herald.amplitude

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.EventTrackerService
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.PropertyTrackerService

public class AmplitudeAnalyticsTrackerService(
    private val eventTrackerFactory: AmplitudeEventTrackerFactory,
    private val propertySetterFactory: AmplitudePropertySetterFactory,
) : EventTrackerService, PropertyTrackerService {

    // Unclaimed events and properties are ignored. End the chain with RequireMappedAmplitudeEventTrackerFactory
    // to make them throw instead.

    override suspend fun track(event: Event) {
        eventTrackerFactory.create(event).handlers.forEach { it.track() }
    }

    override suspend fun set(property: Property) {
        propertySetterFactory.create(property).handlers.forEach { it.set() }
    }
}
