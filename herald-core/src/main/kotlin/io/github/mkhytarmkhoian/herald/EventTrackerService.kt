package io.github.mkhytarmkhoian.herald

/**
 * Somewhere an [Event] can be sent.
 *
 * Implemented by vendor adapters, by the fan-out across several adapters, and by decorators that
 * wrap another implementation.
 *
 * Depend on this rather than on the fan-out: a ViewModel that only tracks events should take an
 * `EventTrackerService`.
 */
public fun interface EventTrackerService {
    public suspend fun track(event: Event)
}
