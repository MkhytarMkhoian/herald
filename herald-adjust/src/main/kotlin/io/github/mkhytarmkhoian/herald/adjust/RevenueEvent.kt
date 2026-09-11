package io.github.mkhytarmkhoian.herald.adjust

import io.github.mkhytarmkhoian.herald.Event

/**
 * An event that carries purchase revenue, so Adjust attributes the amount to the campaign that
 * brought the user in.
 *
 * Revenue is an attribute of an ordinary tokened event, not a different kind of send: the event
 * still needs a token in [TokenAdjustEventTrackerFactory]'s map and still goes out through
 * [Event.toAdjustEvent], which sets `setRevenue` when it sees this type. There is no separate
 * tracker or factory to add to the chain.
 *
 * Lives here rather than in `herald-core` because the shape is Adjust's: Firebase wants a
 * `purchase` event with `value` and `currency` parameters, which a plain event already expresses.
 *
 * For ad revenue from a mediation SDK, see [AdRevenueEvent], which Adjust models as a different
 * call.
 */
public interface RevenueEvent : Event {
    public val revenue: Double

    /** ISO 4217 code, as Adjust expects it. */
    public val currency: String

    /**
     * The transaction id, so a purchase that is reported twice — a retried callback, a restored
     * receipt — is counted once. Adjust drops a revenue event whose id it has already seen.
     * Absent by default, which counts every report.
     */
    public val deduplicationId: String? get() = null
}
