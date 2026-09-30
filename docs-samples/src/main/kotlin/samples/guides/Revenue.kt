package samples.guides

import android.content.Context
import com.adjust.sdk.AdjustInstance
import com.amplitude.android.Amplitude
import com.appsflyer.AppsFlyerLib
import com.appsflyer.share.MediationNetwork
import com.google.firebase.analytics.FirebaseAnalytics
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.adjust.AdjustAdRevenueEvent
import io.github.mkhytarmkhoian.herald.adjust.AdjustEventTracker
import io.github.mkhytarmkhoian.herald.adjust.AdjustEventTrackerFactory
import io.github.mkhytarmkhoian.herald.adjust.AdjustRevenueEvent
import io.github.mkhytarmkhoian.herald.adjust.trackers.RevenueEventTracker as AdjustRevenueEventTracker
import io.github.mkhytarmkhoian.herald.amplitude.AmplitudeEventTracker
import io.github.mkhytarmkhoian.herald.amplitude.AmplitudeEventTrackerFactory
import io.github.mkhytarmkhoian.herald.amplitude.AmplitudeRevenueEvent
import io.github.mkhytarmkhoian.herald.amplitude.trackers.RevenueEventTracker
import io.github.mkhytarmkhoian.herald.appsflyer.AppsFlyerAdRevenueEvent
import io.github.mkhytarmkhoian.herald.appsflyer.AppsFlyerEventTracker
import io.github.mkhytarmkhoian.herald.appsflyer.AppsFlyerEventTrackerFactory
import io.github.mkhytarmkhoian.herald.appsflyer.AppsFlyerPurchaseEvent
import io.github.mkhytarmkhoian.herald.appsflyer.trackers.PurchaseEventTracker
import io.github.mkhytarmkhoian.herald.parameters
import io.github.mkhytarmkhoian.herald.adjust.trackers.AdRevenueEventTracker as AdjustAdRevenueEventTracker
import io.github.mkhytarmkhoian.herald.appsflyer.trackers.AdRevenueEventTracker as AppsFlyerAdRevenueEventTracker

// --8<-- [start:purchase]
data class SubscriptionPurchased(
    val plan: String,
    val price: Double,
    val currency: String,
    val orderId: String,
) : Event {
    // Firebase and Mixpanel log it as it is: GA4's recommended purchase, with typed parameters.
    override val name = FirebaseAnalytics.Event.PURCHASE
    override val parameters = parameters {
        put(FirebaseAnalytics.Param.VALUE, price)
        put(FirebaseAnalytics.Param.CURRENCY, currency)
        put("plan", plan)
    }
}
// --8<-- [end:purchase]

// --8<-- [start:mappings]
// Adjust: the tokened event, plus revenue, counted once per transaction.
fun SubscriptionPurchased.toAdjustRevenueEvent() = AdjustRevenueEvent(
    name = name,
    revenue = price,
    currency = currency,
    deduplicationId = orderId,
    parameters = parameters,
)

// AppsFlyer: af_purchase, with the amount under af_revenue.
fun SubscriptionPurchased.toAppsFlyerPurchaseEvent() = AppsFlyerPurchaseEvent(
    name = name,
    revenue = price,
    currency = currency,
    contentId = plan,
    orderId = orderId,
    parameters = parameters,
)

// Amplitude: the revenue API, deduplicated by the insert id.
fun SubscriptionPurchased.toAmplitudeRevenueEvent() = AmplitudeRevenueEvent(
    name = name,
    price = price,
    productId = plan,
    currency = currency,
    insertId = orderId,
    parameters = parameters,
)
// --8<-- [end:mappings]

// --8<-- [start:factories]
class BillingAdjustEventTrackerFactory(private val adjust: AdjustInstance) : AdjustEventTrackerFactory {
    override fun create(event: Event): Resolution<AdjustEventTracker> = when (event) {
        is SubscriptionPurchased -> Resolution.Claimed(
            AdjustRevenueEventTracker(event.toAdjustRevenueEvent(), eventToken = "abc123", adjust),
        )
        else -> Resolution.Declined
    }
}

class BillingAppsFlyerEventTrackerFactory(
    private val appsFlyer: AppsFlyerLib,
    private val context: Context,
) : AppsFlyerEventTrackerFactory {
    override fun create(event: Event): Resolution<AppsFlyerEventTracker> = when (event) {
        is SubscriptionPurchased -> Resolution.Claimed(
            PurchaseEventTracker(event.toAppsFlyerPurchaseEvent(), appsFlyer, context),
        )
        else -> Resolution.Declined
    }
}

class BillingAmplitudeEventTrackerFactory(private val amplitude: Amplitude) : AmplitudeEventTrackerFactory {
    override fun create(event: Event): Resolution<AmplitudeEventTracker> = when (event) {
        is SubscriptionPurchased -> Resolution.Claimed(
            RevenueEventTracker(event.toAmplitudeRevenueEvent(), amplitude),
        )
        else -> Resolution.Declined
    }
}
// --8<-- [end:factories]

// --8<-- [start:ad-impression]
data class AdImpression(val revenue: Double, val network: String) : Event {
    override val name = "ad_impression"
    override val parameters = parameters { put("network", network) }
}

fun AdImpression.toAdjustAdRevenueEvent() = AdjustAdRevenueEvent(
    name = name,
    source = "applovin_max_sdk",
    revenue = revenue,
    currency = "USD",
    adRevenueNetwork = network,
    parameters = parameters,
)

fun AdImpression.toAppsFlyerAdRevenueEvent() = AppsFlyerAdRevenueEvent(
    name = name,
    monetizationNetwork = network,
    mediationNetwork = MediationNetwork.APPLOVIN_MAX,
    revenue = revenue,
    currency = "USD",
    parameters = parameters,
)

class AdsAdjustEventTrackerFactory(private val adjust: AdjustInstance) : AdjustEventTrackerFactory {
    override fun create(event: Event): Resolution<AdjustEventTracker> = when (event) {
        is AdImpression -> Resolution.Claimed(AdjustAdRevenueEventTracker(event.toAdjustAdRevenueEvent(), adjust))
        else -> Resolution.Declined
    }
}

class AdsAppsFlyerEventTrackerFactory(private val appsFlyer: AppsFlyerLib) : AppsFlyerEventTrackerFactory {
    override fun create(event: Event): Resolution<AppsFlyerEventTracker> = when (event) {
        is AdImpression -> Resolution.Claimed(AppsFlyerAdRevenueEventTracker(event.toAppsFlyerAdRevenueEvent(), appsFlyer))
        else -> Resolution.Declined
    }
}
// --8<-- [end:ad-impression]
