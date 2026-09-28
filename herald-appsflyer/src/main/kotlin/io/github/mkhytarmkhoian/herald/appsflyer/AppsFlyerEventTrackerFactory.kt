package io.github.mkhytarmkhoian.herald.appsflyer

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.requireFallbackLast

/**
 * Turns an event into the AppsFlyer calls it should produce — or declines it.
 *
 * [Resolution.Declined] passes the event to the next factory; [Resolution.Claimed] and
 * [Resolution.Dropped] both end the search, the first sending its handlers and the second sending
 * nothing.
 *
 * A conversion usually goes to AppsFlyer under one of its predefined names. Give it a tracker that
 * makes the call, and a factory that claims the event by its type:
 *
 * ```kotlin
 * class SignUpEventTracker(
 *     private val event: SignUpFinished,
 *     private val appsFlyer: AppsFlyerLib,
 *     private val context: Context,
 * ) : AppsFlyerEventTracker {
 *     override suspend fun track() {
 *         appsFlyer.logEvent(context, AFInAppEventType.COMPLETE_REGISTRATION, event.parameters.toAppsFlyerEventValues())
 *     }
 * }
 *
 * class SignUpAppsFlyerEventTrackerFactory(
 *     private val appsFlyer: AppsFlyerLib,
 *     private val context: Context,
 * ) : AppsFlyerEventTrackerFactory {
 *     override fun create(event: Event): Resolution<AppsFlyerEventTracker> = when (event) {
 *         is SignUpFinished -> Resolution.Claimed(SignUpEventTracker(event, appsFlyer, context))
 *         else -> Resolution.Declined
 *     }
 * }
 * ```
 */
public fun interface AppsFlyerEventTrackerFactory {
    public fun create(event: Event): Resolution<AppsFlyerEventTracker>
}

/** Asks each factory in order and takes the first answer that is not a decline. */
public class CompositeAppsFlyerEventTrackerFactory(
    private val factories: List<AppsFlyerEventTrackerFactory>,
) : AppsFlyerEventTrackerFactory {

    init {
        requireFallbackLast(factories)
    }

    public constructor(vararg factories: AppsFlyerEventTrackerFactory) : this(factories.toList())

    override fun create(event: Event): Resolution<AppsFlyerEventTracker> {
        for (factory in factories) {
            val resolution = factory.create(event)
            if (resolution !is Resolution.Declined) return resolution
        }
        return Resolution.Declined
    }
}
