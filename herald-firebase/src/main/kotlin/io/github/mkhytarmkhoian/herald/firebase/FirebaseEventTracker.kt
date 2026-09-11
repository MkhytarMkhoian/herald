package io.github.mkhytarmkhoian.herald.firebase

import android.os.Bundle
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.asString

/**
 * One pending call to Firebase on behalf of one event.
 *
 * A `fun interface`, so a one-off handler inside a custom factory can be a lambda rather than a
 * class.
 */
public fun interface FirebaseEventTracker {
    public suspend fun track()
}

/**
 * Widens a float to the decimal it was written as. `9.99f.toDouble()` is `9.989999771118164`,
 * which is what a GA4 report would otherwise show.
 */
private fun Float.faithfulDouble(): Double = toString().toDouble()

/**
 * Event parameters as a Firebase [Bundle].
 *
 * GA4 accepts string, integer and floating-point parameters, so a number arrives as a number and
 * stays aggregable. It has no boolean parameter type, so a flag is written as its string form.
 */
public fun Map<String, AnalyticsValue>.toBundle(): Bundle = Bundle().apply {
    for ((key, parameter) in this@toBundle) {
        when (parameter) {
            is AnalyticsValue.Int -> putLong(key, parameter.value.toLong())
            is AnalyticsValue.Long -> putLong(key, parameter.value)
            is AnalyticsValue.Float -> putDouble(key, parameter.value.faithfulDouble())
            is AnalyticsValue.Double -> putDouble(key, parameter.value)
            is AnalyticsValue.String, is AnalyticsValue.Boolean -> putString(key, parameter.asString)
        }
    }
}
