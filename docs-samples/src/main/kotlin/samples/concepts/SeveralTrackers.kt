package samples.concepts

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.mixpanel.MixpanelEventTracker
import io.github.mkhytarmkhoian.herald.mixpanel.MixpanelEventTrackerFactory
import io.github.mkhytarmkhoian.herald.mixpanel.trackers.GenericEventTracker
import io.github.mkhytarmkhoian.herald.parameters

// --8<-- [start:event]
data class OrderPaid(val orderId: String, val total: Double) : Event {
    override val name = "order_paid"
    override val parameters = parameters {
        put("order_id", orderId)
        put("total", total)
    }
}
// --8<-- [end:event]

// --8<-- [start:tracker]
// Adds the amount to the buyer's profile, which Mixpanel's revenue reports read.
class ChargeMixpanelEventTracker(
    private val event: OrderPaid,
    private val mixpanel: MixpanelAPI,
) : MixpanelEventTracker {
    override suspend fun track() {
        mixpanel.people.trackCharge(event.total, null)
    }
}
// --8<-- [end:tracker]

// --8<-- [start:factory]
class CheckoutMixpanelEventTrackerFactory(
    private val mixpanel: MixpanelAPI,
) : MixpanelEventTrackerFactory {
    override fun create(event: Event): Resolution<MixpanelEventTracker> = when (event) {
        is OrderPaid -> Resolution.Claimed(
            GenericEventTracker(event, mixpanel),        // 1. the order_paid event, as usual
            ChargeMixpanelEventTracker(event, mixpanel), // 2. a charge on the buyer's profile
        )
        else -> Resolution.Declined
    }
}
// --8<-- [end:factory]
