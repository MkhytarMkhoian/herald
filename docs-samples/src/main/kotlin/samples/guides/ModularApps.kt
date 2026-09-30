package samples.guides

import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.github.mkhytarmkhoian.herald.Herald
import io.github.mkhytarmkhoian.herald.firebase.CompositeFirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.CompositeFirebasePropertySetterFactory
import io.github.mkhytarmkhoian.herald.firebase.FirebaseAnalyticsService
import io.github.mkhytarmkhoian.herald.firebase.FirebaseAnalyticsTrackerService
import io.github.mkhytarmkhoian.herald.firebase.FirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.GenericFirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.GenericFirebasePropertySetterFactory
import io.github.mkhytarmkhoian.herald.firebase.ScreenViewFirebaseEventTrackerFactory
import org.koin.core.qualifier.named
import org.koin.dsl.module
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
        val service = FirebaseAnalyticsService(firebaseAnalytics, identificationEnabled = true)
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
