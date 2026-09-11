package io.github.mkhytarmkhoian.herald.log

/**
 * Where the log adapter prints.
 *
 * Every record is one call with one string, already formatted. Implement it over whatever logging
 * you use — a `fun interface`, so `AnalyticsLogger { Log.d("analytics", it) }` is enough — and
 * pass it to the adapter's factories.
 *
 * One level only: a record is debug output by nature, and which level that maps to is the
 * implementation's decision.
 */
public fun interface AnalyticsLogger {
    public fun log(message: String)
}
