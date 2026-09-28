package io.github.mkhytarmkhoian.herald.appsflyer

import com.appsflyer.share.AFInAppEventParameterName
import io.github.mkhytarmkhoian.herald.Event

/**
 * A paid subscription, sent to AppsFlyer as its predefined `af_subscribe` with the revenue under
 * `af_revenue`, which is what AppsFlyer counts as revenue and forwards to ad networks for ROAS.
 *
 * Lives here rather than in `herald-core` because the shape is AppsFlyer's. Claimed by
 * [SubscribeAppsFlyerEventTrackerFactory]. [name] does not reach AppsFlyer; [parameters] travel
 * alongside the predefined ones.
 *
 * The same event can implement Adjust's and Amplitude's revenue markers as well — [revenue] and
 * [currency] are shared.
 */
public interface SubscribeEvent : Event {
    /** `af_revenue`. */
    public val revenue: Double

    /** `af_currency`, an ISO 4217 code. */
    public val currency: String
}

/**
 * The event values for `af_subscribe`: the event's parameters, then the predefined ones, which win
 * on a clash. Public because a custom [AppsFlyerEventTracker] almost always needs it.
 */
public fun SubscribeEvent.toAppsFlyerEventValues(): Map<String, Any> =
    parameters.toAppsFlyerEventValues() + mapOf(
        AFInAppEventParameterName.REVENUE to revenue,
        AFInAppEventParameterName.CURRENCY to currency,
    )
