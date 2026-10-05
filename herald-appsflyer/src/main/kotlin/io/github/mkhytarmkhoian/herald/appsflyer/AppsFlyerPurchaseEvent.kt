package io.github.mkhytarmkhoian.herald.appsflyer

import com.appsflyer.share.AFInAppEventParameterName
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event

/**
 * A purchase as AppsFlyer's predefined `af_purchase`, with the revenue under `af_revenue`, which
 * is what AppsFlyer counts as revenue and forwards to ad networks for ROAS.
 *
 * A final class, so an app event can't be one. The app's AppsFlyer factory builds one from its
 * own event and sends it through
 * [PurchaseEventTracker][io.github.mkhytarmkhoian.herald.appsflyer.trackers.PurchaseEventTracker].
 */
public data class AppsFlyerPurchaseEvent(
    /** Usually the app event's own. It isn't sent: AppsFlyer receives `af_purchase`. */
    override val name: String,

    /** `af_revenue`. */
    public val revenue: Double,

    /** `af_currency`, an ISO 4217 code. */
    public val currency: String,

    /** `af_content_id`. */
    public val contentId: String? = null,

    /** `af_content_type`. */
    public val contentType: String? = null,

    /** `af_quantity`. */
    public val quantity: Int? = null,

    /** `af_order_id`. */
    public val orderId: String? = null,

    /** Sent alongside the predefined values; don't use their keys. Usually the app event's own. */
    override val parameters: Map<String, AnalyticsValue> = emptyMap(),
) : Event

/**
 * The event values for `af_purchase`: [AppsFlyerPurchaseEvent.parameters], then the predefined
 * ones. An absent optional is left out rather than sent empty. Public because a custom
 * [AppsFlyerEventTracker] almost always needs it.
 *
 * Throws an [IllegalArgumentException] if a parameter has a key the purchase sets itself.
 */
public fun AppsFlyerPurchaseEvent.toAppsFlyerEventValues(): Map<String, Any> =
    withParameters(
        listOfNotNull(
            AFInAppEventParameterName.REVENUE to revenue,
            AFInAppEventParameterName.CURRENCY to currency,
            contentId?.let { AFInAppEventParameterName.CONTENT_ID to it },
            contentType?.let { AFInAppEventParameterName.CONTENT_TYPE to it },
            quantity?.let { AFInAppEventParameterName.QUANTITY to it },
            orderId?.let { AFInAppEventParameterName.ORDER_ID to it },
        ).toMap(),
    )
