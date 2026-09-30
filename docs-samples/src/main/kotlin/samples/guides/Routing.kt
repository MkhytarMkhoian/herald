package samples.guides

import com.adjust.sdk.AdjustInstance
import com.google.firebase.analytics.FirebaseAnalytics
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.adjust.AdjustPropertySetter
import io.github.mkhytarmkhoian.herald.adjust.AdjustPropertySetterFactory
import io.github.mkhytarmkhoian.herald.adjust.CompositeAdjustEventTrackerFactory
import io.github.mkhytarmkhoian.herald.adjust.TokenAdjustEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.CompositeFirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.FirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.GenericFirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.ScreenViewFirebaseEventTrackerFactory
import samples.concepts.AppTheme

fun routing(
    firebaseAnalytics: FirebaseAnalytics,
    adjust: AdjustInstance,
    featureFirebaseFactories: List<FirebaseEventTrackerFactory>,
) {
    // --8<-- [start:two-kinds]
    // Firebase gets everything: events no factory took are sent under their own name.
    val firebaseEvents = CompositeFirebaseEventTrackerFactory(
        featureFirebaseFactories +
            ScreenViewFirebaseEventTrackerFactory(firebaseAnalytics) +
            GenericFirebaseEventTrackerFactory(firebaseAnalytics), // everything else
    )

    // Adjust gets only events with a dashboard token.
    val adjustEvents = CompositeAdjustEventTrackerFactory(
        TokenAdjustEventTrackerFactory(mapOf("checkout_completed" to "abc123"), adjust),
    ) // nothing at the end: other events aren't sent
    // --8<-- [end:two-kinds]
    listOf(firebaseEvents, adjustEvents)
}

// --8<-- [start:drop-property]
class AppAdjustPropertySetterFactory : AdjustPropertySetterFactory {
    override fun create(property: Property): Resolution<AdjustPropertySetter> = when (property) {
        is AppTheme -> Resolution.Dropped   // a UI preference, not an attribution signal
        else -> Resolution.Declined
    }
}
// --8<-- [end:drop-property]
