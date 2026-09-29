package io.github.mkhytarmkhoian.herald.amplitude

import com.amplitude.core.events.Revenue
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event

/**
 * A purchase as Amplitude's revenue API takes it, so it lands in Amplitude's revenue charts and
 * LTV rather than as an ordinary event. The fields are Amplitude's `Revenue` fields, under
 * Amplitude's names.
 *
 * A final class, so an app event can't be one. The app's Amplitude factory builds one from its
 * own event and sends it through
 * [RevenueEventTracker][io.github.mkhytarmkhoian.herald.amplitude.trackers.RevenueEventTracker].
 */
public data class AmplitudeRevenueEvent(
    /** Usually the app event's own. It isn't sent: Amplitude uses its own revenue event type. */
    override val name: String,

    /** The price of one item. Amplitude drops a revenue event without one. */
    public val price: Double,

    public val quantity: Int = 1,
    public val productId: String? = null,

    /** Amplitude's free-form revenue category — `purchase`, `refund`, `subscription`, ... */
    public val revenueType: String? = null,

    /** ISO 4217 code. Absent, Amplitude assumes USD. */
    public val currency: String? = null,

    /** The total, when it is not [price] × [quantity] — after a discount, say. */
    public val revenue: Double? = null,

    public val receipt: String? = null,
    public val receiptSig: String? = null,

    /**
     * The transaction id, so a purchase that is reported twice — a retried callback, a restored
     * receipt — is counted once. Amplitude drops an event whose insert id it has already seen.
     * Absent by default, which counts every report.
     */
    public val insertId: String? = null,

    /** Sent as the revenue's event properties. Usually the app event's own. */
    override val parameters: Map<String, AnalyticsValue> = emptyMap(),
) : Event

/**
 * Builds the Amplitude payload for revenue, with [AmplitudeRevenueEvent.parameters] as its
 * properties. Public because a custom [AmplitudeEventTracker] almost always needs it.
 */
public fun AmplitudeRevenueEvent.toAmplitudeRevenue(): Revenue = Revenue().also {
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
