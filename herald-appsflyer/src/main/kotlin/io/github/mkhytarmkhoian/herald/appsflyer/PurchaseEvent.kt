package io.github.mkhytarmkhoian.herald.appsflyer

import com.appsflyer.share.AFInAppEventParameterName
import io.github.mkhytarmkhoian.herald.Event

/**
 * A purchase, sent to AppsFlyer as its predefined `af_purchase` with the revenue under
 * `af_revenue`, which is what AppsFlyer counts as revenue and forwards to ad networks for ROAS.
 *
 * Lives here rather than in `herald-core` because the shape is AppsFlyer's: each field is one of
 * its predefined parameters. Claimed by [PurchaseAppsFlyerEventTrackerFactory]. [name] does not
 * reach AppsFlyer; [parameters] travel alongside the predefined ones.
 *
 * The same event can implement Adjust's and Amplitude's revenue markers as well — [revenue] and
 * [currency] are shared.
 */
public interface PurchaseEvent : Event {
    /** `af_revenue`. */
    public val revenue: Double

    /** `af_currency`, an ISO 4217 code. */
    public val currency: String

    /** `af_content_id`. */
    public val contentId: String? get() = null

    /** `af_content_type`. */
    public val contentType: String? get() = null

    /** `af_quantity`. */
    public val quantity: Int? get() = null

    /** `af_order_id`. */
    public val orderId: String? get() = null
}

/**
 * The event values for `af_purchase`: the event's parameters, then the predefined ones, which win
 * on a clash. An absent optional is left out rather than sent empty. Public because a custom
 * [AppsFlyerEventTracker] almost always needs it.
 */
public fun PurchaseEvent.toAppsFlyerEventValues(): Map<String, Any> =
    parameters.toAppsFlyerEventValues() + listOfNotNull(
        AFInAppEventParameterName.REVENUE to revenue,
        AFInAppEventParameterName.CURRENCY to currency,
        contentId?.let { AFInAppEventParameterName.CONTENT_ID to it },
        contentType?.let { AFInAppEventParameterName.CONTENT_TYPE to it },
        quantity?.let { AFInAppEventParameterName.QUANTITY to it },
        orderId?.let { AFInAppEventParameterName.ORDER_ID to it },
    )
