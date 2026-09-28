package io.github.mkhytarmkhoian.herald.appsflyer

import com.appsflyer.share.AFAdRevenueData
import com.appsflyer.share.MediationNetwork
import io.github.mkhytarmkhoian.herald.Event

/**
 * Ad revenue reported by a mediation SDK, sent through AppsFlyer's ad-revenue API rather than as an
 * in-app event.
 *
 * Lives here rather than in `herald-core` because the shape is AppsFlyer's: the fields are
 * `AFAdRevenueData`'s. Claimed by [AdRevenueAppsFlyerEventTrackerFactory]. [name] does not reach
 * AppsFlyer; [parameters] travel as the additional parameters of the ad-revenue call.
 *
 * For revenue from a purchase or a subscription, see [PurchaseEvent] and [SubscribeEvent].
 *
 * The same event can implement Adjust's `AdRevenueEvent` as well — [revenue] and [currency] are
 * shared — so one impression callback reaches both.
 */
public interface AdRevenueEvent : Event {
    /** The network that served the ad — AppsFlyer's `monetizationNetwork`. */
    public val monetizationNetwork: String
    public val mediationNetwork: MediationNetwork
    public val revenue: Double

    /** ISO 4217 code — AppsFlyer's `currencyIso4217Code`. */
    public val currency: String
}

/**
 * Builds the AppsFlyer payload for an ad-revenue event. Public because a custom
 * [AppsFlyerEventTracker] almost always needs it.
 */
public fun AdRevenueEvent.toAFAdRevenueData(): AFAdRevenueData =
    AFAdRevenueData(monetizationNetwork, mediationNetwork, currency, revenue)
