package samples.concepts

import com.google.firebase.analytics.FirebaseAnalytics
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.adjust.AdjustEventTrackerFactory
import io.github.mkhytarmkhoian.herald.adjust.CompositeAdjustEventTrackerFactory
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.firebase.CompositeFirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.FirebaseEventTracker
import io.github.mkhytarmkhoian.herald.firebase.FirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.GenericFirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.RequireMappedFirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.ScreenViewFirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.toBundle
import io.github.mkhytarmkhoian.herald.firebase.trackers.GenericEventTracker
import io.github.mkhytarmkhoian.herald.parameters

data class PayTapped(val plan: String) : Event {
    override val name = "pay_tapped"
    override val parameters = parameters { put("plan", plan) }
}

data class CardNumberSeen(val last4: String) : Event {
    override val name = "card_number_seen"
}

data class CheckoutCompleted(val value: Double, val currency: String) : Event {
    override val name = "checkout_completed"
    override val parameters = parameters {
        put("value", value)
        put("currency", currency)
    }
}

// --8<-- [start:tracker]
class PurchaseFirebaseEventTracker(
    private val event: CheckoutCompleted,
    private val firebaseAnalytics: FirebaseAnalytics,
) : FirebaseEventTracker {

    override suspend fun track() {
        // GA4's recommended purchase event, with the typed parameters as its Bundle.
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, event.parameters.toBundle())
    }
}
// --8<-- [end:tracker]

// --8<-- [start:factory]
class CheckoutFirebaseEventTrackerFactory(
    private val firebaseAnalytics: FirebaseAnalytics,
) : FirebaseEventTrackerFactory {

    override fun create(event: Event): Resolution<FirebaseEventTracker> = when (event) {
        is CheckoutCompleted -> Resolution.Claimed(PurchaseFirebaseEventTracker(event, firebaseAnalytics))
        is PayTapped -> Resolution.Claimed(GenericEventTracker(event, firebaseAnalytics)) // Herald's own
        is CardNumberSeen -> Resolution.Dropped   // mine, and it goes nowhere
        else -> Resolution.Declined               // not mine: ask the next factory
    }
}
// --8<-- [end:factory]

fun chains(firebaseAnalytics: FirebaseAnalytics) {
    // --8<-- [start:chain]
    val chain = CompositeFirebaseEventTrackerFactory(
        CheckoutFirebaseEventTrackerFactory(firebaseAnalytics),  // feature factories first
        ScreenViewFirebaseEventTrackerFactory(firebaseAnalytics), // then Herald's screen views
        GenericFirebaseEventTrackerFactory(firebaseAnalytics),    // then everything else
    )
    // --8<-- [end:chain]

    // --8<-- [start:strict]
    val strict = CompositeFirebaseEventTrackerFactory(
        CheckoutFirebaseEventTrackerFactory(firebaseAnalytics),
        ScreenViewFirebaseEventTrackerFactory(firebaseAnalytics),
        RequireMappedFirebaseEventTrackerFactory, // an unclaimed event throws UnhandledEventException
    )
    // --8<-- [end:strict]
    listOf(chain, strict)
}

fun defaultRules(
    firebaseAnalytics: FirebaseAnalytics,
    firebaseFactories: List<FirebaseEventTrackerFactory>,
    adjustFactories: List<AdjustEventTrackerFactory>,
) {
    // --8<-- [start:default]
    // Firebase: events without a factory of their own are still sent, under their own name.
    val firebase = CompositeFirebaseEventTrackerFactory(
        firebaseFactories + GenericFirebaseEventTrackerFactory(firebaseAnalytics),
    )

    // Adjust: nothing at the end, so only events a factory handled are sent.
    val adjustRules = CompositeAdjustEventTrackerFactory(adjustFactories)
    // --8<-- [end:default]
    listOf(firebase, adjustRules)
}
