package samples

import android.content.Context
import com.appsflyer.AppsFlyerLib
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.appsflyer.AppsFlyerEventTracker
import io.github.mkhytarmkhoian.herald.appsflyer.AppsFlyerEventTrackerFactory
import io.github.mkhytarmkhoian.herald.appsflyer.AppsFlyerPurchaseEvent
import io.github.mkhytarmkhoian.herald.appsflyer.trackers.PurchaseEventTracker
import io.github.mkhytarmkhoian.herald.parameters

// --8<-- [start:event]
// The event: what happened, in your app's words. It knows nothing about AppsFlyer.
data class TicketPurchased(val fare: String, val price: Double, val currency: String) : Event {
    override val name = "ticket_purchased"
    override val parameters = parameters { put("fare", fare) }
}
// --8<-- [end:event]

// --8<-- [start:mapping]
// The mapping: the only code that knows how AppsFlyer wants a purchase.
fun TicketPurchased.toAppsFlyerPurchaseEvent() = AppsFlyerPurchaseEvent(
    name = name,
    revenue = price, // sent as af_revenue
    currency = currency,
    contentId = fare,
    parameters = parameters,
)

// The factory: sends the purchase to AppsFlyer, using the mapping above.
class TicketsAppsFlyerEventTrackerFactory(
    private val appsFlyer: AppsFlyerLib,
    private val context: Context,
) : AppsFlyerEventTrackerFactory {
    override fun create(event: Event): Resolution<AppsFlyerEventTracker> = when (event) {
        is TicketPurchased -> Resolution.Claimed(
            PurchaseEventTracker(event.toAppsFlyerPurchaseEvent(), appsFlyer, context),
        )
        else -> Resolution.Declined // not mine
    }
}
// --8<-- [end:mapping]
