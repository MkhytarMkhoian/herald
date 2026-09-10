package io.github.mkhytarmkhoian.herald

/**
 * Builds an event's parameters without naming [AnalyticsValue] at every entry.
 *
 * The overload you call decides the type, so `put("seats", 3)` stores an [AnalyticsValue.Int] and
 * `put("price", 9.99)` an [AnalyticsValue.Double].
 *
 * ```kotlin
 * override val parameters = parameters {
 *     put("plan", "pro")
 *     put("seats", 3)
 *     put("price", 9.99)
 *     put("trial", false)
 * }
 * ```
 */
public fun parameters(block: ParametersBuilder.() -> Unit): Map<String, AnalyticsValue> =
    ParametersBuilder().apply(block).build()

/** Receiver of the [parameters] block. */
public class ParametersBuilder internal constructor() {

    private val values = mutableMapOf<String, AnalyticsValue>()

    public fun put(name: String, value: String) {
        values[name] = AnalyticsValue.String(value)
    }

    public fun put(name: String, value: Int) {
        values[name] = AnalyticsValue.Int(value)
    }

    public fun put(name: String, value: Long) {
        values[name] = AnalyticsValue.Long(value)
    }

    public fun put(name: String, value: Float) {
        values[name] = AnalyticsValue.Float(value)
    }

    public fun put(name: String, value: Double) {
        values[name] = AnalyticsValue.Double(value)
    }

    public fun put(name: String, value: Boolean) {
        values[name] = AnalyticsValue.Boolean(value)
    }

    internal fun build(): Map<String, AnalyticsValue> = values.toMap()
}
