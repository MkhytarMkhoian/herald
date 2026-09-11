package io.github.mkhytarmkhoian.herald.mixpanel

import io.github.mkhytarmkhoian.herald.AnalyticsValue

/**
 * The value as Mixpanel should store it. Mixpanel properties are JSON, so numbers and booleans go
 * across as themselves rather than as text and stay usable in numeric filters.
 */
public val AnalyticsValue.asMixpanelValue: Any
    get() = when (this) {
        is AnalyticsValue.String -> value
        is AnalyticsValue.Int -> value
        is AnalyticsValue.Long -> value
        is AnalyticsValue.Float -> value
        is AnalyticsValue.Double -> value
        is AnalyticsValue.Boolean -> value
    }

/** Event parameters as Mixpanel should store them; see [asMixpanelValue]. */
public fun Map<String, AnalyticsValue>.toMixpanelProperties(): Map<String, Any> =
    mapValues { (_, parameter) -> parameter.asMixpanelValue }
