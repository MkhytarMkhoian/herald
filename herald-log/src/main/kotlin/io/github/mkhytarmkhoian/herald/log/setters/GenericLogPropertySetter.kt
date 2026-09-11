package io.github.mkhytarmkhoian.herald.log.setters

import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.asString
import io.github.mkhytarmkhoian.herald.log.AnalyticsLogger
import io.github.mkhytarmkhoian.herald.log.LogPropertySetter
import io.github.mkhytarmkhoian.herald.log.logRecord

public class GenericLogPropertySetter(
    private val property: Property,
    private val logger: AnalyticsLogger,
) : LogPropertySetter {

    override suspend fun set() {
        logger.log(
            logRecord(
                kind = "prop",
                headline = "${property.name} = ${property.value.asString}",
                parameters = emptyMap(),
            )
        )
    }
}
