package io.github.mkhytarmkhoian.herald.firebase

import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.Resolution

/** The [FirebaseEventTrackerFactory] counterpart for properties, with the same partial contract. */
public fun interface FirebasePropertySetterFactory {
    public fun create(property: Property): Resolution<FirebasePropertySetter>
}

/** Asks each factory in order and takes the first answer that is not a decline. */
public class CompositeFirebasePropertySetterFactory(
    private val factories: List<FirebasePropertySetterFactory>,
) : FirebasePropertySetterFactory {

    public constructor(vararg factories: FirebasePropertySetterFactory) : this(factories.toList())

    override fun create(property: Property): Resolution<FirebasePropertySetter> {
        for (factory in factories) {
            val resolution = factory.create(property)
            if (resolution !is Resolution.Declined) return resolution
        }
        return Resolution.Declined
    }
}
