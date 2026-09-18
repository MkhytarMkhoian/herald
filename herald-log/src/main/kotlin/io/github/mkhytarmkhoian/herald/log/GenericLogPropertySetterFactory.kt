package io.github.mkhytarmkhoian.herald.log

import io.github.mkhytarmkhoian.herald.FallbackFactory
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.log.setters.GenericLogPropertySetter

/** Prints any property. Claims everything, so it belongs last in a chain. */
public class GenericLogPropertySetterFactory(
    private val logger: AnalyticsLogger,
) : LogPropertySetterFactory, FallbackFactory {

    override fun create(property: Property): Resolution<LogPropertySetter> =
        Resolution.Claimed(GenericLogPropertySetter(property, logger))
}
