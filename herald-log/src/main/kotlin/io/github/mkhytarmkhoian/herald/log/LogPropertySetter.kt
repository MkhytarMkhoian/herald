package io.github.mkhytarmkhoian.herald.log

/**
 * One pending log line on behalf of one property.
 *
 * A `fun interface`, so a one-off handler inside a custom factory can be a lambda rather than a
 * class.
 */
public fun interface LogPropertySetter {
    public suspend fun set()
}
