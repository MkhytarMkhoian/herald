package io.github.mkhytarmkhoian.herald.adjust

import com.adjust.sdk.AdjustAdRevenue
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.asString

/**
 * Ad revenue reported by a mediation SDK, sent through Adjust's dedicated ad-revenue API rather
 * than as a tokened event.
 *
 * Lives here rather than in `herald-core` because only Adjust has this API: Firebase and Mixpanel
 * see an ad impression as an ordinary event, and their generic factories already send it as one.
 * Claimed by [AdRevenueAdjustEventTrackerFactory].
 *
 * [source] is Adjust's name for the mediation SDK — `applovin_max_sdk`, `admob_sdk`, and so on,
 * from Adjust's documentation. The four optional fields are the breakdown Adjust reports on.
 * [parameters] travel as callback parameters, as on every other Adjust event.
 *
 * For revenue on a purchase, see [RevenueEvent], which stays a tokened event.
 */
public interface AdRevenueEvent : Event {
    public val source: String
    public val revenue: Double

    /** ISO 4217 code, as Adjust expects it. */
    public val currency: String

    public val adImpressionsCount: Int? get() = null
    public val adRevenueNetwork: String? get() = null
    public val adRevenueUnit: String? get() = null
    public val adRevenuePlacement: String? get() = null
}

/**
 * Builds the Adjust payload for an ad-revenue event, carrying the event's parameters as callback
 * parameters. Public because a custom [AdjustEventTracker] almost always needs it.
 */
public fun AdRevenueEvent.toAdjustAdRevenue(): AdjustAdRevenue {
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
