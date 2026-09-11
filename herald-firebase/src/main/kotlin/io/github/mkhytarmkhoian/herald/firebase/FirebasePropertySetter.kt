package io.github.mkhytarmkhoian.herald.firebase

/**
 * One pending call to Firebase on behalf of one property.
 *
 * A `fun interface`, so a one-off handler inside a custom factory can be a lambda rather than a
 * class.
 */
public fun interface FirebasePropertySetter {
    public suspend fun set()
}
