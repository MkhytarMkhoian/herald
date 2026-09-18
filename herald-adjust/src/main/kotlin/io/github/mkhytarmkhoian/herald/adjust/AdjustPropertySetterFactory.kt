package io.github.mkhytarmkhoian.herald.adjust

import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.requireFallbackLast

/** The [AdjustEventTrackerFactory] counterpart for properties, with the same partial contract. */
public fun interface AdjustPropertySetterFactory {
    public fun create(property: Property): Resolution<AdjustPropertySetter>
}

/** Asks each factory in order and takes the first answer that is not a decline. */
public class CompositeAdjustPropertySetterFactory(
    private val factories: List<AdjustPropertySetterFactory>,
) : AdjustPropertySetterFactory {

    init {
        requireFallbackLast(factories)
    }

    public constructor(vararg factories: AdjustPropertySetterFactory) : this(factories.toList())

    override fun create(property: Property): Resolution<AdjustPropertySetter> {
        for (factory in factories) {
            val resolution = factory.create(property)
            if (resolution !is Resolution.Declined) return resolution
        }
        return Resolution.Declined
    }
}
