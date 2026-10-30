package samples.guides

import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.EventTrackerService
import io.github.mkhytarmkhoian.herald.Herald
import io.github.mkhytarmkhoian.herald.firebase.CompositeFirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.CompositeFirebasePropertySetterFactory
import io.github.mkhytarmkhoian.herald.firebase.FirebaseAnalyticsService
import io.github.mkhytarmkhoian.herald.firebase.FirebaseAnalyticsTrackerService
import io.github.mkhytarmkhoian.herald.firebase.FirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.GenericFirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.GenericFirebasePropertySetterFactory
import io.github.mkhytarmkhoian.herald.firebase.ScreenViewFirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.parameters
import org.koin.core.qualifier.named
import org.koin.dsl.module
import samples.CheckoutStarted
import samples.concepts.CheckoutFirebaseEventTrackerFactory

// --8<-- [start:koin-feature]
// :feature:checkout adds its factory, and knows nothing about the app's other vendors.
val checkoutModule = module {
    factory<FirebaseEventTrackerFactory>(named("checkout")) { CheckoutFirebaseEventTrackerFactory(get()) }
}
// --8<-- [end:koin-feature]

// --8<-- [start:koin-root]
// :analytics builds the Firebase provider from whatever the features added.
val firebaseAnalyticsModule = module {
    single<Herald.Provider>(named("firebase")) {
        val firebaseAnalytics = get<FirebaseAnalytics>()
        val tracker = FirebaseAnalyticsTrackerService(
            CompositeFirebaseEventTrackerFactory(
                getAll<FirebaseEventTrackerFactory>() +                    // features, any order
                    ScreenViewFirebaseEventTrackerFactory(firebaseAnalytics) + // then placed by hand
                    GenericFirebaseEventTrackerFactory(firebaseAnalytics),
            ),
            CompositeFirebasePropertySetterFactory(GenericFirebasePropertySetterFactory(firebaseAnalytics)),
        )
        val service = FirebaseAnalyticsService(firebaseAnalytics)
        Herald.Provider("firebase", tracker, tracker, service, service, service)
    }
}
// --8<-- [end:koin-root]

// --8<-- [start:hilt-feature]
@Module
@InstallIn(SingletonComponent::class)
object CheckoutAnalyticsModule {
    @Provides
    @IntoSet
    fun firebase(firebaseAnalytics: FirebaseAnalytics): FirebaseEventTrackerFactory =
        CheckoutFirebaseEventTrackerFactory(firebaseAnalytics)
}
// --8<-- [end:hilt-feature]

// --8<-- [start:vendor-module]
// :analytics:firebase knows every feature's events, through their API modules, so it lists the
// factories itself.
val firebaseVendorModule = module {
    single<Herald.Provider>(named("firebase")) {
        val firebaseAnalytics = get<FirebaseAnalytics>()
        val tracker = FirebaseAnalyticsTrackerService(
            CompositeFirebaseEventTrackerFactory(
                CheckoutFirebaseEventTrackerFactory(firebaseAnalytics), // one for each feature that needs one
                ScreenViewFirebaseEventTrackerFactory(firebaseAnalytics),
                GenericFirebaseEventTrackerFactory(firebaseAnalytics),
            ),
            CompositeFirebasePropertySetterFactory(GenericFirebasePropertySetterFactory(firebaseAnalytics)),
        )
        val service = FirebaseAnalyticsService(firebaseAnalytics)
        Herald.Provider("firebase", tracker, tracker, service, service, service)
    }
}
// --8<-- [end:vendor-module]

// --8<-- [start:interface]
// :feature:checkout:api, one method for each thing that can happen. No Herald, no vendors.
interface CheckoutAnalytics {
    suspend fun checkoutStarted(plan: String, seats: Int)
    suspend fun paymentFailed(reason: String)
}
// --8<-- [end:interface]

// --8<-- [start:feature]
// :feature:checkout calls the interface, and knows nothing about analytics libraries.
class PaymentViewModel(private val analytics: CheckoutAnalytics) {
    suspend fun onPaymentFailed(reason: String) {
        analytics.paymentFailed(reason)
    }
}
// --8<-- [end:feature]

// --8<-- [start:implementation]
// :analytics owns the events, and turns each call into one.
internal data class PaymentFailed(val reason: String) : Event {
    override val name = "payment_failed"
    override val parameters = parameters { put("reason", reason) }
}

internal class HeraldCheckoutAnalytics(private val analytics: EventTrackerService) : CheckoutAnalytics {

    override suspend fun checkoutStarted(plan: String, seats: Int) {
        analytics.track(CheckoutStarted(plan, seats))
    }

    override suspend fun paymentFailed(reason: String) {
        analytics.track(PaymentFailed(reason))
    }
}
// --8<-- [end:implementation]

// --8<-- [start:binding]
// :analytics hands each feature its interface. Herald is bound as EventTrackerService.
val checkoutAnalyticsModule = module {
    single<CheckoutAnalytics> { HeraldCheckoutAnalytics(get()) }
}
// --8<-- [end:binding]
