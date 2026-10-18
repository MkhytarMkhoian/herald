package io.github.mkhytarmkhoian.herald.meta

/**
 * One pending call to Meta on behalf of one event.
 *
 * A `fun interface`, so a one-off handler inside a custom factory can be a lambda rather than a
 * class.
 */
public fun interface MetaEventTracker {
    public suspend fun track()
}
