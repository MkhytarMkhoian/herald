package io.github.mkhytarmkhoian.herald.appsflyer

/**
 * One pending call to AppsFlyer on behalf of one event.
 *
 * A `fun interface`, so a one-off handler inside a custom factory can be a lambda rather than a
 * class.
 */
public fun interface AppsFlyerEventTracker {
    public suspend fun track()
}
