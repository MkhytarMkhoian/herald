package io.github.mkhytarmkhoian.herald.mixpanel

import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.Resolution

/**
 * The [MixpanelEventTrackerFactory] counterpart for properties, with the same partial contract.
 */
public fun interface MixpanelPropertySetterFactory {
    public fun create(property: Property): Resolution<MixpanelPropertySetter>
}

/** Asks each factory in order and takes the first answer that is not a decline. */
public class CompositeMixpanelPropertySetterFactory(
    private val factories: List<MixpanelPropertySetterFactory>,
) : MixpanelPropertySetterFactory {

    public constructor(vararg factories: MixpanelPropertySetterFactory) : this(factories.toList())

    override fun create(property: Property): Resolution<MixpanelPropertySetter> {
        for (factory in factories) {
            val resolution = factory.create(property)
            if (resolution !is Resolution.Declined) return resolution
        }
        return Resolution.Declined
    }
}
