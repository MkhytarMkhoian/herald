package io.github.mkhytarmkhoian.herald

/**
 * Where Herald reports a vendor failure it has contained.
 *
 * A provider that throws is caught so the remaining providers still run. Without a reporter, a
 * vendor that fails on every call looks exactly like one that is working — so implement this over
 * whatever crash or logging tool you already use.
 *
 * [operation] says what was in flight and, for a track or a property, which one by name. It never
 * carries parameters, property values or the user id, so it is safe to forward whole to a crash
 * reporter.
 */
public fun interface AnalyticsErrorReporter {
    public fun onFailure(provider: String, operation: AnalyticsOperation, failure: Throwable)

    public companion object {
        /** Discards failures. The default, so reporting is something you opt in to. */
        public val NONE: AnalyticsErrorReporter = AnalyticsErrorReporter { _, _, _ -> }
    }
}
