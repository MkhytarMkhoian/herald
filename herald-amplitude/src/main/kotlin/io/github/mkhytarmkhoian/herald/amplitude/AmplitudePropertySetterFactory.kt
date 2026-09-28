package io.github.mkhytarmkhoian.herald.amplitude

import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.requireFallbackLast

/**
 * The [AmplitudeEventTrackerFactory] counterpart for properties, with the same partial contract.
 */
public fun interface AmplitudePropertySetterFactory {
    public fun create(property: Property): Resolution<AmplitudePropertySetter>
}

/** Asks each factory in order and takes the first answer that is not a decline. */
public class CompositeAmplitudePropertySetterFactory(
    private val factories: List<AmplitudePropertySetterFactory>,
) : AmplitudePropertySetterFactory {

    init {
        requireFallbackLast(factories)
    }

    public constructor(vararg factories: AmplitudePropertySetterFactory) : this(factories.toList())

    override fun create(property: Property): Resolution<AmplitudePropertySetter> {
        for (factory in factories) {
            val resolution = factory.create(property)
            if (resolution !is Resolution.Declined) return resolution
        }
        return Resolution.Declined
    }
}
