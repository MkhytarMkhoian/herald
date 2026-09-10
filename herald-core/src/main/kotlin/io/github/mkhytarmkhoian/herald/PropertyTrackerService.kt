package io.github.mkhytarmkhoian.herald

/**
 * Somewhere a [Property] can be set.
 *
 * The [EventTrackerService] counterpart, implemented by adapters, by the fan-out and by
 * decorators.
 */
public fun interface PropertyTrackerService {
    public suspend fun set(property: Property)
}
