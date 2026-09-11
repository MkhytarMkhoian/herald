package io.github.mkhytarmkhoian.herald.adjust

/**
 * One pending call to Adjust on behalf of one property.
 *
 * A `fun interface`, so a one-off handler inside a custom factory can be a lambda rather than a
 * class.
 */
public fun interface AdjustPropertySetter {
    public suspend fun set()
}
