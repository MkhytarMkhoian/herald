package io.github.mkhytarmkhoian.herald.meta

import android.os.Bundle
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.asString

// A Float widened as-is reads 0.1 as 0.10000000149011612; through its text it stays 0.1.
private fun Float.faithfulDouble(): Double = toString().toDouble()

/**
 * Event parameters as Meta takes them: numbers stay numbers, and true/false becomes text, since
 * Meta's parameters are text or numbers.
 */
public fun Map<String, AnalyticsValue>.toMetaParameters(): Bundle = Bundle().apply {
    for ((key, parameter) in this@toMetaParameters) {
        when (parameter) {
            is AnalyticsValue.Int -> putLong(key, parameter.value.toLong())
            is AnalyticsValue.Long -> putLong(key, parameter.value)
            is AnalyticsValue.Float -> putDouble(key, parameter.value.faithfulDouble())
            is AnalyticsValue.Double -> putDouble(key, parameter.value)
            is AnalyticsValue.String, is AnalyticsValue.Boolean -> putString(key, parameter.asString)
        }
    }
}
