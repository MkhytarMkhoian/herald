package io.github.mkhytarmkhoian.herald.mixpanel

/**
 * One pending call to Mixpanel on behalf of one property.
 *
 * A `fun interface`, so a one-off handler inside a custom factory can be a lambda rather than a
 * class.
 */
public fun interface MixpanelPropertySetter {
    public suspend fun set()
}
