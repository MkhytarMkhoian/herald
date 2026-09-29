package io.github.mkhytarmkhoian.herald.adjust

import com.adjust.sdk.AdjustEvent
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event

/**
 * A purchase as Adjust counts revenue: a tokened event that also carries the amount, so Adjust
 * attributes it to the campaign that brought the user in.
 *
 * A final class, so an app event can't be one. The app's Adjust factory builds one from its
 * own event and sends it through
 * [RevenueEventTracker][io.github.mkhytarmkhoian.herald.adjust.trackers.RevenueEventTracker],
 * with the purchase's token.
 *
 * For ad revenue from a mediation SDK, see [AdjustAdRevenueEvent], which Adjust models as a
 * different call.
 */
public data class AdjustRevenueEvent(
    /** Usually the app event's own. It isn't sent: Adjust identifies the event by its token. */
    override val name: String,

    public val revenue: Double,

    /** ISO 4217 code, as Adjust expects it. */
    public val currency: String,

    /**
     * The transaction id, so a purchase that is reported twice — a retried callback, a restored
     * receipt — is counted once. Adjust drops a revenue event whose id it has already seen.
     * Absent by default, which counts every report.
     */
    public val deduplicationId: String? = null,

    /** Sent as callback parameters, as on every other Adjust event. Usually the app event's own. */
    override val parameters: Map<String, AnalyticsValue> = emptyMap(),
) : Event

/**
 * Builds the Adjust payload under [eventToken]: the revenue, the deduplication id when there is
 * one, and [AdjustRevenueEvent.parameters] as callback parameters. Public because a custom
 * [AdjustEventTracker] almost always needs it.
 */
public fun AdjustRevenueEvent.toAdjustEvent(eventToken: String): AdjustEvent =
    parameters.toAdjustEvent(eventToken).also { adjustEvent ->
        adjustEvent.setRevenue(revenue, currency)
        deduplicationId?.let(adjustEvent::setDeduplicationId)
    }
