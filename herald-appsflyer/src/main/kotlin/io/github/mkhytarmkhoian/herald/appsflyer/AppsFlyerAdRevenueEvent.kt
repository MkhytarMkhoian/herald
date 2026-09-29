package io.github.mkhytarmkhoian.herald.appsflyer

import com.appsflyer.share.AFAdRevenueData
import com.appsflyer.share.MediationNetwork
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event

/**
 * Ad revenue reported by a mediation SDK, as AppsFlyer's ad-revenue API takes it rather than as
 * an in-app event. The fields are `AFAdRevenueData`'s.
 *
 * A final class, so an app event can't be one. The app's AppsFlyer factory builds one from its
 * own event and sends it through
 * [AdRevenueEventTracker][io.github.mkhytarmkhoian.herald.appsflyer.trackers.AdRevenueEventTracker].
 *
 * For revenue from a purchase or a subscription, see [AppsFlyerPurchaseEvent] and
 * [AppsFlyerSubscribeEvent].
 */
public data class AppsFlyerAdRevenueEvent(
    /** Usually the app event's own. It isn't sent: AppsFlyer's ad-revenue API has no event name. */
    override val name: String,

    /** The network that served the ad — AppsFlyer's `monetizationNetwork`. */
    public val monetizationNetwork: String,
    public val mediationNetwork: MediationNetwork,
    public val revenue: Double,

    /** ISO 4217 code — AppsFlyer's `currencyIso4217Code`. */
    public val currency: String,

    /** Sent as the additional parameters of the ad-revenue call. Usually the app event's own. */
    override val parameters: Map<String, AnalyticsValue> = emptyMap(),
) : Event

/**
 * Builds the AppsFlyer payload for ad revenue. Public because a custom [AppsFlyerEventTracker]
 * almost always needs it.
 */
public fun AppsFlyerAdRevenueEvent.toAFAdRevenueData(): AFAdRevenueData =
    AFAdRevenueData(monetizationNetwork, mediationNetwork, currency, revenue)
