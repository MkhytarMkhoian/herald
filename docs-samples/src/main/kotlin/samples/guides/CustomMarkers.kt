package samples.guides

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.firebase.FirebaseEventTracker
import io.github.mkhytarmkhoian.herald.firebase.FirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.toBundle

// --8<-- [start:marker]
/** Money returned to a customer. Your app's concept, so your app's marker. */
interface RefundEvent : Event {
    val amount: Double
    val currency: String
    val transactionId: String
}

data class TicketRefunded(
    override val amount: Double,
    override val currency: String,
    override val transactionId: String,
) : RefundEvent {
    override val name = "ticket_refunded"
}
// --8<-- [end:marker]

// --8<-- [start:marker-tracker]
class RefundFirebaseEventTracker(
    private val event: RefundEvent,
    private val firebaseAnalytics: FirebaseAnalytics,
) : FirebaseEventTracker {

    override suspend fun track() {
        val bundle = event.parameters.toBundle().apply {
            putString(FirebaseAnalytics.Param.TRANSACTION_ID, event.transactionId)
            putDouble(FirebaseAnalytics.Param.VALUE, event.amount)
            putString(FirebaseAnalytics.Param.CURRENCY, event.currency)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.REFUND, bundle) // GA4's reserved refund
    }
}

class RefundFirebaseEventTrackerFactory(
    private val firebaseAnalytics: FirebaseAnalytics,
) : FirebaseEventTrackerFactory {

    override fun create(event: Event): Resolution<FirebaseEventTracker> = when (event) {
        is RefundEvent -> Resolution.Claimed(RefundFirebaseEventTracker(event, firebaseAnalytics))
        else -> Resolution.Declined
    }
}
// --8<-- [end:marker-tracker]

// --8<-- [start:structured]
/** Names an event by where it happened, so two modules cannot pick the same name by accident. */
interface StructuredEvent : Event {
    val screen: String
    val component: String
    val action: String

    override val name: String
        get() = listOf(screen, component, action).filter { it.isNotBlank() }.joinToString("_")
}

data class CheckoutPayButtonTapped(
    override val screen: String = "checkout",
    override val component: String = "pay_button",
    override val action: String = "tap",
) : StructuredEvent
// --8<-- [end:structured]

// --8<-- [start:structured-tracker]
class StructuredFirebaseEventTracker(
    private val event: StructuredEvent,
    private val firebaseAnalytics: FirebaseAnalytics,
) : FirebaseEventTracker {

    override suspend fun track() {
        val bundle: Bundle = event.parameters.toBundle().apply {
            putString("screen", event.screen)   // your call: spread the structure across
            putString("action", event.action)   // parameters, or let the name carry it alone
        }
        firebaseAnalytics.logEvent(event.component, bundle)
    }
}
// --8<-- [end:structured-tracker]
