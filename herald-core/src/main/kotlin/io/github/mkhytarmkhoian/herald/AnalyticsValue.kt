package io.github.mkhytarmkhoian.herald

/**
 * A parameter or property value, carrying its type so adapters can hand vendors a real number
 * rather than a number spelled as text.
 *
 * The type decides what a dashboard can do with it: Firebase and Mixpanel can sum or average a
 * numeric value and can only group a textual one. Vendors that accept strings only flatten it
 * with [asString].
 *
 * This types the value, not its meaning — an [Int] called `amount` is still just a number to an
 * adapter.
 *
 * The variants are named for the Kotlin types they hold, so inside this declaration the wrapped
 * types are spelled `kotlin.String` and so on.
 */
public sealed interface AnalyticsValue {

    @JvmInline
    public value class String(public val value: kotlin.String) : AnalyticsValue

    @JvmInline
    public value class Int(public val value: kotlin.Int) : AnalyticsValue

    @JvmInline
    public value class Long(public val value: kotlin.Long) : AnalyticsValue

    @JvmInline
    public value class Float(public val value: kotlin.Float) : AnalyticsValue

    @JvmInline
    public value class Double(public val value: kotlin.Double) : AnalyticsValue

    @JvmInline
    public value class Boolean(public val value: kotlin.Boolean) : AnalyticsValue
}

/**
 * The value as a string-only vendor would see it.
 *
 * An [AnalyticsValue.Int] of 3 reads as `3`; an [AnalyticsValue.Double] of 3.0 keeps its point and
 * reads as `3.0`.
 */
public val AnalyticsValue.asString: String
    get() = when (this) {
        is AnalyticsValue.String -> value
        is AnalyticsValue.Int -> value.toString()
        is AnalyticsValue.Long -> value.toString()
        is AnalyticsValue.Float -> value.toString()
        is AnalyticsValue.Double -> value.toString()
        is AnalyticsValue.Boolean -> value.toString()
    }
