package io.github.mkhytarmkhoian.herald.appsflyer

import com.appsflyer.AppsFlyerLib
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.appsflyer.trackers.AdRevenueEventTracker

/**
 * Claims every [AdRevenueEvent] and declines everything else.
 *
 * Put it before [GenericAppsFlyerEventTrackerFactory], which would otherwise log the impression as
 * an ordinary event.
 */
public class AdRevenueAppsFlyerEventTrackerFactory(
    private val appsFlyer: AppsFlyerLib,
) : AppsFlyerEventTrackerFactory {

    override fun create(event: Event): Resolution<AppsFlyerEventTracker> = when (event) {
        is AdRevenueEvent -> Resolution.Claimed(AdRevenueEventTracker(event, appsFlyer))
        else -> Resolution.Declined
    }
}
