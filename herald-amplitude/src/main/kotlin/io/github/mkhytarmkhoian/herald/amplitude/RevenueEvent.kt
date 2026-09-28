package io.github.mkhytarmkhoian.herald.amplitude

import com.amplitude.core.events.Revenue
import io.github.mkhytarmkhoian.herald.Event

/**
 * A purchase, sent through Amplitude's revenue API so it lands in Amplitude's revenue charts and
 * LTV rather than as an ordinary event.
 *
 * Lives here rather than in `herald-core` because the shape is Amplitude's: the fields are
 * Amplitude's `Revenue` fields, under Amplitude's names. Claimed by
 * [RevenueAmplitudeEventTrackerFactory]. Amplitude records it under its own revenue event type, so
 * [name] does not reach Amplitude; [parameters] travel as its event properties.
 *
 * The same event can implement Adjust's `RevenueEvent` as well: a non-null `currency` and a
 * `revenue` satisfy both.
 */
public interface RevenueEvent : Event {
    /** The price of one item. Amplitude drops a revenue event without one. */
    public val price: Double

    public val quantity: Int get() = 1
    public val productId: String? get() = null

    /** Amplitude's free-form revenue category — `purchase`, `refund`, `subscription`, ... */
    public val revenueType: String? get() = null

    /** ISO 4217 code. Absent, Amplitude assumes USD. */
    public val currency: String? get() = null

    /** The total, when it is not [price] × [quantity] — after a discount, say. */
    public val revenue: Double? get() = null

    public val receipt: String? get() = null
    public val receiptSig: String? get() = null

    /**
     * The transaction id, so a purchase that is reported twice — a retried callback, a restored
     * receipt — is counted once. Amplitude drops an event whose insert id it has already seen.
     * Absent by default, which counts every report.
     */
    public val insertId: String? get() = null
}

/**
 * Builds the Amplitude payload for a revenue event, carrying the event's parameters as its
 * properties. Public because a custom [AmplitudeEventTracker] almost always needs it.
 */
public fun RevenueEvent.toAmplitudeRevenue(): Revenue = Revenue().also {
    it.price = price
    it.quantity = quantity
    it.productId = productId
    it.revenueType = revenueType
    it.currency = currency
    it.revenue = revenue
    it.receipt = receipt
    it.receiptSig = receiptSig
    it.properties = parameters.toAmplitudeProperties().toMutableMap()
}
