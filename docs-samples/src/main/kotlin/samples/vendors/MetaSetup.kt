package samples.vendors

import android.app.Application
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import io.github.mkhytarmkhoian.herald.ConsentService
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.EventTrackerService
import io.github.mkhytarmkhoian.herald.Herald
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.meta.CompositeMetaEventTrackerFactory
import io.github.mkhytarmkhoian.herald.meta.MetaAnalyticsService
import io.github.mkhytarmkhoian.herald.meta.MetaAnalyticsTrackerService
import io.github.mkhytarmkhoian.herald.meta.MetaEventTracker
import io.github.mkhytarmkhoian.herald.meta.MetaEventTrackerFactory
import io.github.mkhytarmkhoian.herald.meta.MetaPurchaseEvent
import io.github.mkhytarmkhoian.herald.meta.toMetaParameters
import io.github.mkhytarmkhoian.herald.meta.trackers.PurchaseEventTracker
import samples.concepts.CheckoutCompleted
import samples.concepts.PlanSelected

fun metaProvider(
    application: Application,
    featureFactories: List<MetaEventTrackerFactory>,
): Herald.Provider {
    // --8<-- [start:provider]
    val logger = AppEventsLogger.newLogger(application)
    val service = MetaAnalyticsService(logger)

    val tracker = MetaAnalyticsTrackerService(
        // Your conversions. Nothing at the end, so other events aren't sent.
        CompositeMetaEventTrackerFactory(featureFactories),
    )

    val provider = Herald.Provider(
        name = "meta",
        events = tracker, // no properties: Meta keeps no user attributes
        identity = service,
        lifecycle = service,
        consent = service,
    )
    // --8<-- [end:provider]
    return provider
}

fun gatedMetaProvider(
    logger: AppEventsLogger,
    featureFactories: List<MetaEventTrackerFactory>,
): Herald.Provider {
    val tracker = MetaAnalyticsTrackerService(CompositeMetaEventTrackerFactory(featureFactories))
    val service = MetaAnalyticsService(logger)
    // --8<-- [start:gate]
    val gate = ConsentGate(events = tracker, consent = service)

    val provider = Herald.Provider(
        name = "meta",
        events = gate,  // dropped while consent is off
        identity = service,
        lifecycle = service,
        consent = gate, // keeps the answer, then turns Meta's own switch
    )
    // --8<-- [end:gate]
    return provider
}

// --8<-- [start:gate-class]
// Meta's own switch doesn't stop the events your app logs, so this drops them while consent is off.
class ConsentGate(
    private val events: EventTrackerService,
    private val consent: ConsentService,
) : EventTrackerService, ConsentService {

    @Volatile
    private var enabled = false

    override suspend fun track(event: Event) {
        if (enabled) events.track(event)
    }

    override suspend fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        consent.setEnabled(enabled)
    }
}
// --8<-- [end:gate-class]

// --8<-- [start:conversions]
class CheckoutMetaEventTrackerFactory(
    private val logger: AppEventsLogger,
) : MetaEventTrackerFactory {

    override fun create(event: Event): Resolution<MetaEventTracker> = when (event) {
        // A purchase: Meta's fb_mobile_purchase, which it counts as revenue.
        is CheckoutCompleted -> Resolution.Claimed(
            PurchaseEventTracker(
                MetaPurchaseEvent(event.name, event.value, event.currency, event.parameters),
                logger,
            ),
        )
        // Any other conversion, under one of Meta's standard names.
        is PlanSelected -> Resolution.Claimed(
            MetaEventTracker {
                logger.logEvent(
                    AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT,
                    event.parameters.toMetaParameters(),
                )
            },
        )
        else -> Resolution.Declined
    }
}
// --8<-- [end:conversions]
