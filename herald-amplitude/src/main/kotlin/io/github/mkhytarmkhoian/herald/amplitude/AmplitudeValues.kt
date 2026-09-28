package io.github.mkhytarmkhoian.herald.amplitude

import io.github.mkhytarmkhoian.herald.AnalyticsValue

/**
 * The value as Amplitude should store it. Amplitude properties are JSON, so numbers and booleans go
 * across as themselves rather than as text and stay usable in numeric filters.
 */
public val AnalyticsValue.asAmplitudeValue: Any
    get() = when (this) {
        is AnalyticsValue.String -> value
        is AnalyticsValue.Int -> value
        is AnalyticsValue.Long -> value
        is AnalyticsValue.Float -> value
        is AnalyticsValue.Double -> value
        is AnalyticsValue.Boolean -> value
    }

/** Event parameters as Amplitude should store them; see [asAmplitudeValue]. */
public fun Map<String, AnalyticsValue>.toAmplitudeProperties(): Map<String, Any> =
    mapValues { (_, parameter) -> parameter.asAmplitudeValue }
