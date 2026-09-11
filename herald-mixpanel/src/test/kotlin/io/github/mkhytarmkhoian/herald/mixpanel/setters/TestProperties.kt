package io.github.mkhytarmkhoian.herald.mixpanel.setters

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Property

/**
 * A real [Property] rather than a mock: `value` returns a `@JvmInline value class`, and stubbing
 * such a getter erases the type and fails the cast inside mockk.
 */
internal fun property(propertyName: String, propertyValue: AnalyticsValue) = object : Property {
    override val name = propertyName
    override val value = propertyValue
}
