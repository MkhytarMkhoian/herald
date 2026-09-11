package io.github.mkhytarmkhoian.herald.adjust

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.EventTrackerService
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.PropertyTrackerService

public class AdjustAnalyticsTrackerService(
    private val eventTrackerFactory: AdjustEventTrackerFactory,
    private val propertySetterFactory: AdjustPropertySetterFactory,
) : EventTrackerService, PropertyTrackerService {

    override suspend fun track(event: Event) {
        eventTrackerFactory.create(event).handlers.forEach { it.track() }
    }

    override suspend fun set(property: Property) {
        propertySetterFactory.create(property).handlers.forEach { it.set() }
    }
}
