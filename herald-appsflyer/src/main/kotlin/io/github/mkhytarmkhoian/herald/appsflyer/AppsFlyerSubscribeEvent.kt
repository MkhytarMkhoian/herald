package io.github.mkhytarmkhoian.herald.appsflyer

import com.appsflyer.share.AFInAppEventParameterName
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event

/**
 * A paid subscription as AppsFlyer's predefined `af_subscribe`, with the revenue under
 * `af_revenue`, which is what AppsFlyer counts as revenue and forwards to ad networks for ROAS.
 *
 * A final class, so an app event can't be one. The app's AppsFlyer factory builds one from its
 * own event and sends it through
 * [SubscribeEventTracker][io.github.mkhytarmkhoian.herald.appsflyer.trackers.SubscribeEventTracker].
 */
public data class AppsFlyerSubscribeEvent(
    /** Usually the app event's own. It isn't sent: AppsFlyer receives `af_subscribe`. */
    override val name: String,

    /** `af_revenue`. */
    public val revenue: Double,

    /** `af_currency`, an ISO 4217 code. */
    public val currency: String,

    /** Sent alongside the predefined values; don't use their keys. Usually the app event's own. */
    override val parameters: Map<String, AnalyticsValue> = emptyMap(),
) : Event

/**
 * The event values for `af_subscribe`: [AppsFlyerSubscribeEvent.parameters], then the predefined
 * ones. Public because a custom [AppsFlyerEventTracker] almost always needs it.
 *
 * Throws an [IllegalArgumentException] if a parameter has a key the subscription sets itself.
 */
public fun AppsFlyerSubscribeEvent.toAppsFlyerEventValues(): Map<String, Any> =
    withParameters(
        mapOf(
            AFInAppEventParameterName.REVENUE to revenue,
            AFInAppEventParameterName.CURRENCY to currency,
        ),
    )
