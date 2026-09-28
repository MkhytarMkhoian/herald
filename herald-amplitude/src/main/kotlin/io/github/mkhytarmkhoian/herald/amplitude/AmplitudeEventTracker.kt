package io.github.mkhytarmkhoian.herald.amplitude

/**
 * One pending call to Amplitude on behalf of one event.
 *
 * A `fun interface`, so a one-off handler inside a custom factory can be a lambda rather than a
 * class.
 */
public fun interface AmplitudeEventTracker {
    public suspend fun track()
}
