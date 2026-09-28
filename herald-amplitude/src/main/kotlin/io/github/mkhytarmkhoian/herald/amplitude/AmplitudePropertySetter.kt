package io.github.mkhytarmkhoian.herald.amplitude

/**
 * One pending call to Amplitude on behalf of one property.
 *
 * A `fun interface`, so a one-off handler inside a custom factory can be a lambda rather than a
 * class.
 */
public fun interface AmplitudePropertySetter {
    public suspend fun set()
}
