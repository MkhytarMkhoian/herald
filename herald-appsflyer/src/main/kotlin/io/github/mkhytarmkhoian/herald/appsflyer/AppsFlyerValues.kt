package io.github.mkhytarmkhoian.herald.appsflyer

import io.github.mkhytarmkhoian.herald.AnalyticsValue

/**
 * The value as AppsFlyer should receive it. AppsFlyer event values are JSON, so numbers and
 * booleans go across as themselves rather than as text — `af_revenue` in particular has to arrive
 * as a number for AppsFlyer to count it as revenue.
 */
public val AnalyticsValue.asAppsFlyerValue: Any
    get() = when (this) {
        is AnalyticsValue.String -> value
        is AnalyticsValue.Int -> value
        is AnalyticsValue.Long -> value
        is AnalyticsValue.Float -> value
        is AnalyticsValue.Double -> value
        is AnalyticsValue.Boolean -> value
    }

/** Event parameters as AppsFlyer event values; see [asAppsFlyerValue]. */
public fun Map<String, AnalyticsValue>.toAppsFlyerEventValues(): Map<String, Any> =
    mapValues { (_, parameter) -> parameter.asAppsFlyerValue }
