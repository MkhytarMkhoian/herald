package io.github.mkhytarmkhoian.herald.testing

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Property

internal fun event(eventName: String, eventParameters: Map<String, AnalyticsValue> = emptyMap()) =
    object : Event {
        override val name = eventName
        override val parameters = eventParameters
    }

internal fun property(propertyName: String, propertyValue: AnalyticsValue) = object : Property {
    override val name = propertyName
    override val value = propertyValue
}
