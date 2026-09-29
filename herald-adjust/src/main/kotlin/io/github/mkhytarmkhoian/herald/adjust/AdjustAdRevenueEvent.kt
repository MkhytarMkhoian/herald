package io.github.mkhytarmkhoian.herald.adjust

import com.adjust.sdk.AdjustAdRevenue
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.asString

/**
 * Ad revenue reported by a mediation SDK, as Adjust's dedicated ad-revenue API takes it rather
 * than as a tokened event.
 *
 * A final class, so an app event can't be one. The app's Adjust factory builds one from its
 * own event and sends it through
 * [AdRevenueEventTracker][io.github.mkhytarmkhoian.herald.adjust.trackers.AdRevenueEventTracker].
 *
 * For revenue on a purchase, see [AdjustRevenueEvent], which stays a tokened event.
 */
public data class AdjustAdRevenueEvent(
    /** Usually the app event's own. It isn't sent: Adjust's ad-revenue API has no event name. */
    override val name: String,

    /** Adjust's name for the mediation SDK: `applovin_max_sdk`, `admob_sdk`, and so on. */
    public val source: String,
    public val revenue: Double,

    /** ISO 4217 code, as Adjust expects it. */
    public val currency: String,

    public val adImpressionsCount: Int? = null,
    public val adRevenueNetwork: String? = null,
    public val adRevenueUnit: String? = null,
    public val adRevenuePlacement: String? = null,

    /** Sent as callback parameters, as on every other Adjust event. Usually the app event's own. */
    override val parameters: Map<String, AnalyticsValue> = emptyMap(),
) : Event

/**
 * Builds the Adjust payload for ad revenue, with [AdjustAdRevenueEvent.parameters] as callback
 * parameters. Public because a custom [AdjustEventTracker] almost always needs it.
 */
public fun AdjustAdRevenueEvent.toAdjustAdRevenue(): AdjustAdRevenue {
    val adRevenue = AdjustAdRevenue(source)
    adRevenue.setRevenue(revenue, currency)
    adImpressionsCount?.let(adRevenue::setAdImpressionsCount)
    adRevenueNetwork?.let(adRevenue::setAdRevenueNetwork)
    adRevenueUnit?.let(adRevenue::setAdRevenueUnit)
    adRevenuePlacement?.let(adRevenue::setAdRevenuePlacement)
    for ((key, value) in parameters) {
        adRevenue.addCallbackParameter(key, value.asString)
    }
    return adRevenue
}
