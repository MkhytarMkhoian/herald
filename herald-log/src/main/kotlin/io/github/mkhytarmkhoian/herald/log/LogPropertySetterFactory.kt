package io.github.mkhytarmkhoian.herald.log

import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.requireFallbackLast

/** The [LogEventTrackerFactory] counterpart for properties, with the same partial contract. */
public fun interface LogPropertySetterFactory {
    public fun create(property: Property): Resolution<LogPropertySetter>
}

/** Asks each factory in order and takes the first answer that is not a decline. */
public class CompositeLogPropertySetterFactory(
    private val factories: List<LogPropertySetterFactory>,
) : LogPropertySetterFactory {

    init {
        requireFallbackLast(factories)
    }

    public constructor(vararg factories: LogPropertySetterFactory) : this(factories.toList())

    override fun create(property: Property): Resolution<LogPropertySetter> {
        for (factory in factories) {
            val resolution = factory.create(property)
            if (resolution !is Resolution.Declined) return resolution
        }
        return Resolution.Declined
    }
}
