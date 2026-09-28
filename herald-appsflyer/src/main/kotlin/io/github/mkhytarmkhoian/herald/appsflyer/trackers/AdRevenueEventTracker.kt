package io.github.mkhytarmkhoian.herald.appsflyer.trackers

import com.appsflyer.AppsFlyerLib
import io.github.mkhytarmkhoian.herald.appsflyer.AdRevenueEvent
import io.github.mkhytarmkhoian.herald.appsflyer.AppsFlyerEventTracker
import io.github.mkhytarmkhoian.herald.appsflyer.toAFAdRevenueData
import io.github.mkhytarmkhoian.herald.appsflyer.toAppsFlyerEventValues

/** Sends one [AdRevenueEvent] through AppsFlyer's ad-revenue API. */
public class AdRevenueEventTracker(
    private val event: AdRevenueEvent,
    private val appsFlyer: AppsFlyerLib,
) : AppsFlyerEventTracker {

    override suspend fun track() {
        appsFlyer.logAdRevenue(event.toAFAdRevenueData(), event.parameters.toAppsFlyerEventValues())
    }
}
